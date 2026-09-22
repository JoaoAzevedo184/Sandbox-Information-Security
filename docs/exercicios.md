# Exercícios resolvidos — Segurança da Informação

Este documento explica, para cada atividade de [`exercises/exercises.md`](../exercises/exercises.md), **qual é o problema** e **como a solução deste repositório o resolve**, incluindo os resultados reais obtidos ao executar o código.

| # | Atividade | Código | Situação |
|---|---|---|---|
| 1 | Cifra de Vigenère | `src/VigenereCipher.java` | Resolvido |
| 2 | Ataque à cifra de Vigenère | `src/VigenereBreaker.java` | Resolvido — chave `BRASIL` |
| 3 | Ataque ao One-Time Pad | `src/OneTimePadBreaker.java` | Resolvido parcialmente (~64% do texto) |
| 4 | Colisões parciais em hash | `src/HashCollisionCounter.java` | Resolvido |

> **Sobre a execução:** os comandos abaixo assumem que você está na raiz do repositório, porque os caminhos de `data/` são relativos. Compile tudo de uma vez com:
>
> ```bash
> javac -d out src/*.java
> ```

---

## 1. Cifra de Vigenère

### O problema

Escrever um programa capaz de **encriptar e decriptar** textos usando a cifra de Vigenère. A cifra usa uma palavra-chave repetida ao longo do texto; cada letra da chave define o deslocamento aplicado à letra correspondente da mensagem, segundo a aritmética modular:

$$
C_i = (P_i + K_i) \bmod 26
\qquad
P_i = (C_i - K_i + 26) \bmod 26
$$

A teoria completa, com exemplo passo a passo e a comparação com a cifra de César, está em [`docs/cifra-de-vigenere.md`](cifra-de-vigenere.md).

### A solução

A implementação está em [`src/VigenereCipher.java`](../src/VigenereCipher.java). Encriptar e decriptar são a mesma operação com o sinal do deslocamento invertido, então os dois métodos públicos (`encrypt` e `decrypt`) delegam para um único método privado `process`, que recebe um booleano `encrypting`.

Três decisões de implementação merecem atenção:

1. **O índice da chave só avança em letras.** Espaços, pontuação e números são copiados sem alteração e *não* consomem uma letra da chave. Isso mantém o alinhamento entre chave e texto, o que é essencial para que a decifragem funcione e para que o ataque do exercício 2 possa recuperar as colunas corretamente.
2. **A caixa (maiúscula/minúscula) é preservada.** O código escolhe a base `'A'` ou `'a'` conforme o caractere original, de modo que o texto cifrado mantém a aparência do original.
3. **A soma usa `+ 26` antes do módulo.** Em Java, o operador `%` pode devolver valores negativos; somar 26 antes de aplicar o módulo garante um índice válido no alfabeto durante a decifragem.

A chave vazia ou nula é rejeitada com `IllegalArgumentException`, já que um deslocamento indefinido tornaria a operação ambígua.

### Como rodar

Há uma demonstração pronta em `Main.runVigenereDemo()` (cifra e decifra um texto com a chave `chave`), mas ela é privada e não é chamada por `main`. Para usar a cifra diretamente, invoque os métodos estáticos:

```java
String cifrado  = VigenereCipher.encrypt("Texto em claro", "BRASIL");
String decifrado = VigenereCipher.decrypt(cifrado, "BRASIL");
```

---

## 2. Ataque à cifra de Vigenère

### O problema

Recuperar o texto em claro de [`data/text.txt`](../data/text.txt), cifrado com Vigenère, **sem conhecer a chave**. O enunciado fornece três pistas:

- o texto está em **português**;
- não há caracteres não-ASCII (nada de `ã`, `é`, `ç`, `ô`);
- o texto contém a palavra `seguranca`.

A ausência de acentos é importante: significa que o alfabeto é exatamente de 26 letras, o que permite trabalhar com aritmética módulo 26 e com uma tabela de frequências de letras do português sem acentuação.

### A solução

O ataque está em [`src/VigenereBreaker.java`](../src/VigenereBreaker.java) e segue o roteiro clássico em quatro etapas.

**Etapa 1 — normalizar o texto.** `onlyLetters` descarta tudo que não é letra e converte para maiúsculas. Como a cifra não consome chave em caracteres não-alfabéticos, o texto reduzido a letras tem exatamente o mesmo alinhamento de chave que o original. Neste caso sobram **1778 letras**.

**Etapa 2 — descobrir o tamanho da chave.** Duas técnicas independentes são usadas em conjunto:

