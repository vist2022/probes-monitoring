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
import telran.probes.dto.SensorEmails;
import telran.probes.dto.SensorUpdateData;

@Service
@Slf4j
@Configuration
public class EmailsProviderClientImpl  implements EmailsProviderClient{

	@Autowired
	RestTemplate rest ;
	HashMap<Long, String[]> cache = new HashMap<Long, String[]>();
	
	@Value("${app.range.provider.host:localhost}")
	String host;

	@Value("${app.range.provider.port:8080}")
	String port;

	@Value("${app.range.provider.path:/sensor/emails/}")
	String path;
	
	@Override
	public String[] getEmails(long sensorId) {
		 if (cache.containsKey(sensorId)) {
		        log.debug("Range for sensor {} retrieved from cache: {}", sensorId, cache.get(sensorId));
		        return cache.get(sensorId);
		    }
		 String url = String.format("http://%s:%s%s%s", host, port, path, sensorId);
		 try {
		        ResponseEntity<SensorEmails> response = rest.exchange(url, HttpMethod.GET, null, SensorEmails.class);
		        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
		            SensorEmails sensorEmails = response.getBody();
		            cache.put(sensorId, sensorEmails.emails());
		            log.debug("sensorEmails for sensor {} retrieved from remote server and cached: {}", sensorId, sensorEmails.emails());
		            return sensorEmails.emails();
		        }
		    } catch (Exception e) {
		    	log.warn("Return default sensorEmails for sensor {}", sensorId);
		  	    return new String[] {EmailsProviderClient.DEFAULT_EMAIL};
		    }

		    log.warn("Return default sensorEmails for sensor {}", sensorId);
		    return new String[] {EmailsProviderClient.DEFAULT_EMAIL};
		}

	

	@Bean
	Consumer<SensorUpdateData> updateEmailsConsumer(){
		return updateData -> {
			if(cache.containsKey(updateData.id()))
				cache.put(updateData.id(), updateData.emails());
		};
	}
}
