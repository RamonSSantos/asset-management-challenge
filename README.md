# Sistema de Gestão de Ativos

## Descrição

Este projeto implementa um motor de processamento de carteira de investimentos responsável por calcular o patrimônio atualizado e o rendimento acumulado de uma carteira em uma data específica.

A solução realiza o cruzamento entre:

* Histórico de operações (compra e venda);
* Cotações históricas de fechamento dos ativos.

A partir desses dados, o sistema reconstrói a posição da carteira até a data solicitada e calcula:

* Patrimônio atualizado;
* Lucro ou prejuízo acumulado;
* Rendimento percentual.

O projeto foi desenvolvido utilizando Java 21 e possui cobertura de testes unitários com JUnit 5 e Mockito.

---

# Regras de Negócio

O sistema suporta dois tipos de operação:

## Long (Compra)

Representa uma posição comprada.

Características:

* abertura através de compra;
* aumento da posição através de novas compras;
* redução ou encerramento através de vendas;
* lucro obtido quando o preço de venda é superior ao preço médio de aquisição.

---

## Short (Venda a Descoberto)

Representa uma posição vendida.

Características:

* abertura através de venda;
* aumento da posição através de novas vendas;
* encerramento através de recompra;
* lucro obtido quando o preço de recompra é inferior ao preço médio de venda.

---

## Reversão de posição

O sistema também trata automaticamente os seguintes cenários:

* Long → Short
* Short → Long

Quando uma operação ultrapassa a quantidade atualmente em carteira, a posição existente é encerrada e uma nova posição é aberta automaticamente com a quantidade excedente.

Exemplo:

```
Compra 20 ações

Venda 30 ações
```

Resultado:

* encerra posição LONG de 20 ações;
* abre nova posição SHORT de 10 ações.

---

# Arquitetura

A solução foi desenvolvida seguindo o princípio de responsabilidade única (Single Responsibility Principle), separando o processamento das operações dos cálculos financeiros.

Fluxo de execução:

```
Trades
   │
   ▼
PositionProcessor
   │
   ▼
Position
   │
   ▼
PerformanceCalculator
   │
   ▼
PortfolioResult
```

---

## PositionProcessor

Responsável por reconstruir a posição da carteira até uma determinada data.

Funções:

* processar operações em ordem cronológica;
* calcular preço médio;
* controlar quantidade em carteira;
* controlar custo total da posição;
* calcular lucro realizado;
* tratar operações Long;
* tratar operações Short;
* tratar reversão de posição.

Não realiza qualquer cálculo de patrimônio ou rendimento.

---

## PerformanceCalculator

Responsável exclusivamente pelos cálculos financeiros.

Recebe:

* posição consolidada;
* preço de fechamento da data solicitada.

Calcula:

* patrimônio atual;
* lucro/prejuízo total;
* rendimento percentual.

Mantém separação completa entre processamento operacional e cálculo financeiro.

---

## PortfolioService

Camada de orquestração da aplicação.

Responsabilidades:

* processar as operações da carteira;
* localizar a cotação correspondente à data informada;
* delegar os cálculos ao PerformanceCalculator;
* retornar o resultado consolidado.

Não contém regras de negócio relacionadas às operações financeiras.

---

# Modelo de Dados

## Trade

Representa uma operação realizada pelo investidor.

Principais atributos:

* ticker
* data
* tipo da operação
* quantidade
* preço

---

## Quote

Representa a cotação histórica de fechamento.

Principais atributos:

* ticker
* data
* preço de fechamento

---

## Position

Representa o estado consolidado da carteira após o processamento das operações.

Principais atributos:

* tipo da posição (NONE, LONG ou SHORT)
* quantidade
* preço médio
* custo total
* lucro realizado

---

## PortfolioResult

Representa o resultado final da consulta.

Contém:

* patrimônio atualizado
* lucro/prejuízo acumulado
* rendimento percentual

---

# Precisão Numérica

Todos os cálculos financeiros utilizam `BigDecimal`.

Internamente os valores são mantidos com alta precisão para evitar perdas decorrentes de operações sucessivas.

Os valores retornados ao usuário são arredondados para duas casas decimais utilizando `RoundingMode.HALF_UP`, conforme especificado no desafio.

---

# Cobertura de Testes

O projeto possui testes unitários para todas as regras críticas de negócio.

Entre os cenários validados estão:

* abertura de posição LONG;
* aumento de posição LONG;
* venda parcial;
* venda total;
* abertura de posição SHORT;
* aumento de posição SHORT;
* recompra parcial;
* recompra total;
* reversão Long → Short;
* reversão Short → Long;
* cálculo de patrimônio;
* cálculo de lucro;
* cálculo de rendimento percentual;
* carteira vazia;
* lucro realizado;
* arredondamento de valores.

As dependências utilizadas são:

* JUnit Jupiter 5.10.2
* Mockito JUnit Jupiter 5.12.0

---

# Tecnologias

* Java 21
* Maven
* JUnit 5
* Mockito

---

# Estrutura do Projeto

```
src
├── main
│   └── java
│       ├── model
│       ├── service
│       ├── processor
│       ├── util
│       └── ...
│
└── test
    └── java
        ├── PositionProcessorTest
        ├── PerformanceCalculatorTest
        ├── PortfolioServiceTest
        └── ...
```

---

# Como Executar

## Pré-requisitos

* Java 21
* Apache Maven 3.9+

Verifique as versões instaladas:

```bash
java -version

mvn -version
```

---

## Compilar o projeto

```bash
mvn clean compile
```

---

## Executar os testes

```bash
mvn test
```

---

## Gerar o pacote

```bash
mvn clean package
```

---

# Decisões de Implementação

Durante o desenvolvimento foram adotadas algumas decisões arquiteturais:

* separação entre processamento operacional e cálculo financeiro;
* utilização de `BigDecimal` para todos os cálculos monetários;
* utilização de injeção de dependência por construtor;
* tratamento explícito de operações Long e Short;
* suporte à reversão automática entre posições;
* foco em classes pequenas e coesas;
* cobertura de testes unitários para cenários normais e edge cases.

Essas decisões tornam a solução mais legível, testável e de fácil manutenção.
