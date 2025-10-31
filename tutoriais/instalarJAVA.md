# Instalar o Java Development Kit (JDK) 21 no Windows

## 1. Baixar o JDK

1.  Acesse o site oficial da Oracle para baixar o JDK 21:
    *   **Link para Download:** [https://www.oracle.com/java/technologies/downloads/#java21](https://www.oracle.com/java/technologies/downloads/#java21)

2.  Na página, selecione a aba **Windows** e procure pelo link de download do **x64 Installer**. Clique para baixar o arquivo `.exe`.

## 2. Instalar o JDK

1.  Execute o arquivo de instalação que você baixou (ex: `jdk-21_windows-x64_bin.exe`).

## 3. Verificar a Instalação

Para confirmar que o Java foi instalado e configurado corretamente:

1.  Abra um **novo** Prompt de Comando (CMD) ou PowerShell. É importante que seja uma nova janela para que as variáveis de ambiente atualizadas sejam carregadas.
2.  Execute os seguintes comandos:

    ```bash
    java -version
    javac -version
    ```
3.  Ambos os comandos devem exibir a versão do Java que você instalou (ex: `21.x.x`), confirmando que o JDK foi instalado com sucesso.