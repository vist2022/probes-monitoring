package telran.probes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import telran.probes.dto.DeviationData;
import telran.probes.dto.ProbeData;
import telran.probes.dto.Range;
import telran.probes.service.RangeProviderClient;

@SpringBootTest
@Import (TestChannelBinderConfiguration.class)
class AnalyzerControllerTest {
	
	@MockBean
	RangeProviderClient client;
	
	@Autowired
	InputDestination producer;
	
	@Autowired
	OutputDestination comsumer;
	
	ObjectMapper mapper;
	
	@Value("${app.analyzer.consumer.binding.name:analyzerConsumer-in-0}")
	String consumerBindingName;
	
	@Value("${app.analyzer.producer.binding.name:analyzerProducer-in-0}")
	String producerBindingName;
	
	private static final long SENSOR_ID = 123;
	private static final double MIN_VALUE = 100;
	private static final double MAX_VALUE = 200;
	private static final double NORMAL_VALUE = 150;
	private static final double VALUE_LESS_MIN = 50;
	private static final double VALUE_GREATER_MAX = 220;
	private static final Range RANGE = new Range(MIN_VALUE, MAX_VALUE);
	private static final double DEVIATION_GREATER_MAX = VALUE_GREATER_MAX - MAX_VALUE;
	private static final double DEVIATION_LESS_MIN = VALUE_LESS_MIN - MIN_VALUE;
	private ProbeData probeNormalData = new ProbeData(SENSOR_ID, NORMAL_VALUE, System.currentTimeMillis());
	private ProbeData probeGreaterMaxData = new ProbeData(SENSOR_ID, VALUE_GREATER_MAX, System.currentTimeMillis());
	private ProbeData probeLessMinData = new ProbeData(SENSOR_ID, VALUE_LESS_MIN, System.currentTimeMillis());
	
	@BeforeEach
	void setUp() {
		when(client.getRange(SENSOR_ID)).thenReturn(RANGE);
	}
	
	@Test
	void testNoDeviation() {
		producer.send(new GenericMessage<ProbeData>(probeNormalData), consumerBindingName);
		Message<byte[]> message = comsumer.receive(10, producerBindingName);
		assertNull(message);
	}
	
	
	@Test
	void testGreaterMaxDeviation() throws StreamReadException, DatabindException, IOException {
		producer.send(new GenericMessage<ProbeData>(probeGreaterMaxData), consumerBindingName);
		Message<byte[]> message = comsumer.receive(10, producerBindingName);
		assertNotNull(message);
		DeviationData deviation = mapper.readValue(message.getPayload(), DeviationData.class);
		assertEquals(SENSOR_ID, deviation.id());
		assertEquals(DEVIATION_GREATER_MAX, deviation.deviation());
		assertEquals(MAX_VALUE, deviation.value());
		
	}
	
	@Test
	void testLessMinDeviation() throws StreamReadException, DatabindException, IOException {
		producer.send(new GenericMessage<ProbeData>(probeLessMinData), consumerBindingName);
		Message<byte[]> message = comsumer.receive(10, producerBindingName);
		assertNotNull(message);
		DeviationData deviation = mapper.readValue(message.getPayload(), DeviationData.class);
		assertEquals(SENSOR_ID, deviation.id());
		assertEquals(DEVIATION_LESS_MIN, deviation.deviation());
		assertEquals(MIN_VALUE, deviation.value());
		
	}

	@Test
	void testLoadApplicationContext() {
		assertNotNull(client);
	}
	
	

}
