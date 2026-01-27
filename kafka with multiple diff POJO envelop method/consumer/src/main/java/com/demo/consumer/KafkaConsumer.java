package com.demo.consumer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class KafkaConsumer {

	@Autowired
    private ObjectMapper objectMapper;

	@KafkaListener(topics = "my-new-topic", groupId = "my-new-group-rider")
	public void listenRiderLocation(RiderLocation location,Acknowledgment ack) {
		System.out.println("Received location1: " + location.getRiderId() + " latitude: " + location.getLatitude() + " longitude: "
				+ location.getLongitude());
		ack.acknowledge();
	}

	@KafkaListener(topics = "my-new-topic", groupId = "my-new-group-rider")
	public void listenRiderLocation2(RiderLocation location,Acknowledgment ack) {
		System.out.println("Received location2: " + location.getRiderId() + " latitude: " + location.getLatitude() + " longitude: "
				+ location.getLongitude());
		ack.acknowledge();
	}

	@KafkaListener(topics = "my-new-topic2", groupId = "my-new-group-rider2")
    public void listen(EventEnvelope envelope, Acknowledgment ack) {

        try {
            switch (envelope.getEventType()) {

                case "RIDER_LOCATION" -> {
                    Map<String, Object> payload = (Map<String, Object>) envelope.getPayload();
                    RiderLocation rider = objectMapper.convertValue(payload, RiderLocation.class);
                    System.out.println("Rider: " + rider.getRiderId() +
                            " lat:" + rider.getLatitude() + " lon:" + rider.getLongitude());
                }

                case "ORDER_CREATED" -> {
                    Map<String, Object> payload = (Map<String, Object>) envelope.getPayload();
                    OrderCreated order = objectMapper.convertValue(payload, OrderCreated.class);
                    System.out.println("Order: " + order.getOrderId() + " amount: " + order.getAmount());
                }

                case "STRING_EVENT" -> {
                    String msg = (String) envelope.getPayload();
                    System.out.println("String Event: " + msg);
                }

                default -> System.out.println("Unknown event type: " + envelope.getEventType());
            }

            ack.acknowledge();
        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
            // optionally log and do not ack to retry later
        }
    }


}