- **Exame de Kasiski** (`kasiskiExamination`): trigramas repetidos no texto cifrado normalmente indicam o mesmo trigrama em claro cifrado pela mesma parte da chave. Logo, a distância entre duas ocorrências tende a ser um múltiplo do tamanho da chave. O método fatora todas essas distâncias e ordena os fatores por frequência.
- **Índice de coincidência** (`indexOfCoincidence` e `averageIndexOfCoincidence`): o texto é dividido em colunas, uma por posição da chave. Se o tamanho testado for o correto, cada coluna foi cifrada com um único deslocamento — ou seja, é uma cifra de César — e preserva a distribuição irregular de letras do português (IC por volta de 0,07). Se o tamanho estiver errado, as colunas misturam deslocamentos diferentes e a distribuição tende ao uniforme (IC por volta de 0,038).

Na execução real, o índice de coincidência é decisivo:

```text
Candidatos (Kasiski, por frequência de fatores): [2, 3, 6, 4, 12]
  tamanho  5: IC médio = 0,0411
  tamanho  6: IC médio = 0,0736   <- pico
  tamanho  7: IC médio = 0,0412
  tamanho 12: IC médio = 0,0730   <- múltiplo de 6
  tamanho 18: IC médio = 0,0738   <- múltiplo de 6
```

Os picos em 6, 12 e 18 são esperados: qualquer múltiplo do tamanho verdadeiro também produz colunas de deslocamento único. O menor dos picos é o tamanho real.

**Etapa 3 — recuperar cada letra da chave.** Para cada coluna, `bestShiftForColumn` testa os 26 deslocamentos possíveis e escolhe o que minimiza o **qui-quadrado** entre a distribuição observada e a distribuição esperada do português (`PT_FREQ`):

$$
\chi^2 = \sum_{i=1}^{26} \frac{(\text{observado}_i - \text{esperado}_i)^2}{\text{esperado}_i}
$$

Quanto menor o valor, mais o texto decifrado "se parece" com português. `recoverKey` concatena a melhor letra de cada coluna para formar a chave.

**Etapa 4 — escolher entre os tamanhos candidatos.** `bestKeyLengthByChiSquared` monta uma chave para cada tamanho de 1 a 20 (começando pelos candidatos do Kasiski) e compara o qui-quadrado do texto inteiro decifrado. Vale registrar uma limitação: chaves mais longas têm mais liberdade para ajustar a distribuição e poderiam, em princípio, ganhar por excesso de parâmetros. Por isso a tabela de índices de coincidência é impressa no relatório — ela serve de verificação independente. Neste texto as duas medidas concordam em 6.

### Resultado

```text
Tamanho de chave escolhido: 6
Chave recuperada: BRASIL
Contém "seguranca"? true
```

O texto em claro é um artigo sobre Spring Boot, idêntico a [`data/plaintext.txt`](../data/plaintext.txt). O teste da palavra `seguranca` funciona como **crib**: uma confirmação objetiva de que o ataque acertou, em vez de depender de leitura humana do resultado.

### Como rodar

Atenção: `Main.main` **não** executa este ataque. O método existe como `Main.runVigenereAtack(String[])`, é público, mas nunca é chamado. Chame `VigenereBreaker.run` diretamente:

```java
VigenereBreaker.run("data/text.txt");
```

---

## 3. Ataque ao esquema One-Time Pad

### O problema

O One-Time Pad é o único esquema com **sigilo perfeito comprovado**, mas essa garantia depende de uma condição rígida: a chave precisa ser aleatória, do tamanho da mensagem e **usada uma única vez**. O desafio viola exatamente essa condição — a mesma chave foi usada em **7 mensagens distintas**, situação conhecida como *many-time pad*.

### Por que o reuso quebra a cifra

Se duas mensagens usam a mesma chave $K$:

$$
C_1 = P_1 \oplus K
\qquad
C_2 = P_2 \oplus K
$$

então o XOR dos dois textos cifrados elimina a chave:

$$
C_1 \oplus C_2 = P_1 \oplus P_2
$$

A chave desaparece e sobra apenas a relação entre dois textos em claro — informação que não depende de nenhum segredo. A partir daí, o ataque explora uma propriedade do ASCII: **letra XOR letra raramente resulta em uma letra**. Se um palpite de byte de chave faz *todas* as mensagens produzirem letra ou espaço naquela posição, esse palpite é quase certamente o correto.

### A solução

O ataque está em [`src/OneTimePadBreaker.java`](../src/OneTimePadBreaker.java) e recupera a chave **byte por byte, posição por posição**:

