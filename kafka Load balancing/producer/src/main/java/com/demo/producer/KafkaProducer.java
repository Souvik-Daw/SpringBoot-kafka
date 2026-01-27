package com.demo.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KafkaProducer {

	@Autowired
	private KafkaTemplate<String, RiderLocation> kafkaTemplate;

	@PostMapping("/send")
	public String sendMessage(@RequestParam String message) {
		int random3Digit = 100 + (int)(Math.random() * 900); // 100–999
		String riderId = "rider123" + random3Digit;

		RiderLocation location = new RiderLocation(riderId, 28.61, 77.23);
		kafkaTemplate.send("my-new-topic", riderId, location);

		return "Message sent: " + location.getRiderId();
	}

}
