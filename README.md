# Módulo 4 – Event Streaming com Kafka (Retrocore)

Este repositório implementa o **Módulo 4** do projeto Retrocore: uma solução de **event streaming** baseada em **Apache Kafka**, com:

- **1 produtor HTTP** (`lambda-producer`) que publica eventos em tópicos do Kafka.
- **2 consumidores independentes**:
  - `lambda-consumer-analytics` – focado em eventos de uso/analytics.
  - `lambda-consumer-monitoring` – focado em eventos de saúde/monitoramento.

A ideia é simular uma arquitetura em que uma “Lambda de auditoria” recebe eventos de diferentes tipos, roteia para múltiplos tópicos e processa cada tipo de evento em serviços separados.

---

## 1. Visão Geral da Arquitetura

### 1.1 Componentes principais

- `lambda-producer/`  
  Serviço REST responsável por receber requisições HTTP e publicar mensagens no Kafka:
  - Endpoint: `POST /api/v1/events`
  - Payload define o tipo de evento (`ANALYTICS` ou `MONITORING`).
  - Usa `KafkaTemplate` para enviar mensagens para dois tópicos distintos.

- `lambda-consumer-analytics/`  
  Consumer que lê apenas eventos **do tipo `ANALYTICS`**, transformando a mensagem em um `AnalyticsEvent` e registrando logs estruturados.

- `lambda-consumer-monitoring/`  
  Consumer que lê apenas eventos **do tipo `MONITORING`**, transformando a mensagem em um `MonitoringEvent` e registrando logs estruturados de monitoramento.

- `docker-compose.yml`  
  Orquestra o ambiente completo:
  - `zookeeper`
  - `kafka`
  - `lambda-producer`
  - `lambda-consumer-analytics`
  - `lambda-consumer-monitoring`
  - `kafdrop` (UI para inspecionar tópicos e mensagens)

---

## 2. Arquitetura Lógica

Fluxo de ponta a ponta:

1. Um cliente HTTP (Postman, curl, etc.) envia um `POST` para o **lambda-producer**.
2. O producer monta um `EventPayload` e envia para um tópico Kafka:
   - `retrocore-mod4-analytics` (eventos de analytics)
   - `retrocore-mod4-monitoring` (eventos de monitoramento)
3. Cada consumer:
   - Lê o tópico configurado.
   - Desserializa o JSON em um modelo de domínio.
   - Aplica o caso de uso (`ProcessAnalyticsEventUseCase` ou `ProcessMonitoringEventUseCase`).
   - Registra o evento em log de forma estruturada.

De forma simplificada:

```text
Cliente HTTP
    |
    v
[ lambda-producer ]
    |
    +--> [ Kafka - tópico retrocore-mod4-analytics ] ---> [ lambda-consumer-analytics ]
    |
    +--> [ Kafka - tópico retrocore-mod4-monitoring ] ---> [ lambda-consumer-monitoring ]
````

---

## 3. Organização das Pastas

### 3.1 `lambda-producer/`

Responsável pelo **entrypoint HTTP** do sistema de eventos.

* `api/`

  * `MessageController.java`
    Controller REST que expõe:

    * `POST /api/v1/events`

    Exemplo de payload:

    ```json
    {
      "type": "ANALYTICS",
      "userId": "user-123",
      "source": "web",
      "message": "Usuário clicou em comprar"
    }
    ```

* `application/usecase/`

  * `PublishEventUseCase.java`
    Caso de uso que valida o payload (ex.: `userId` obrigatório) e delega para a porta de publicação.

* `domain/model/`

  * `EventPayload.java` – representa o evento genérico (tipo, usuário, origem, mensagem, timestamp).
  * `EventType.java` – enum com `ANALYTICS` e `MONITORING`.

* `domain/port/`

  * `EventPublisher.java` – porta (interface) para publicação de eventos.

* `infrastructure/kafka/`

  * `KafkaEventPublisher.java` – implementação da porta que:

    * Serializa `EventPayload` em JSON.
    * Escolhe o tópico com base no `EventType`.
    * Envia a mensagem usando `KafkaTemplate<String, String>`.

* `infrastructure/config/`

  * `JacksonConfig.java` – configuração de `ObjectMapper` para datas (`Instant`) e JSON.

* `src/main/resources/application.properties`
  Contém configurações base de Kafka e propriedades da aplicação (porta HTTP, bootstrap servers, nomes de tópicos via variáveis de ambiente).

---

### 3.2 `lambda-consumer-analytics/`

Serviço responsável por processar eventos de **analytics**.

* `domain/model/AnalyticsEvent.java`
  Evento de analytics já normalizado (userId, source, message, type, timestamp).

* `domain/port/AnalyticsProcessor.java`
  Porta que representa o contrato de processamento de um evento de analytics.

* `application/usecase/ProcessAnalyticsEventUseCase.java`
  Implementa a porta, fazendo o **log estruturado** dos eventos:

  ```text
  [ANALYTICS] user={userId} source={source} msg={message} type={rawType} ts={timestamp}
  ```

* `infrastructure/kafka/KafkaAnalyticsListener.java`

  * Consome mensagens do Kafka usando `@KafkaListener`.
  * Desserializa o JSON recebido em um DTO interno.
  * Filtra mensagens de tipo `ANALYTICS`.
  * Constrói um `AnalyticsEvent` e delega para `AnalyticsProcessor`.

* `infrastructure/config/JacksonConfig.java`
  Configuração do `ObjectMapper` para trabalhar bem com `Instant`.

* `src/main/resources/application.properties`
  Configura:

  * `server.port`
  * `spring.kafka.bootstrap-servers`
  * `spring.kafka.consumer.group-id`
  * Tópico usado por este consumer via `app.kafka.topic` / `APP_KAFKA_TOPIC`.

---

### 3.3 `lambda-consumer-monitoring/`

Estrutura muito semelhante ao consumer de analytics, mas focada em **eventos de monitoramento**.

* `domain/model/MonitoringEvent.java`

* `domain/port/MonitoringProcessor.java`

* `application/usecase/ProcessMonitoringEventUseCase.java`
  Log estruturado do tipo:

  ```text
  [MONITORING] user={userId} type={rawType} msg={message} ts={timestamp}
  ```

* `infrastructure/kafka/KafkaMonitoringListener.java`

  * Consome o tópico configurado.
  * Filtra somente mensagens de tipo `MONITORING`.
  * Converte para `MonitoringEvent` e delega para o caso de uso.

* `infrastructure/config/JacksonConfig.java`

* `src/main/resources/application.properties`

---

## 4. Docker Compose

Arquivo: `docker-compose.yml` na raiz.

Serviços definidos:

* `zookeeper` – suporte ao Kafka.
* `kafka` – broker principal.
* `lambda-producer` – exposto na porta `8081` (host).
* `lambda-consumer-analytics` – consumer de analytics.
* `lambda-consumer-monitoring` – consumer de monitoring.
* `kafdrop` – interface web para inspecionar tópicos e mensagens (`http://localhost:9000`).

