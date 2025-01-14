package telran.probes.service;

import java.util.HashMap;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.Range;
import telran.probes.dto.SensorUpdateData;

@Configuration
@Service
@Slf4j
public class RangeProviderClientImpl implements RangeProviderClient{
	
	RestTemplate rest = new RestTemplate();
	HashMap<Long, Range> cache = new HashMap<>();
	
	@Value("${app.range.provider.host:localhost}")
	String host;
	
	@Value("${app.range.provider.port:8080}")
	String port;
	
	@Value("${app.range.provider.path:/sensor/range}")
	String path;
	
	
	
	

	@Override
	public Range getRange(long sensorId) {
		// TODO Auto-generated method stub
		//if data in cache return from cache
		//else sent request get response  put in cache and return data
		return null;
	}

	@Bean
	Consumer<SensorUpdateData> updateRangeComsumer(){
		return updateData ->{
			if(cache.containsKey(updateData.id())) {
				cache.put(updateData.id(), updateData.range());
			}
		};
	}
	
	
	
	
	
	
}
