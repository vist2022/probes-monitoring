package telran.probes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.client.RestTemplate;

import telran.probes.dto.SensorEmails;
import telran.probes.dto.SensorUpdateData;
import telran.probes.messages.ErrorMessages;
import telran.probes.service.EmailsProviderClient;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailNotifierServiceTest {
	
	private static final String URL = "http://localhost:8080/sensor/emails/";
	private static final long SENSOR_ID = 123;
	private static final long SENSOR_ID_NOT_FOUND = 124;
	private static final long SENSOR_ID_UNAVAILABLE = 125;
	private static final String MAIL_1 = "test@gmail.com";
	private static final String MAIL_2 = "test@co.il";
	
	
	private static final String[] EMAILS = {MAIL_1, MAIL_2};
	private static final String[] UPDATED_EMAILS = {MAIL_1};
	private static final SensorEmails SENSOR_EMAILS = new SensorEmails(SENSOR_ID, EMAILS);
	private static final SensorEmails SENSOR_EMAILS_DEFAULT = new SensorEmails(SENSOR_ID_NOT_FOUND, new String[]{EmailsProviderClient.DEFAULT_EMAIL});
	private String updateBindingName = "updateEmailsConsumer-in-0";
	
	@Autowired
	InputDestination producer;
	@Autowired
	EmailsProviderClient service;
	
	@MockBean
	RestTemplate rest;
	
	
	@Test
	@Order(1)
	void testNormalFlowNoCache() {
		when(rest.exchange(URL+SENSOR_ID, HttpMethod.GET, null, SensorEmails.class))
		.thenReturn(new ResponseEntity<SensorEmails>(SENSOR_EMAILS, HttpStatus.OK));
		assertArrayEquals(EMAILS, service.getEmails(SENSOR_ID));
	}
	
	@Test
	@Order(2)
	void testNormalFlowWithCache() {
		verify(rest, never()).exchange(URL+SENSOR_ID, HttpMethod.GET, null, SensorEmails.class);
		assertArrayEquals(EMAILS, service.getEmails(SENSOR_ID));
	}
	
	@Test
	@Order(3)
	void testSensorNotFound() {
		when(rest.exchange(URL+SENSOR_ID_NOT_FOUND, HttpMethod.GET, null, String.class))
		.thenReturn(new ResponseEntity<>(ErrorMessages.SENSOR_NOT_FOUND, HttpStatus.NOT_FOUND));
		assertArrayEquals(SENSOR_EMAILS_DEFAULT.emails(), service.getEmails(SENSOR_ID_NOT_FOUND));
	}

	@Test
	@Order(4)
	void testDefaultRangeNotInCache() {
		when(rest.exchange(URL+SENSOR_ID_NOT_FOUND, HttpMethod.GET, null, SensorEmails.class))
		.thenReturn(new ResponseEntity<SensorEmails>(SENSOR_EMAILS, HttpStatus.OK));
		assertArrayEquals(EMAILS, service.getEmails(SENSOR_ID_NOT_FOUND));
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	void testRemoteWebServerAnailable() {
		when(rest.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
		.thenThrow(new RuntimeException("Service is ubavailable"));
		assertArrayEquals(SENSOR_EMAILS_DEFAULT.emails(), service.getEmails(SENSOR_ID_UNAVAILABLE));
	}
	
	@Test
	void testUpdateEmailsInMap() throws InterruptedException {
		producer.send(new GenericMessage<SensorUpdateData>(new SensorUpdateData(SENSOR_ID, null, UPDATED_EMAILS)),
				updateBindingName );
		Thread.sleep(1000);
		assertArrayEquals(UPDATED_EMAILS, service.getEmails(SENSOR_ID));
	}
}
