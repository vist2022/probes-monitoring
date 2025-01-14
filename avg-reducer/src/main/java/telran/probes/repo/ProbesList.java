package telran.probes.repo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RedisHash
@RequiredArgsConstructor
@Getter
public class ProbesList {

	@Id
	@NotNull
	Long sensorId;
	List<Double> values = new ArrayList<Double>();
}
