# Instalar o Apache Maven no Windows

**Pré-requisito:** Certifique-se de que o **JDK (Java Development Kit)** já esteja instalado e configurado corretamente em seu sistema. O Maven precisa do Java para funcionar.

## Baixe o Maven

Acesse o site oficial do Maven para baixar a versão mais recente:
🔗 [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)

*   Procure pelo link de download do arquivo ZIP, geralmente nomeado como `apache-maven-X.Y.Z-bin.zip` (onde X.Y.Z é o número da versão).

## 2. Extrair o Arquivo

1.  Descompacte o conteúdo do arquivo ZIP baixado.
2.  Mova a pasta extraída (ex: `apache-maven-3.9.11`) para um local de sua preferência, como `C:\Program Files\` ou `C:\`.
3.  **Recomendado:** Renomeie a pasta para `maven` e mova-a para a raiz do disco `C:\`. O caminho final, `C:\maven`, será usado como referência nos passos seguintes.

## 3. Configurar Variáveis de Ambiente

Para que o Maven funcione corretamente, você precisa configurar algumas variáveis de ambiente no Windows.

1.  Pressione a tecla `Windows`, digite "variáveis de ambiente" e selecione "Editar as variáveis de ambiente do sistema".
2.  Na janela "Propriedades do Sistema", clique no botão "Variáveis de Ambiente...".

### 3.1. Criar a variável `MAVEN_HOME`

1.  Na seção "Variáveis do sistema", clique em "Novo...".
2.  No campo **Nome da variável**, digite `MAVEN_HOME`.
3.  No campo **Valor da variável**, digite o caminho completo para a pasta do Maven que você extraiu (ex: `C:\maven`).
4.  Clique em "OK".

### 3.2. Atualizar a variável `Path`

1.  Na lista de "Variáveis do usuário", encontre e selecione a variável `Path`.
2.  Clique em "Editar...".
3.  Na janela "Editar variável de ambiente":
    *   Clique em "Novo" e adicione `%MAVEN_HOME%\bin`.
4.  Clique em "OK" em todas as janelas abertas para salvar as alterações.

## 4. Verificar a Instalação

Para confirmar que o Maven foi instalado e configurado corretamente:

1.  Abra um novo Prompt de Comando (CMD) ou PowerShell. É importante abrir uma nova janela para que as variáveis de ambiente atualizadas sejam carregadas.
2.  Execute o seguinte comando:

    ```bash
    mvn -version
    ```

3.  Você deverá ver informações sobre a versão do Apache Maven, a versão do Java e o sistema operacional.