1. Para a posição `pos`, identifica quais das 7 mensagens têm comprimento suficiente para cobri-la. Com menos de 3 mensagens disponíveis o ataque desiste da posição (a concordância deixa de ser evidência forte) e marca a chave como `null`.
2. Testa os **256 valores possíveis** de byte de chave. Para cada palpite, decifra o byte de todas as mensagens disponíveis e conta quantos resultados são caracteres plausíveis.
3. `isPlausiblePlainChar` aceita **apenas letras ASCII e espaço**. Usar `Character.isLetter` seria amplo demais: aceitaria as letras acentuadas do Latin-1 (`À`–`ÿ`), o que inflaria as coincidências e destruiria o poder discriminante do teste.
4. Aceita o palpite vencedor só quando há **concordância quase unânime** (`bestScore >= available.size() - 1`) e **nenhum empate** com outro palpite. Posições que não passam nesse filtro ficam como `?` na saída.

Duas particularidades do código:

- `decodeChar` aplica `& 0xFF` ao resultado do XOR. Sem isso, um `byte` negativo em Java sofreria extensão de sinal e produziria um `char` de outro bloco Unicode.
- `checkAgainstReference` compara o resultado com [`data/otp_plaintext.txt`](../data/otp_plaintext.txt). É apenas uma **conferência de estudo** — o ataque não usa esse arquivo para nada. Se o arquivo não existir, o ataque continua funcionando igual.

### Resultado

```text
Mensagens cifradas com a mesma chave: 7

? ?onfi?e??ia?ida?? ?o e ?a?an?ida se a ?ha?e nunc? fcr ?e?t???t????
?n? tim? ??d ? pe??e?to n? ?eo?ia mas fr?gi? na pr?tioa.
?e?sar ? ??av? pe??i?e re?u?er?r as mens?ge?s orig?naes ?e? ???m????????
...
Conferência com data/otp_plaintext.txt: 291/452 caracteres corretos (64,4%)
```

A recuperação é **parcial, e isso é inerente ao ataque com poucas mensagens**. Dos 72 bytes da chave, 44 foram recuperados e 28 ficaram indefinidos. Os `?` têm duas origens distintas:

- **Falta de mensagens (4 posições).** Os comprimentos das 7 mensagens são 68, 56, 72, 72, 59, 64 e 61 caracteres. Nas posições 68 a 71 sobram apenas 2 mensagens, abaixo do mínimo de 3 exigido. Nenhuma quantidade de esforço computacional resolve isso — a informação simplesmente não está lá.
- **Ambiguidade estatística (24 posições).** Com apenas 3 a 7 mensagens, é comum que dois ou mais palpites de chave produzam letras plausíveis em todas elas. O código prefere marcar `?` a arriscar um palpite.

Além disso, **2 dos 44 bytes recuperados estão errados** — são eles que geram trechos como `fcr` em vez de `for` e `pr?tioa` em vez de `pratica`. Vale notar que a tolerância de um desacordo (`available.size() - 1`) **não** é a causa: repetindo o ataque com unanimidade estrita (`bestScore == available.size()`), os mesmos 2 erros permanecem e apenas 1 byte correto é perdido (43 recuperados, 2 errados). Ou seja, com tão poucas mensagens é possível que um byte de chave errado produza letra ou espaço em *todas* elas — o critério de plausibilidade simplesmente não tem poder suficiente para separá-lo do correto. Na prática, um analista completaria essas lacunas por leitura: o texto parcial já é legível o bastante para adivinhar as palavras e, de cada palavra adivinhada, deduzir de volta os bytes de chave que faltam.

### Como rodar

Este é o único ataque que `Main` executa:

```bash
java -cp out Main "" data/otp_texto.txt
```

`Main.main` lê o caminho do OTP em `args[1]` (não em `args[0]`), então o primeiro argumento precisa estar presente mesmo que vazio. Sem argumentos, o padrão `data/otp_texto.txt` é usado:

```bash
java -cp out Main
```

### Gerando um novo desafio

[`src/OneTimePadChallengeGenerator.java`](../src/OneTimePadChallengeGenerator.java) produziu os arquivos do desafio: sorteia uma chave aleatória com `SecureRandom`, do tamanho da maior mensagem, e cifra as 7 mensagens com **a mesma chave**, gravando os textos em claro em `data/otp_plaintext.txt` e os cifrados em hexadecimal em `data/otp_texto.txt`.

> **Cuidado:** rodar `java -cp out OneTimePadChallengeGenerator` **sobrescreve** os dois arquivos com uma chave nova. Os arquivos atuais estão versionados; só regere se quiser um desafio diferente.

