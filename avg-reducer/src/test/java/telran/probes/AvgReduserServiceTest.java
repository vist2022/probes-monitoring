package telran.probes;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import telran.probes.dto.ProbeData;
import telran.probes.repo.ProbesList;
import telran.probes.repo.ProbesListRepository;
import telran.probes.service.AvgReducerService;

@SpringBootTest
class AvgReduserServiceTest {

	private static final Double VALUE = 100.;
	private static final long SENSOR_ID = 123;
	private ProbeData data = new ProbeData(SENSOR_ID, VALUE, 0);
	HashMap<Long, ProbesList> reddisMockMap= new HashMap<>();
	
	@Autowired
	AvgReducerService service;
	
	@MockBean
	ProbesListRepository repo;
	
	@BeforeEach
	void setUp() {
		when(repo.findById(any(Long.class))).then(new Answer<Optional<ProbesList>>() {

			@Override
			public Optional<ProbesList> answer(InvocationOnMock invocation) throws Throwable {
				Long sensorId = invocation.getArgument(0);
				ProbesList list=reddisMockMap.get(sensorId);
				return Optional.ofNullable(list);
			}
		});
		
		when(repo.save(any(ProbesList.class))).then(new Answer<ProbesList>() {

			@Override
			public ProbesList answer(InvocationOnMock invocation) throws Throwable {
				ProbesList list=invocation.getArgument(0);
				reddisMockMap.put(list.getSensorId(), list);
				return list;
			}
		});
	}
	
	
	@Test
	void test() {
		Double res = service.getAvgValue(data);
		assertNull(res);
		res = service.getAvgValue(data);
		assertNotNull(res);
		assertEquals(VALUE, res);
		
		res = service.getAvgValue(data);
		assertNull(res);
		res = service.getAvgValue(data);
		assertNotNull(res);
		assertEquals(VALUE, res);
	}

}
