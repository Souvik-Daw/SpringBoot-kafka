package com.demo.producer;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KafkaProducer {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@PostMapping("/send")
    public String sendMessage() {

        // --- Rider Location ---
        Map<String, Object> riderPayload = Map.of(
                "riderId", "rider123" + (100 + (int) (Math.random() * 900)),
                "latitude", 28.61,
                "longitude", 77.23
        );
        EventEnvelope riderEvent = new EventEnvelope("RIDER_LOCATION", riderPayload);

        // --- Order Created ---
        Map<String, Object> orderPayload = Map.of(
                "orderId", "order-" + System.currentTimeMillis(),
                "amount", 499.99
        );
        EventEnvelope orderEvent = new EventEnvelope("ORDER_CREATED", orderPayload);

        // --- String event ---
        EventEnvelope stringEvent = new EventEnvelope("STRING_EVENT", "PLAIN_STRING_EVENT");

        // --- Send with key extracted from payload ---
        kafkaTemplate.send("my-new-topic2", riderPayload.get("riderId").toString(), riderEvent);
        kafkaTemplate.send("my-new-topic2", orderPayload.get("orderId").toString(), orderEvent);
        kafkaTemplate.send("my-new-topic2", "string-event", stringEvent);

        return "Messages sent";
    }


}
