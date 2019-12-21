package de.solence.valves;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.catalina.LifecycleException;
import org.junit.jupiter.api.Test;

public class HttpAccessLogValveConfigurationTest {

	@Test
	public void testNoParameters() throws LifecycleException {
		assertThrows(LifecycleException.class,
				() -> new HttpAccessLogConfiguration());
	}

}
