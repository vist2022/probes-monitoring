package telran.probes;

import java.util.Arrays;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;


import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.DeviationData;
import telran.probes.service.EmailsProviderClient;

@SpringBootApplication
@Slf4j
public class EmailNotifierAppl {

	@Value("${app.notifier.producer.binding.name}")
	String producerBindingName;

	@Autowired
	EmailsProviderClient service;

	@Autowired
	StreamBridge bridge;

	public static void main(String[] args) {
		SpringApplication.run(EmailNotifierAppl.class, args);

	}

	@Bean
	Consumer<DeviationData> emailNotifierConsumer() {
		return deviationData -> {
			log.trace("received deviation data: {}", deviationData);
			long sensorId = deviationData.id();
			log.trace("sensorId : {}", sensorId);
			String[] sensorEmails = service.getEmails(sensorId);
			log.debug("sensor emails data {}", Arrays.toString(sensorEmails));
			bridge.send(producerBindingName, sensorEmails);
			log.debug("sensorEmails data {} sent to {}", sensorEmails, producerBindingName);
		};
	}
}
