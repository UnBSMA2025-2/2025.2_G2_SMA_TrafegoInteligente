# Instalação do JADE

1.  **Baixe o JADE:**
    Acesse https://jade.tilab.com/download/jade/license/jade-download/ e faça o download da opção `jadeBin`.

2.  **Extraia o Arquivo:**
    Descompacte o arquivo ZIP baixado. Para facilitar, mova a pasta extraída para um caminho simples, como `C:\jade`.

3.  **Instale no Maven:**
    Abra um Prompt de Comando (CMD) ou PowerShell e navegue até a pasta onde você extraiu o JADE. Em seguida, execute o comando abaixo para instalar o arquivo `jade.jar` no seu repositório local do Maven.

    > **Atenção:** Ajuste o caminho em `-Dfile` se você extraiu o JADE em um local diferente de `C:\jade`.

    ```bash
    mvn install:install-file -Dfile="C:\jade\lib\jade.jar" -DgroupId=com.tilab.jade -DartifactId=jade -Dversion=4.6.0 -Dpackaging=jar
    ```
    *Observação: A versão `4.6.0` é um exemplo comum. Você pode verificar a versão exata do JADE que baixou ou usar esta como padrão.*
