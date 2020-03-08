package de.solence.valves.httpaccesslogvalve;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SenderTest {
	private static final String MESSAGE = "message";

	@Test
	public void sendSingleEvent() {
		BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);

		queue.add(createEvent());

		Target target = mockTarget(25);

		Configuration config = mock(Configuration.class);
		when(config.getTarget()).thenReturn(target);

		HttpConnection conn = mock(HttpConnection.class);
		when(conn.sendMessage(anyString())).thenReturn(true);

		Sender sender = new Sender(config, conn, queue);
		sender.run();

		verify(conn).sendMessage(anyString());
	}

	@Test
	public void sendConcatenatedEvents() {
		BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);

		queue.add(createEvent());
		queue.add(createEvent());
		queue.add(createEvent());

		Target target = mockTarget(25);

		Configuration config = mock(Configuration.class);
		when(config.getTarget()).thenReturn(target);

		HttpConnection conn = mock(HttpConnection.class);
		when(conn.sendMessage(anyString())).thenReturn(true);

		Sender sender = new Sender(config, conn, queue);
		sender.run();

		verify(conn).sendMessage(anyString());
	}

	@Test
	public void sendMultipleMessages() {
		BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);

		queue.add(createEvent());
		queue.add(createEvent());
		queue.add(createEvent());

		Target target = mockTarget(2);

		Configuration config = mock(Configuration.class);
		when(config.getTarget()).thenReturn(target);

		HttpConnection conn = mock(HttpConnection.class);
		when(conn.sendMessage(anyString())).thenReturn(true);

		Sender sender = new Sender(config, conn, queue);
		sender.run();

		verify(conn, times(2)).sendMessage(anyString());
	}

	@Test
	public void retryFailedSend() {
		BlockingQueue<Event> queue = new ArrayBlockingQueue<>(10);

		queue.add(createEvent());

		Target target = mockTarget(25);

		Configuration config = mock(Configuration.class);
		when(config.getTarget()).thenReturn(target);

		HttpConnection conn = mock(HttpConnection.class);

		when(conn.sendMessage(anyString())).thenAnswer(new Answer<Boolean>() {
			private int count = 2;

			public Boolean answer(InvocationOnMock invocation) {
				if (count-- > 0)
					return false;

				return true;
			}
		});

		Sender sender = new Sender(config, conn, queue);
		sender.run();

		verify(conn, times(3)).sendMessage(anyString());
	}

	private Event createEvent() {
		Request request = mock(Request.class);
		Response response = mock(Response.class);

		when(request.getRemoteHost()).thenReturn("127.0.0.1");
		when(request.getMethod()).thenReturn("GET");
		when(request.getRequestURI()).thenReturn("/");
		when(request.getRemoteUser()).thenReturn(null);
		when(request.getRequestedSessionId()).thenReturn(null);
		when(request.getHeader("User-Agent")).thenReturn("testClient");

		when(response.getStatus()).thenReturn(200);
		when(response.getBufferSize()).thenReturn(123);

		return new Event(request, response, 5);
	}

	private Target mockTarget(int eventsPerMessage) {
		Target target = mock(Target.class);
		when(target.getEventsPerMessage()).thenReturn(eventsPerMessage);
		when(target.getMessage(any(Configuration.class), any(Event.class))).thenReturn(MESSAGE);
		return target;
	}

}
