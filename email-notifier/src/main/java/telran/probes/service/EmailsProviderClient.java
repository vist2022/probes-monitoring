package telran.probes.service;

public interface EmailsProviderClient {
	
	String DEFAULT_EMAIL = "admin@mail.mail";

	String[] getEmails(long sensorId);
}