---

## 4. Colisões parciais em funções hash

### O problema

Escrever um programa que **conte colisões parciais** de uma função hash. Uma colisão parcial ocorre quando duas entradas diferentes produzem digests que coincidem nos **primeiros N bits**, ainda que os digests completos sejam diferentes.

O interesse prático está no **paradoxo do aniversário**: para encontrar uma colisão em uma saída de N bits não são necessárias $2^N$ tentativas, mas apenas cerca de

$$
\sqrt{\frac{\pi}{2} \cdot 2^{N}} \approx 1{,}25 \cdot 2^{N/2}
$$

Isso é o que explica por que a resistência a colisões de um hash de N bits vale apenas **N/2 bits** de segurança — e por que SHA-256 é escolhido quando se quer 128 bits de resistência a colisão.

### A solução

O programa está em [`src/HashCollisionCounter.java`](../src/HashCollisionCounter.java) e é deliberadamente direto:

1. Gera uma entrada aleatória de 16 bytes com `SecureRandom` (entradas aleatórias garantem independência entre as tentativas, o que é o que o modelo do aniversário pressupõe).
2. Calcula o SHA-256 dessa entrada com `MessageDigest`.
3. Trunca o digest para os **N bits mais significativos** com `truncateBits`, obtendo um `long`.
4. Guarda o valor truncado em um `HashSet<Long>`. Como `Set.add` devolve `false` quando o elemento já existe, a detecção de colisão é uma única chamada — sem comparações par a par, que seriam $O(n^2)$.
5. Registra o total de colisões e a tentativa em que a **primeira** ocorreu, comparando com a estimativa teórica.

`truncateBits` merece uma leitura atenta: ele lê $\lceil N/8 \rceil$ bytes e depois descarta os bits sobrando com um deslocamento à direita **sem sinal** (`>>>`), necessário porque `>>` propagaria o bit de sinal. O `& 0xFF` em cada byte evita a extensão de sinal na montagem do `long`. O limite prático é 63 bits, já que o resultado é um `long` assinado.

O método `selfTest` verifica os três casos que poderiam esconder um erro de deslocamento: 8 bits (primeiro byte), 4 bits (nibble alto) e 16 bits (dois primeiros bytes). Como usa `assert`, ele **só executa com a flag `-ea`** — sem ela, as verificações são silenciosamente ignoradas pela JVM.

### Resultado

Com 16 bits (espaço de 65 536 valores) e 100 000 tentativas:

```text
Tentativas: 100000
Colisões observadas: 48649
Primeira colisão na tentativa: 335
Estimativa teórica (aniversário) para 1ª colisão: ~321 tentativas
```

Com 24 bits (espaço de 16 777 216 valores) e 200 000 tentativas:

```text
Tentativas: 200000
Colisões observadas: 1194
Primeira colisão na tentativa: 3678
Estimativa teórica (aniversário) para 1ª colisão: ~5134 tentativas
```

> **Estes números não se reproduzem.** As entradas vêm de `SecureRandom`, então cada execução dá uma contagem e uma primeira colisão diferentes. O que é estável é a ordem de grandeza e a concordância com a estimativa do aniversário — não os valores exatos. (Todos os outros blocos de saída deste documento *são* determinísticos, porque partem dos arquivos fixos de `data/`.)

A primeira colisão aparece perto do valor previsto (335 contra 321 previstos; 3678 contra 5134), o que confirma a raiz quadrada do paradoxo do aniversário na prática: **8 bits a mais no digest só multiplica o custo do ataque por 16**, não por 256.

Um detalhe de interpretação: "colisões observadas" conta **inserções repetidas** (tentativas menos valores distintos), não pares colidentes. Daí o número altíssimo em 16 bits — com 100 000 tentativas em um espaço de 65 536 valores, o espaço se esgota e quase toda tentativa nova cai sobre um valor já visto (100 000 − 48 649 = 51 351 valores distintos, próximo dos ~51 290 esperados). Esse total não deve ser comparado com a fórmula do aniversário, que prevê apenas *quando surge a primeira* colisão. Para observar o paradoxo do aniversário é mais instrutivo usar um número de tentativas próximo de $\sqrt{2^{N}}$.

### Como rodar

Os argumentos são `[bits] [tentativas]`, com padrão 16 bits e 100 000 tentativas. Use `-ea` para que o `selfTest` realmente execute:

```bash
java -ea -cp out HashCollisionCounter 16 100000
java -ea -cp out HashCollisionCounter 24 200000
```
