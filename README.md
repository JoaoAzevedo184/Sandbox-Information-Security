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
│   └── OneTimePadBreaker.java          # Ataque many-time pad (chave reutilizada)
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

1. Cifra de Vigenère (`src/Main.java`)
2. Ataque à cifra de Vigenère (`src/VigenereBreaker.java`, chamado por `Main`)
3. Ataque ao esquema One-Time Pad (`src/OneTimePadBreaker.java`, chamado por `Main`)
4. Colisões parciais em funções hash

## Guias

- [`exercises/linux.md`](exercises/linux.md) — auditoria de logs (`rsyslog`) e assinatura digital (GPG/GnuPG), passo a passo.
- [`docs/cifra-de-vigenere.md`](docs/cifra-de-vigenere.md) — notas sobre a implementação da cifra de Vigenère.

## Como rodar

`Main` é o único ponto de entrada: roda o ataque de Vigenère sobre `data/text.txt` e, em seguida, o ataque ao One-Time Pad sobre `data/otp_texto.txt` (ou os caminhos passados como argumentos, nessa ordem).

```bash
javac -d out src/VigenereCipher.java src/Main.java src/VigenereBreaker.java \
    src/OneTimePadCipher.java src/OneTimePadBreaker.java src/OneTimePadChallengeGenerator.java
java -cp out Main [caminho-vigenere] [caminho-otp]
```

`data/otp_texto.txt` já está versionado, mas para gerar um novo desafio (chave aleatória nova) rode:

```bash
java -cp out OneTimePadChallengeGenerator
```

## Licença

Ver [LICENSE](LICENSE).
