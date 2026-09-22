# Sandbox-Information-Security

Sandbox de estudo da disciplina de **Segurança da Informação** (UNINASSAU, 6º período, 2026.2). Contém implementações práticas e guias passo a passo para os desafios propostos em aula.

## Estrutura

```text
.
├── src/
│   ├── Main.java                       # Ponto de entrada: dispara os ataques
│   ├── VigenereCipher.java             # Encrypt/decrypt (Vigenère) — compartilhado
│   ├── VigenereBreaker.java            # Ataque à cifra de Vigenère
│   ├── OneTimePadCipher.java           # XOR (encrypt/decrypt) — compartilhado
│   ├── OneTimePadChallengeGenerator.java  # Gera o desafio (7 msgs, mesma chave)
│   ├── OneTimePadBreaker.java          # Ataque many-time pad (chave reutilizada)
│   └── HashCollisionCounter.java       # Colisões parciais em SHA-256 (aniversário)
├── data/
│   ├── text.txt                # Texto cifrado com Vigenère (alvo do ataque)
│   ├── plaintext.txt           # Texto original, para conferência
│   ├── otp_texto.txt           # 7 mensagens cifradas com a mesma chave OTP
│   └── otp_plaintext.txt       # As 7 mensagens originais, para conferência
├── docs/
│   └── cifra-de-vigenere.md
└── exercises/
    ├── exercises.md        # Lista de atividades e prazos
    └── linux.md             # Passo a passo: auditoria (rsyslog) e GPG
```

## Atividades

Lista completa e prazos em [`exercises/exercises.md`](exercises/exercises.md):

1. Cifra de Vigenère (`src/VigenereCipher.java`)
2. Ataque à cifra de Vigenère (`src/VigenereBreaker.java`)
3. Ataque ao esquema One-Time Pad (`src/OneTimePadBreaker.java`, chamado por `Main`)
4. Colisões parciais em funções hash (`src/HashCollisionCounter.java`)

## Guias

- [`exercises/linux.md`](exercises/linux.md) — auditoria de logs (`rsyslog`) e assinatura digital (GPG/GnuPG), passo a passo.
- [`docs/cifra-de-vigenere.md`](docs/cifra-de-vigenere.md) — notas sobre a implementação da cifra de Vigenère.
- [`docs/exercicios.md`](docs/exercicios.md) — o problema e a solução de cada exercício, com os resultados reais de execução.

## Como rodar

Compile tudo de uma vez, a partir da raiz do repositório (os caminhos de `data/` são relativos):

```bash
javac -d out src/*.java
```

`Main` roda **apenas** o ataque ao One-Time Pad, sobre `data/otp_texto.txt`:

```bash
java -cp out Main
```

O caminho do arquivo cifrado pode ser trocado, mas `Main` o lê em `args[1]`, então o
primeiro argumento precisa estar presente mesmo que vazio:

```bash
java -cp out Main "" caminho/para/otp.txt
```

O ataque à cifra de Vigenère (exercício 2) **não** é chamado por `Main`. Ele existe como
`Main.runVigenereAtack(String[])` e como `VigenereBreaker.run(String)`; chame um dos dois
para executá-lo sobre `data/text.txt`.

Para o exercício 4 (colisões parciais em hash), rode à parte, passando `[bits] [tentativas]`
(padrão: 16 bits, 100000 tentativas). A flag `-ea` é necessária para que o `selfTest` interno
realmente execute:

```bash
java -ea -cp out HashCollisionCounter 16 100000
```

`data/otp_texto.txt` já está versionado. Para gerar um novo desafio (chave aleatória nova):

```bash
java -cp out OneTimePadChallengeGenerator
```

> **Cuidado:** isso sobrescreve `data/otp_texto.txt` e `data/otp_plaintext.txt`.

## Licença

Ver [LICENSE](LICENSE).
