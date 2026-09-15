# Aula 04 — Solução dos Desafios: Segurança, Gestão, Risco e Prática

**Disciplina:** Segurança da Informação
**Período:** 6º período
**Semestre:** 2026.2
**Campus:** UNINASSAU — Olinda/PE

---

## Índice

1. [Visão Geral](#1-visão-geral)
2. [Blueprint para o Laboratório](#2-blueprint-para-o-laboratório--hands-on)
3. [Missão 1 — Configuração de Auditoria (Logs)](#3-missão-1--configuração-de-auditoria-com-logs)
4. [Missão 2 — Criptografia e Não Repúdio](#4-missão-2--criptografia-e-não-repúdio)
5. [Resumo dos Comandos](#5-resumo-dos-comandos)
6. [Resumo Final da Aula](#6-resumo-final-da-aula)
7. [Conceitos-chave para Revisão](#7-conceitos-chave-para-revisão)

---

## 1. Visão Geral

Esta aula apresenta a **resolução prática dos desafios de segurança** propostos anteriormente, com foco em duas missões:

1. **Configuração de Auditoria (Logs)**
2. **Criptografia e Não Repúdio (Assinaturas Digitais)**

O objetivo é transformar conceitos teóricos de Segurança da Informação em atividades práticas, utilizando Linux, `rsyslog` e GPG/GnuPG.

---

## 2. Blueprint para o Laboratório — Hands-on

### Missão 1 — Configuração de Auditoria (Logs)

**Ambiente:** Linux — Terminal

**Objetivo:** Configurar o `rsyslog` para capturar eventos de autenticação de usuários distintos.

**Validação:** Simular um acesso falho e localizar IP, timestamp e registro da falha no arquivo:

```bash
/var/log/auth.log
```

### Missão 2 — Criptografia e Não Repúdio

**Ambiente:** Linux/Windows
**Ferramenta:** GPG / GnuPG

**Objetivo:**

- gerar um par de chaves assimétricas;
- assinar um arquivo de texto;
- verificar a assinatura;
- simular o papel de um receptor.

---

## 3. Missão 1 — Configuração de Auditoria com Logs

### 3.1 Passo 1 — Escolher um terminal Linux online

O material sugere utilizar um ambiente Linux online.

**Ferramentas citadas:**

- Webminal
- DistroSea
- Killercoda

O exemplo adotado utiliza o **DistroSea**.

**Procedimento:**

1. Abra uma nova aba no navegador.
2. Acesse o DistroSea.
3. Caso prefira, pesquise por "Linux Web Terminal".

### 3.2 Passo 2 — Escolher uma distribuição Linux

Linux possui diversas distribuições. Para a atividade, o material recomenda:

- Ubuntu;
- Debian.

**Procedimento:**

1. Na página inicial do serviço, localize uma distribuição.
2. Escolha Ubuntu ou Debian.
3. Selecione uma versão recente.
4. Clique em **Start**.

### 3.3 Passo 3 — Inicialização do ambiente

Como os serviços são gratuitos, pode ser necessário comprovar que o usuário não é um robô.

**Procedimento:**

1. Resolva o CAPTCHA, caso apareça.
2. Aguarde a criação do computador virtual.
3. Ao finalizar, clique em **Launch**.

### 3.4 Passo 4 — Entrar no Terminal

Após a inicialização, poderá aparecer uma interface gráfica ou diretamente uma tela de terminal.

- **Caso apareça uma interface gráfica:** procure pelo aplicativo **Terminal**.
- **Caso apareça uma tela de login:** as credenciais costumam ser informadas pelo próprio serviço. Exemplos comuns:

```text
root
user
```

### 3.5 Passo 5 — Obter privilégios administrativos

Para alterar configurações do sistema é necessário acesso administrativo. No terminal, execute:

```bash
sudo su
```

Caso o sistema peça senha, utilize a senha da máquina virtual.

### 3.6 Passo 6 — Configurar o Rsyslog

O próximo passo é configurar o `rsyslog` para salvar mensagens de autenticação no arquivo correto. Abra o arquivo de configuração:

```bash
nano /etc/rsyslog.conf
```

### 3.7 Passo 7 — Adicionar regra de autenticação

Vá até o final do arquivo e adicione:

```text
auth,authpriv.* /var/log/auth.log
```

Essa regra indica que mensagens relacionadas à autenticação devem ser registradas em `/var/log/auth.log`.

### 3.8 Passo 8 — Salvar a configuração

No editor `nano`:

- **Salvar:** `Ctrl + O`, depois `Enter`
- **Sair:** `Ctrl + X`

### 3.9 Passo 9 — Reiniciar o serviço Rsyslog

Para aplicar a configuração:

```bash
systemctl restart rsyslog
```

### 3.10 Passo 10 — Validar a Auditoria

O objetivo da validação é provocar propositalmente uma falha de login e verificar se ela foi registrada.

#### 3.10.1 Gerar uma tentativa de acesso falha

Execute:

```bash
ssh usuario_errado@localhost
```

O sistema tentará realizar uma conexão SSH com um usuário inválido.

**Procedimento:**

1. Quando for solicitada uma senha, digite qualquer senha incorreta.
2. Repita a tentativa algumas vezes.
3. Aguarde o encerramento da conexão.

#### 3.10.2 Procurar o registro da falha

Execute:

```bash
cat /var/log/auth.log | grep -i "failed"
```

O resultado esperado deve mostrar: data, horário, mensagem `Failed password`, nome do usuário e IP de origem.

Exemplo conceitual:

```text
Failed password for usuario_errado from 127.0.0.1
```

### 3.11 Resultado da Missão 1

Se o registro da tentativa falha aparecer no arquivo, a missão foi concluída.

**O que foi demonstrado:**

- funcionamento de logs de autenticação;
- rastreabilidade de eventos;
- registro de IP;
- registro de timestamp;
- utilidade da auditoria em sistemas.

---

## 4. Missão 2 — Criptografia e Não Repúdio

A segunda missão utiliza criptografia assimétrica para gerar e verificar assinaturas digitais.

### 4.1 Passo 1 — Instalar GPG

No Windows, o material recomenda o **Gpg4win**.

**Procedimento:**

1. Baixe e instale o Gpg4win.
2. Execute o instalador.
3. Mantenha as opções padrão.
4. Certifique-se de que o executável `gpg.exe` esteja disponível no Prompt de Comando.

### 4.2 Passo 2 — Gerar um par de chaves

No Prompt de Comando, execute:

```bash
gpg --full-generate-key
```

**Configuração sugerida:**

- algoritmo: RSA;
- tamanho da chave: 2048 bits ou 4096 bits;
- informar nome;
- informar e-mail;
- criar uma senha forte.

### 4.3 Chaves assimétricas

Ao final do processo, são criadas:

- **chave privada** — deve permanecer protegida e ser usada pelo proprietário;
- **chave pública** — pode ser compartilhada para permitir que outras pessoas verifiquem assinaturas.

### 4.4 Passo 4 — Criar arquivo de texto

Crie um arquivo simples utilizando o Bloco de Notas ou outro editor. Salve como:

```text
mensagem.txt
```

Adicione qualquer conteúdo ao arquivo.

### 4.5 Passo 5 — Assinar o arquivo

No terminal ou Prompt de Comando, execute:

```bash
gpg --detach-sign mensagem.txt
```

Esse comando cria `mensagem.txt.sig`. A assinatura fica separada do conteúdo original.

#### 4.5.1 O que é uma assinatura destacada

Uma assinatura destacada mantém `mensagem.txt` separado de `mensagem.txt.sig`. Isso permite que o arquivo original continue legível enquanto a assinatura pode ser validada separadamente.

### 4.6 Passo 6 — Verificar a assinatura

Execute:

```bash
gpg --verify mensagem.txt.sig mensagem.txt
```

- **Resultado válido:** o GPG deve informar algo semelhante a `Good signature`.
- **Resultado inválido:** o sistema exibirá uma mensagem de erro.

### 4.7 Passo 7 — Exportar a chave pública

Para permitir que outra pessoa valide a assinatura, exporte a chave pública:

```bash
gpg --export -a "Seu Nome" > chave_publica.asc
```

Isso criará `chave_publica.asc`.

#### 4.7.1 Compartilhar a chave pública

O material orienta enviar junto:

```text
mensagem.txt
mensagem.txt.sig
chave_publica.asc
```

O receptor deverá importar a chave pública:

```bash
gpg --import chave_publica.asc
```

Depois poderá verificar a assinatura:

```bash
gpg --verify mensagem.txt.sig mensagem.txt
```

### 4.8 Fluxo da Assinatura Digital

```text
Autor
  |
  | usa chave privada
  v
Assina mensagem.txt
  |
  v
mensagem.txt.sig
  |
  +----------------------+
  |                      |
  v                      v
mensagem.txt       chave_publica.asc
  |                      |
  +----------+-----------+
             |
             v
          Receptor
             |
             | importa chave pública
             v
   verifica a assinatura
```

### 4.9 Não Repúdio

O conceito de **não repúdio** significa que o autor não deve conseguir negar a autoria de uma ação ou mensagem devidamente assinada.

Na prática da aula:

- a assinatura é criada com a chave privada;
- a chave pública é usada para verificação;
- uma assinatura válida serve como evidência de autoria.

---

## 5. Resumo dos Comandos

### 5.1 Missão 1

```bash
sudo su
```

```bash
nano /etc/rsyslog.conf
```

Adicionar:

```text
auth,authpriv.* /var/log/auth.log
```

Reiniciar:

```bash
systemctl restart rsyslog
```

Gerar tentativa falha:

```bash
ssh usuario_errado@localhost
```

Consultar logs:

```bash
cat /var/log/auth.log | grep -i "failed"
```

### 5.2 Missão 2

Gerar chaves:

```bash
gpg --full-generate-key
```

Assinar arquivo:

```bash
gpg --detach-sign mensagem.txt
```

Verificar assinatura:

```bash
gpg --verify mensagem.txt.sig mensagem.txt
```

Exportar chave pública:

```bash
gpg --export -a "Seu Nome" > chave_publica.asc
```

Importar chave pública:

```bash
gpg --import chave_publica.asc
```

---

## 6. Resumo Final da Aula

### Auditoria

A missão de logs demonstra como registrar eventos de autenticação para permitir:

- rastreabilidade;
- identificação de falhas;
- identificação do usuário;
- identificação do IP;
- registro temporal.

### Criptografia

A missão com GPG demonstra:

- uso de chaves assimétricas;
- assinatura digital;
- verificação de integridade;
- autenticação do autor;
- não repúdio.

---

## 7. Conceitos-chave para Revisão

| Conceito | Função |
|---|---|
| **Rsyslog** | Registrar eventos do sistema |
| **auth.log** | Armazenar eventos de autenticação |
| **SSH** | Permitir conexão remota e gerar eventos de login |
| **Timestamp** | Registrar quando uma ação ocorreu |
| **IP** | Identificar a origem da tentativa |
| **GPG/GnuPG** | Gerenciar criptografia e assinaturas |
| **Chave privada** | Criar a assinatura |
| **Chave pública** | Verificar a assinatura |
| **Assinatura digital** | Comprovar autoria e integridade |
| **Não repúdio** | Impedir a negação da autoria |

---

> **Ideia central da aula:** Segurança da Informação precisa ser verificável na prática. Logs fornecem rastreabilidade e evidências de eventos, enquanto assinaturas digitais fornecem integridade, autenticidade e não repúdio.
