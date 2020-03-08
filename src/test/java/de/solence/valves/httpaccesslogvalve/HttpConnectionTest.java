package de.solence.valves.httpaccesslogvalve;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.solence.valves.httpaccesslogvalve.Configuration;
import de.solence.valves.httpaccesslogvalve.HttpConnection;
import de.solence.valves.httpaccesslogvalve.Target;
import de.solence.valves.util.TestSocket;

public class HttpConnectionTest {
	private static final String CONTENT_TYPE = "application/json";
	private static final String TOKEN = "testToken";
	private static final String JSON = "{\"msg\":\"test\"}";

	@Test
	public void echo() throws MalformedURLException {
		Target target = mockTarget();

		Configuration config = mock(
				Configuration.class);
		when(config.getTarget()).thenReturn(target);
		when(config.getEndpointUrl())
				.thenReturn(new URL("https://postman-echo.com/post"));
		when(config.getAuthToken()).thenReturn(TOKEN);

		HttpConnection conn = new HttpConnection(config);
		assertTrue(conn.sendMessage(JSON));

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(target).isResponseOk(anyInt(), captor.capture());
		String response = captor.getValue();

		assertTrue(response.contains(CONTENT_TYPE));
		assertTrue(response.contains(TOKEN));
		assertTrue(response.contains(JSON));
	}

	@Test
	public void endpointInvalid() throws MalformedURLException {
		Target target = mockTarget();

		Configuration config = mock(
				Configuration.class);
		when(config.getTarget()).thenReturn(target);
		when(config.getEndpointUrl())
				.thenReturn(new URL("https://invalidHost"));
		when(config.getAuthToken()).thenReturn(TOKEN);

		HttpConnection conn = new HttpConnection(config);
		assertFalse(conn.sendMessage(JSON));
	}

	@Test
	public void timeout() throws MalformedURLException {
		int port = 54321;
		TestSocket socket = new TestSocket(port, 3000);
		Thread socketThread = new Thread(socket);
		socketThread.start();

		Target target = mockTarget();

		Configuration config = mock(
				Configuration.class);
		when(config.getTarget()).thenReturn(target);
		when(config.getEndpointUrl())
				.thenReturn(new URL("http://localhost:" + port));
		when(config.getAuthToken()).thenReturn(TOKEN);
		when(config.getTimeout()).thenReturn(1000);

		HttpConnection conn = new HttpConnection(config);
		assertFalse(conn.sendMessage(JSON));
	}

	private Target mockTarget() {
		Target target = mock(Target.class);
		when(target.getContentType()).thenReturn(CONTENT_TYPE);
		when(target.getAuthenticationHeader(TOKEN))
				.thenReturn("Bearer " + TOKEN);
		when(target.isResponseOk(eq(200), anyString())).thenReturn(true);
		when(target.isResponseOk(not(eq(200)), anyString())).thenReturn(false);
		return target;
	}
}
