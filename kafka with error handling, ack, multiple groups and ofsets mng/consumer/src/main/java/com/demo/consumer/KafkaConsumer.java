package com.demo.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

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

}