Para subir o ambiente completo:

```bash
docker compose up -d --build
```

Para ver os serviços rodando:

```bash
docker ps
```

---

## 5. Como Testar Diretamente nos Containers

### 5.1 Subir o ambiente

Na raiz do repositório:

```bash
docker compose up -d --build
```

Isso irá subir:

* Kafka + Zookeeper
* Producer (`mod4-lambda-producer`)
* Dois consumers (`mod4-lambda-consumer-analytics` e `mod4-lambda-consumer-monitoring`)
* Kafdrop (`mod4-kafdrop`)

---

### 5.2 Enviar eventos via HTTP (producer)

Com o ambiente rodando, envie requisições para o **producer**:

#### Evento de Analytics

```bash
curl -X POST http://localhost:8081/api/v1/events `
  -H "Content-Type: application/json" `
  -d "{
    \"type\": \"ANALYTICS\",
    \"userId\": \"user-123\",
    \"source\": \"web\",
    \"message\": \"Usuário clicou em comprar\"
  }"
```

#### Evento de Monitoring

```bash
curl -X POST http://localhost:8081/api/v1/events `
  -H "Content-Type: application/json" `
  -d "{
    \"type\": \"MONITORING\",
    \"userId\": \"user-999\",
    \"source\": \"lambda\",
    \"message\": \"CPU acima de 80%\"
  }"
```

A resposta será uma mensagem simples confirmando o envio:

```text
Evento publicado com sucesso para ANALYTICS
```

ou

```text
Evento publicado com sucesso para MONITORING
```

---

### 5.3 Verificar processamento nos consumers (dentro dos containers)

Para acompanhar os logs do consumer de analytics:

```bash
docker logs -f mod4-lambda-consumer-analytics
```

Exemplo de saída esperada:

```text
[ANALYTICS] user=user-123 source=web msg=Usuário clicou em comprar type=ANALYTICS ts=2025-12-02T10:05:00Z
```

Para acompanhar os logs do consumer de monitoring:

```bash
docker logs -f mod4-lambda-consumer-monitoring
```

Exemplo de saída:

```text
[MONITORING] user=user-999 type=MONITORING msg=CPU acima de 80% ts=2025-12-02T10:06:00Z
```

---

### 5.4 Inspecionar tópicos via Kafdrop

Acesse no navegador:

* `http://localhost:9000`

Lá é possível:

* Ver os tópicos criados (`retrocore-mod4-analytics`, `retrocore-mod4-monitoring`, etc.).
* Ler mensagens de cada tópico.
* Confirmar que os eventos enviados pelo producer chegaram corretamente no Kafka.

---

## 6. Configuração de Tópicos e Variáveis de Ambiente

Os nomes dos tópicos e o bootstrap server são configurados via **variáveis de ambiente** no `docker-compose.yml`, por exemplo:

```yaml
lambda-producer:
  environment:
    SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
    APP_KAFKA_TOPIC_ANALYTICS: "retrocore-mod4-analytics"
    APP_KAFKA_TOPIC_MONITORING: "retrocore-mod4-monitoring"
```

Os consumers recebem:

```yaml
lambda-consumer-analytics:
  environment:
    SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
    APP_KAFKA_TOPIC: "retrocore-mod4-analytics"

lambda-consumer-monitoring:
  environment:
    SPRING_KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
    APP_KAFKA_TOPIC: "retrocore-mod4-monitoring"
```

Dentro das aplicações, essas variáveis são utilizadas para montar:

* `spring.kafka.bootstrap-servers`
* `app.kafka.topic`
* Propriedades específicas de cada tópico.

