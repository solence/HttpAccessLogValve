package de.solence.valves.httpaccesslogvalve;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.catalina.LifecycleException;
import org.junit.jupiter.api.Test;

import de.solence.valves.httpaccesslogvalve.Configuration;

public class ConfigurationTest {

	@Test
	public void testNoParameters() throws LifecycleException {
		assertThrows(LifecycleException.class,
				() -> new Configuration());
	}

}
