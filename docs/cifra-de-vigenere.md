A **Cifra de Vigenère** é uma técnica clássica de criptografia criada para dificultar a quebra da **Cifra de César**. Ela usa uma **palavra-chave** para aplicar deslocamentos diferentes a cada letra da mensagem.

Por exemplo, imagine:

* Texto: `ATAQUE`
* Chave: `LIMAO`

A chave é repetida até ter o mesmo tamanho do texto:

```text
Texto: A T A Q U E
Chave: L I M A O L
```

Cada letra da chave determina quantas posições a letra do texto será deslocada no alfabeto.

Usando `A = 0`, `B = 1`, ..., `Z = 25`:

```text
A = 0
T = 19
A = 0
Q = 16
U = 20
E = 4
```

E a chave:

```text
L = 11
I = 8
M = 12
A = 0
O = 14
L = 11
```

A fórmula da criptografia é:

$$
C_i = (P_i + K_i) \mod 26
$$

onde:

* \(P_i\) = letra original
* \(K_i\) = letra da chave
* \(C_i\) = letra criptografada
* `26` = quantidade de letras do alfabeto.

No primeiro caractere:

$$
(0 + 11) \mod 26 = 11
$$

`11 = L`.

No segundo:

$$
(19 + 8) \mod 26 = 1
$$

`1 = B`.

Então o resultado começa assim:

```text
ATAQUE
LIMAOL
------
LBMQIP
```

Para **descriptografar**, fazemos o contrário:

$$
P_i = (C_i - K_i + 26) \mod 26
$$

O `+26` evita problemas quando a subtração gera um número negativo.

### Por que ela foi importante?

A Cifra de César sempre usa o mesmo deslocamento. Por exemplo:

```text
A -> D
B -> E
C -> F
```

Então padrões aparecem facilmente.

Na Vigenère, os deslocamentos mudam:

```text
Chave: JAVA
```

Pode acontecer:

```text
A + J
B + A
C + V
D + A
```

Isso tornou a análise de frequência muito mais difícil para os métodos antigos.

### O problema

Apesar de historicamente importante, a Vigenère **não é segura atualmente**. Se a chave for curta e repetida, é possível descobrir o tamanho da chave e depois aplicar análise de frequência separadamente em cada grupo de letras.

Por isso ela é ótima para aprender conceitos como:

* criptografia simétrica;
* chave;
* cifragem e decifragem;
* aritmética modular;
* manipulação de caracteres;
* diferença entre criptografia histórica e criptografia moderna.

Mas não deve ser usada para proteger senhas, arquivos, APIs ou informações reais. Hoje, em Java, você normalmente utilizaria algoritmos modernos como **AES**, por exemplo.

Uma maneira simples de pensar nela é:

```text
César:
todas as letras usam o mesmo deslocamento.

Vigenère:
cada posição pode usar um deslocamento diferente,
determinado pela palavra-chave.
```

Para uma aula de **Segurança em Java**, ela é especialmente interessante porque dá para implementar praticamente tudo manualmente, sem depender inicialmente de bibliotecas criptográficas. Depois você pode comparar sua implementação de Vigenère com AES usando `javax.crypto`, mostrando por que a criptografia moderna é muito mais robusta.
