package telran.probes.service;

import java.util.HashMap;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.Range;
import telran.probes.dto.SensorUpdateData;

@Configuration
@Service
@Slf4j
public class RangeProviderClientImpl implements RangeProviderClient {

	@Autowired
	RestTemplate restTemplate;
	HashMap<Long, Range> cache = new HashMap<>();

	@Value("${app.range.provider.host:localhost}")
	String host;

	@Value("${app.range.provider.port:8080}")
	String port;

	@Value("${app.range.provider.path:/sensor/range/}")
	String path;

	@Override
	public Range getRange(long sensorId) {
	    if (cache.containsKey(sensorId)) {
	        log.debug("Range for sensor {} retrieved from cache: {}", sensorId, cache.get(sensorId));
	        return cache.get(sensorId);
	    }

	    String url = String.format("http://%s:%s%s%s", host, port, path, sensorId);
	    try {
	        ResponseEntity<Range> response = restTemplate.exchange(url, HttpMethod.GET, null, Range.class);
	        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
	            Range range = response.getBody();
	            cache.put(sensorId, range);
	            log.debug("Range for sensor {} retrieved from remote server and cached: {}", sensorId, range);
	            return range;
	        }
	    } catch (Exception e) {
	    	log.warn("Return default range for sensor {}", sensorId);
	  	    return new Range(MIN_DEFALT_VALUE, MAX_DEFALT_VALUE);
	    }

	    log.warn("Return default range for sensor {}", sensorId);
	    return new Range(MIN_DEFALT_VALUE, MAX_DEFALT_VALUE);
	}

	@Bean
	Consumer<SensorUpdateData> updateRangeComsumer() {
		return updateData -> {
			if (cache.containsKey(updateData.id())) {
				cache.put(updateData.id(), updateData.range());
			}
		};
	}


}
