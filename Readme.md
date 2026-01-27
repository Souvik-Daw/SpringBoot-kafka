....................Kafka Cmd....................
Create Topic
docker exec -it kafka kafka-topics --create --topic orders --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

List topics
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

Describe topic
docker exec -it kafka kafka-topics --describe --topic orders --bootstrap-server localhost:9092

Get topic offsets
docker exec -it kafka kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic orders

Delete topic
docker exec -it kafka kafka-topics --delete --topic orders --bootstrap-server localhost:9092

Produce 
docker exec -it kafka kafka-console-producer --topic orders --bootstrap-server localhost:9092

Producer with key
docker exec -it kafka kafka-console-producer --topic orders --bootstrap-server localhost:9092 --property "parse.key=true" --property "key.separator=:"
order1:created
order1:paid

Consume
docker exec -it kafka kafka-console-consumer --topic orders --from-beginning --bootstrap-server localhost:9092

Consume as a group
docker exec -it kafka kafka-console-consumer --topic orders --group test-group --bootstrap-server localhost:9092

List consumer groups
docker exec -it kafka kafka-consumer-groups --list --bootstrap-server localhost:9092

Describe consumer group
docker exec -it kafka kafka-consumer-groups --describe --group test-group --bootstrap-server localhost:9092
....................Theory....................

Producer
Sends messages to a topic
Topic is split into partitions
One producer can write to all partitions
Partition selection:
	No key → round-robin (load balanced across partitions)
	With key → same key → same partition (ordering guaranteed per key)
	
Partitions
Unit of parallelism
Order guaranteed per partition
More partitions → more parallel consumers possible

Consumer Group
Consumers with same group.id form a group
Load balancing happens within the group
Each partition → only one consumer
Each consumer → can handle multiple partitions
Each group gets all messages independently
→ Different groups reading the same topic act like pub/sub

Consumer ↔ Partition Rules
1 partition → 1 consumer (per group)
1 consumer → multiple partitions possible
Consumers > partitions → extra consumers idle

Load Balancing (Very Important)
Kafka load-balances partitions, not individual messages
Load balancing works only if messages are spread across partitions
(No key or high-cardinality keys)
Low-cardinality or fixed keys → hot partition → uneven load

YT ref -> https://www.youtube.com/watch?v=NWLwGtkBrkQ

....................Additional topics....................

1️⃣ Kafka Error Handling
Retry Strategies + Dead Letter Topic (DLT)
Goal
Retry a failed message 3 times
If still failing → send to DLT
Do NOT commit offset on failure

1.1 application.yml (minimal & correct)
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: order-group
      auto-offset-reset: earliest
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      enable-idempotence: true

    listener:
      ack-mode: manual

1.2 Kafka Error Handler Bean (THIS is the core)
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(template);

        FixedBackOff backOff = new FixedBackOff(2000, 3); // 3 retries

        return new DefaultErrorHandler(recoverer, backOff);
    }
}

What this does:
try → fail
retry → fail
retry → fail
retry → fail
→ send to <topic>.DLT

1.3 Consumer with Failure
@KafkaListener(topics = "orders")
public void consume(
        OrderEvent event,
        Acknowledgment ack
) {
    if (event.getAmount() < 0) {
        throw new RuntimeException("Invalid order");
    }

    System.out.println("Processed order " + event.getOrderId());
    ack.acknowledge(); // commit offset ONLY on success
}

✔️ Offset committed only if processing succeeds
✔️ Failed messages go to DLT

1.4 DLT Consumer (optional but realistic)
@KafkaListener(topics = "orders.DLT")
public void consumeDLT(OrderEvent event) {
    System.out.println("DLT message: " + event);
}

2️⃣ Offset Management (Manual & Safe)
Goal
Control when offset is committed
Avoid message loss
Allow retries

2.1 Disable Auto Commit (already done)
enable-auto-commit: false
listener:
  ack-mode: manual

2.2 Manual Offset Commit (Correct Way)
@KafkaListener(topics = "payments")
public void consume(
        PaymentEvent event,
        Acknowledgment ack
) {
    processPayment(event);   // DB write, API call, etc
    ack.acknowledge();       // offset committed here
}

Execution Flow
poll()
processPayment()
ack()
If app crashes before ack() → Kafka re-delivers.
2.3 What NOT to do ❌
ack.acknowledge();
process(event); // ❌ message loss if crash
2.4 Offset Reset Example
auto-offset-reset: earliest
Used when:
New consumer group
No offset exists

3️⃣ Producer ACKS (Reliability Config)
Goal
Ensure message durability
Avoid message loss
Avoid duplicates

3.1 Producer Config (Minimal & Correct)
spring:
  kafka:
    producer:
      acks: all
      retries: 3
      enable-idempotence: true

3.2 Producer Code
@Autowired
private KafkaTemplate<String, OrderEvent> kafkaTemplate;

public void publish(OrderEvent event) {
    kafkaTemplate.send(
        "orders",
        event.getOrderId(), // KEY
        event
    );
}

✔️ acks=all → leader + replicas
✔️ enable-idempotence=true → no duplicates
✔️ key ensures ordering per order
3.3 What Happens Internally
Producer → Leader
Leader → Replicas
All ACK → Producer success
If retry happens → Kafka detects duplicate → drops it.
