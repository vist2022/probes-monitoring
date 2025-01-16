package telran.probes;

import java.util.Arrays;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.DeviationData;
import telran.probes.service.EmailsProviderClient;

@SpringBootApplication
@Slf4j
public class EmailNotifierAppl {


	@Autowired
	EmailsProviderClient service;

	@Autowired
	JavaMailSender sender;


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
			SimpleMailMessage smm = new SimpleMailMessage();
			smm.setTo(sensorEmails);
			smm.setSubject("Deviation data for sensor" + deviationData.id());
			smm.setText(String.format("Sensor %d has deviation %.2f value %.2f", deviationData.id(), deviationData.deviation(),deviationData.value()));
			sender.send(smm);
			log.debug("deviation data {} sent to email {}", deviationData, Arrays.toString(sensorEmails));
			
		};
	}

	
}
