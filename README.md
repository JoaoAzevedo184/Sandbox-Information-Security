# Sandbox-Information-Security

Sandbox de estudo da disciplina de **Segurança da Informação** (UNINASSAU, 6º período, 2026.2). Contém implementações práticas e guias passo a passo para os desafios propostos em aula.

## Estrutura

```text
.
├── Main.java              # Ponto de entrada — demo da cifra de Vigenère
├── VigenereCipher.java     # Encrypt/decrypt (Vigenère)
├── VigenereBreaker.java    # Ataque à cifra de Vigenère
├── docs/
│   └── cifra-de-vigenere.md
└── exercises/
    ├── exercises.md        # Lista de atividades e prazos
    └── linux.md             # Passo a passo: auditoria (rsyslog) e GPG
```

## Atividades

Lista completa e prazos em [`exercises/exercises.md`](exercises/exercises.md):

1. Cifra de Vigenère (`VigenereCipher.java`)
2. Ataque à cifra de Vigenère (`VigenereBreaker.java`)
3. Ataque ao esquema One-Time Pad
4. Colisões parciais em funções hash

## Guias

- [`exercises/linux.md`](exercises/linux.md) — auditoria de logs (`rsyslog`) e assinatura digital (GPG/GnuPG), passo a passo.
- [`docs/cifra-de-vigenere.md`](docs/cifra-de-vigenere.md) — notas sobre a implementação da cifra de Vigenère.

## Como rodar

```bash
javac Main.java VigenereCipher.java
java Main
```

## Licença

Ver [LICENSE](LICENSE).
