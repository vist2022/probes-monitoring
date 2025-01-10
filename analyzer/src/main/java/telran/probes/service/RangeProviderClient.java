package telran.probes.service;

import telran.probes.dto.Range;

public interface RangeProviderClient {
	
	double MIN_DEFALT_VALUE = -100;
	double MAX_DEFALT_VALUE = 100;

	Range getRange(long sensorId);
}
