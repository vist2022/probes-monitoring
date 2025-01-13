package telran.probes;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.DeviationData;
import telran.probes.dto.ProbeData;
import telran.probes.dto.Range;
import telran.probes.service.RangeProviderClient;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class AnalyzerAppl {

	@Value("${app.analyzer.producer.binding.name}")
	String producerBindingName;

	final RangeProviderClient service;
	final StreamBridge bridge;

	public static void main(String[] args) {
		SpringApplication.run(AnalyzerAppl.class, args);

	}

	@Bean
	Consumer<ProbeData> analyzerConsumer() {
		return probeData -> {
			DeviationData deviation = null;
			Range range = service.getRange(probeData.id());
			double minValue = range.min();
			double maxValue = range.max();
			log.trace("recive probe: {}", probeData);
			if (probeData.value() > maxValue) {
				deviation = new DeviationData(probeData.id(), probeData.value() - maxValue,
						maxValue, probeData.timestamp());
			} else if (probeData.value() < minValue) {
				deviation = new DeviationData(probeData.id(), probeData.value() - minValue,
						minValue, probeData.timestamp());
			}
			log.debug("deviation : {}", deviation); 
			if (deviation != null) {
			    bridge.send(producerBindingName, deviation);
			    log.debug("deviation data {} send to { }", deviation, producerBindingName); 
			}else {
				log.debug("deviation is null and was not send"); 
			}
		};
	}

}
