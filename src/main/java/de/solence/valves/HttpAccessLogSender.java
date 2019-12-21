package de.solence.valves;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.apache.catalina.AccessLog;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Sends messages of log events to endpoint.
 * <p>
 * Tries to send multiple events at once to reduce overhead and retries in case
 * message delivery has not been successful. This guarantees that messages are
 * delivered even with temporary connection problems. The only way to lose log
 * events is if the endpoint is permanently unreachable or the application is
 * interrupted.
 * 
 * @author Robin Seggelmann
 *
 */
public class HttpAccessLogSender implements Runnable {
	private static final Log log = LogFactory.getLog(AccessLog.class);
	private static final int MAX_EVENTS_PER_MESSAGE = 25;
	private final HttpAccessLogConfiguration config;
	private final HttpAccessLogTarget target;
	private final BlockingQueue<HttpAccessLogEvent> queue;

	/**
	 * Constructor.
	 * 
	 * @param config The {@link HttpAccessLogConfiguration} for connection details.
	 * @param queue  The event queue to send messages from.
	 */
	public HttpAccessLogSender(HttpAccessLogConfiguration config, BlockingQueue<HttpAccessLogEvent> queue) {
		this.config = config;
		this.target = config.getTarget();
		this.queue = queue;
	}

	/**
	 * Perform sending while queue is not empty. Multiple log events are
	 * concatenated to a single message to avoid overhead. Tries to send a message
	 * until it has been delivered or the application is interrupted.
	 */
	@Override
	public void run() {
		while (!queue.isEmpty()) {
			// Concatenate multiple events for a message
			String message = concatenateEvents();
			System.out.println(message);

			// Try to send message
			int waitBeforeRetry = 1;

			// Never give up, unless interrupted
			while (!sendMessage(message) && !Thread.interrupted()) {
				// If message could not be sent, the endpoint is likely down, so
				// wait before retrying.
				try {
					TimeUnit.SECONDS.sleep(waitBeforeRetry);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					// Thread has been interrupted, so give up. This likely results in lost events,
					// but there is not much that can be done to avoid that.
					return;
				}

				// Increase wait time to avoid spamming an unvailable endpoint,
				// but don't wait more than a minute.
				if (waitBeforeRetry < 60) {
					waitBeforeRetry *= 2;
				} else {
					waitBeforeRetry = 60;
				}
			}
		}
	}

	private String concatenateEvents() {
		StringBuilder message = new StringBuilder();
		message.append('[');
		for (int i = 0; i < MAX_EVENTS_PER_MESSAGE; i++) {
			// Get an event from the queue. At least one should be in it,
			// otherwise we wouldn't have made it here. Wait up to 100 ms
			// for further events to avoid sending single event, like Nagle's
			// algorithm.
			HttpAccessLogEvent event = null;
			try {
				event = queue.poll(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				// Interrupt has been received so don't wait for more events and continue
				// sending the message.
			}

			// If no events are left, continue to sending.
			if (event == null) {
				break;
			}

			if (i > 0) {
				message.append(',');
			}

			message.append(target.getMessage(event));
		}
		message.append(']');

		return message.toString();
	}

	private boolean sendMessage(String message) {
		try {
			HttpURLConnection conn;

			// Allow HTTPS and HTTP
			if ("https".equals(config.getEndpointUrl().getProtocol())) {
				conn = (HttpsURLConnection) config.getEndpointUrl().openConnection();
			} else {
				conn = (HttpURLConnection) config.getEndpointUrl().openConnection();
			}

			// Connection properties
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			conn.setDoOutput(true);

			// Headers
			conn.setRequestProperty("Content-Type", target.getContentType());
			conn.setRequestProperty("Authorization", target.getAuthenticationHeader(config.getAuthToken()));
			conn.setRequestMethod("POST");

			// Send message
			try (OutputStream os = conn.getOutputStream()) {
				os.write(message.getBytes(StandardCharsets.UTF_8));
			}

			// Get response
			String response = null;
			try (InputStream is = conn.getInputStream()) {
				response = readInputStream(is);
			}

			return target.isResponseOk(conn.getResponseCode(), response);

		} catch (IOException e) {
			log.error(e.getMessage());
			return false;
		}
	}

	private String readInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString(StandardCharsets.UTF_8.name());
	}

}
