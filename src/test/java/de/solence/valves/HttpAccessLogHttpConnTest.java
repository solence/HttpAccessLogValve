package de.solence.valves;

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

public class HttpAccessLogHttpConnTest {
	private static final String CONTENT_TYPE = "application/json";
	private static final String TOKEN = "testToken";
	private static final String JSON = "{\"msg\":\"test\"}";

	@Test
	public void echo() throws MalformedURLException {
		HttpAccessLogTarget target = mockHttpAccessLogTarget();

		HttpAccessLogConfiguration config = mock(HttpAccessLogConfiguration.class);
		when(config.getTarget()).thenReturn(target);
		when(config.getEndpointUrl()).thenReturn(new URL("https://postman-echo.com/post"));
		when(config.getAuthToken()).thenReturn(TOKEN);

		HttpAccessLogHttpConn conn = new HttpAccessLogHttpConn(config);
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
		HttpAccessLogTarget target = mockHttpAccessLogTarget();

		HttpAccessLogConfiguration config = mock(HttpAccessLogConfiguration.class);
		when(config.getTarget()).thenReturn(target);
		when(config.getEndpointUrl()).thenReturn(new URL("https://invalidHost"));
		when(config.getAuthToken()).thenReturn(TOKEN);

		HttpAccessLogHttpConn conn = new HttpAccessLogHttpConn(config);
		assertFalse(conn.sendMessage(JSON));
	}

	private HttpAccessLogTarget mockHttpAccessLogTarget() {
		HttpAccessLogTarget target = mock(HttpAccessLogTarget.class);
		when(target.getContentType()).thenReturn(CONTENT_TYPE);
		when(target.getAuthenticationHeader(TOKEN)).thenReturn("Bearer " + TOKEN);
		when(target.isResponseOk(eq(200), anyString())).thenReturn(true);
		when(target.isResponseOk(not(eq(200)), anyString())).thenReturn(false);
		return target;
	}
}
