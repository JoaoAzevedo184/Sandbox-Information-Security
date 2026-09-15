# Sandbox-Information-Security

Sandbox de estudo da disciplina de **Segurança da Informação** (UNINASSAU, 6º período, 2026.2). Contém implementações práticas e guias passo a passo para os desafios propostos em aula.

## Estrutura

```text
.
├── src/
│   ├── Main.java              # Demo de cifragem/decifragem
│   ├── VigenereCipher.java    # Encrypt/decrypt (Vigenère) — compartilhado
│   └── VigenereBreaker.java   # Ataque à cifra de Vigenère
├── data/
│   ├── text.txt                # Texto cifrado (alvo do ataque)
│   └── plaintext.txt           # Texto original, para conferência
├── docs/
│   └── cifra-de-vigenere.md
└── exercises/
    ├── exercises.md        # Lista de atividades e prazos
    └── linux.md             # Passo a passo: auditoria (rsyslog) e GPG
```

## Atividades

Lista completa e prazos em [`exercises/exercises.md`](exercises/exercises.md):

1. Cifra de Vigenère (`src/Main.java`)
2. Ataque à cifra de Vigenère (`src/VigenereBreaker.java`)
3. Ataque ao esquema One-Time Pad
4. Colisões parciais em funções hash

## Guias

- [`exercises/linux.md`](exercises/linux.md) — auditoria de logs (`rsyslog`) e assinatura digital (GPG/GnuPG), passo a passo.
- [`docs/cifra-de-vigenere.md`](docs/cifra-de-vigenere.md) — notas sobre a implementação da cifra de Vigenère.

## Como rodar

Atividade 1 — cifra de Vigenère:

```bash
javac -d out src/VigenereCipher.java src/Main.java
java -cp out Main
```

Atividade 2 — ataque à cifra de Vigenère:

```bash
javac -d out src/VigenereCipher.java src/VigenereBreaker.java
java -cp out VigenereBreaker data/text.txt
```

## Licença

Ver [LICENSE](LICENSE).
