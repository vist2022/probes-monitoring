package telran.probes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import telran.probes.dto.Range;
import telran.probes.service.RangeProviderClient;


@SpringBootTest
@Import (TestChannelBinderConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnalyzerServiceTest {

	private static final long SENSOR_ID = 123;
	private static final double MIN_VALUE = 100;
	private static final double MAX_VALUE = 200;
	private static final Range RANGE = new Range(MIN_VALUE, MAX_VALUE);
	
	private static final Range RANGE_DEFAULT = new Range(RangeProviderClient.MIN_DEFALT_VALUE, RangeProviderClient.MAX_DEFALT_VALUE);
	private static final Range RANGE_UPDATED = new Range(MIN_VALUE+10, MAX_VALUE+10);

	private static final long SENSOR_ID_NOT_FOUND = 124;
	
	private static final String URL = "http://localhost:8080/sensor/range/";
	
	@Autowired
	InputDestination producer;
	
	@Autowired
	RangeProviderClient service;
	
	@MockBean
	RestTemplate rest;
	
	@Test
	@Order(1)
	void testNormalFlowNoCash() {
		when(rest.exchange(URL+SENSOR_ID, HttpMethod.GET, null, Range.class))
			.thenReturn(new ResponseEntity<Range>(RANGE, HttpStatus.OK));
		assertEquals(RANGE, service.getRange(SENSOR_ID));
	}

}
