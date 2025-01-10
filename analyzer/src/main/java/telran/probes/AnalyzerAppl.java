package telran.probes;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.ProbeData;
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
	Consumer<ProbeData> analyzerConsumer(){
		return probeData->{
			log.trace("recive probe: {}", probeData);
			//TODO
			log.debug("deviation : {}"); //deviation
			//TODO
			log.debug("deviation data {} send to { }"); //deviation data, chanal name
		};
	}
	

}
