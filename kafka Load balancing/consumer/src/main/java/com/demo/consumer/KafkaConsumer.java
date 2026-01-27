package com.demo.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

	@KafkaListener(topics = "my-topic", groupId = "my-new-group")
	public void listen1(String message) {
		System.out.println("Received Message1: " + message);
	}

	@KafkaListener(topics = "my-topic", groupId = "my-new-group")
	public void listen2(String message) {
		System.out.println("Received Message2: " + message);
	}

	@KafkaListener(topics = "my-new-topic", groupId = "my-new-group-rider")
	public void listenRiderLocation(RiderLocation location) {
		System.out.println("Received location1: " + location.getRiderId() + " latitude: " + location.getLatitude() + " longitude: "
				+ location.getLongitude());
	}

	@KafkaListener(topics = "my-new-topic", groupId = "my-new-group-rider")
	public void listenRiderLocation2(RiderLocation location) {
		System.out.println("Received location2: " + location.getRiderId() + " latitude: " + location.getLatitude() + " longitude: "
				+ location.getLongitude());
	}

}
