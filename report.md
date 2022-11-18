![](https://www.pm2alliance.eu/wp-content/uploads/2019/10/fct-1024x212.jpg)

# SCC<br>Auctions Project</span><br>
### Repositório: [https://github.com/goncalovirginia/CCS](https://github.com/goncalovirginia/CCS)<br>
### André Correia - 64783 - aas.correia@campus.fct.unl.pt<br>
### Gonçalo Virgínia - 56773 - g.virginia@campus.fct.unl.pt<br>
### Rodrigo Fontinha - 64813 - r.fontinha@campus.fct.unl.pt<br>

<br>

## 1. Introdução

Este projeto surge no âmbito da unidade curricular Sistemas de Computação em Cloud e tem como objetivo o desenvolvimento de uma aplicação de leilões à semelhança do EBay, focando no design, na implementação do backend e nos respetivos scripts de teste.

A implementação do projeto é possível através dos serviços e recursos Azure, que fornecem tanto uma plataforma para deployment da aplicação como funcionalidades que tornam todo o processo de desenvolvimento mais fácil e eficaz.

<div style="page-break-after: always"></div>

## 2. Design

O projeto foi separado em duas componentes - **auction.project** e **fun.project** - que, por si, são constituídas por diferentes módulos.

De entre os vários serviços globalmente disponíveis pela Azure, este projeto visa tirar partido de:

* **Storage Account** para gerir quaisquer ficheiros media publicados à aplicação, nomeadamente imagens;
* **CosmosDB** como sistema de armazenamento e gestão de dados;
* **Redis** como sistema de caching para melhorar a performance do projeto em tempo de resposta e redução dos esforço de processamento de dados;
* **Azure Functions** para executar excertos de código serverless;
* **Azure Cognitive Search** para reduzir a complexidade da importação de dados para consumo imediato através de índices.

### 2.1 Auctions App

A componente de aplicação de leilões inclui o backend correspondente ao projeto e os scripts de teste em formato *yml*.

O backend foi dividido por cinco módulos diferentes que interagem entre si para permitir a utilização dos serviços oferecidos pela aplicação. Estes podem ser representados como na tabela seguinte:

| Módulo | Função |
| --- | --- |
| **cache** | Camada de comunicação com o Redis como sistema de caching |
| **data** | Estruturas de dados e camada de comunicação com a CosmosDB |
| **mgt** | Gestão automatizada de recursos Azure e chaves de acesso correspondentes |
| **srv** | Definição de endpoints de interação com os serviços da aplicação |
| **utils** | Armazenamento e disponibilidade de propriedades auxiliares à aplicação |

Os ficheiros *yml* utilizam a tecnologia do **Artillery** para executar diferentes cenários de teste e assegurar o correto funcionamento da aplicação. Os cenários permitem organizar um conjunto de testes definidos sobre dados e endpoints da aplicação, facilitando todo o processo de teste do código desenvolvido.

### 2.2 Functions App

Esta componente do projeto utiliza o serviço Azure Functions para incorporar funções serverless na execução de pequenos segmentos de código.

[...]

<div style="page-break-after: always"></div>

## 3. Implementação

### 3.1 Auctions App

[...]

<div style="page-break-after: always"></div>

## 4. Avaliação

[...]