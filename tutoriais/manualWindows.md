# WINDOWS

## 1. Clonar o Projeto

Primeiro, clone o repositório do projeto para sua máquina local.

```bash
git clone <URL_DO_REPOSITORIO>
cd ./smart_traffic_control
```

## 2. Instalar o Java Development Kit (JDK) 21

O projeto requer Java 21. Baixe e instale a versão mais recente do JDK 21.

Para instalar, consulte a pasta `tutoriais` no repositório do projeto para ver o tutorial completo de instalação de cada ferramenta.

## 3. Instalar e Configurar o Apache Maven

O Maven é usado para gerenciar as dependências do projeto e para a compilação.

Para instalar, consulte a pasta `tutoriais` no repositório do projeto para ver o tutorial completo de instalação de cada ferramenta.

## 4. Instalar a Biblioteca JADE no Repositório Maven Local

O projeto utiliza a plataforma JADE, ela não vem instalada por padrão no Maven. Você precisará baixar o JADE e instalá-lo no seu repositório Maven local.

Para instalar, consulte a pasta `tutoriais` no repositório do projeto para ver o tutorial completo de instalação de cada ferramenta.

## 5. Compilar o Projeto

Navegue até a pasta raiz do projeto (`smart_traffic_control`) abra o terminal e execute o comando de compilação:

```bash
mvn clean compile
```

## 6. Rodar a Aplicação

Após a compilação bem sucedida, você pode executar a aplicação a partir da pasta raiz do projeto:

```bash
mvn exec:java
```

Agora o projeto Smart Traffic Control deve estar em execução no seu ambiente Windows.
