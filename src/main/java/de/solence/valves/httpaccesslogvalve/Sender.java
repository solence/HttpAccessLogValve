package de.solence.valves.httpaccesslogvalve;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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
public class Sender implements Runnable {
	private static final Log log = LogFactory.getLog(AccessLog.class);
	private final Configuration config;
	private final HttpConnection conn;
	private final BlockingQueue<Event> queue;

	/**
	 * Constructor.
	 * 
	 * @param config The {@link Configuration} for connection details.
	 * @param queue  The event queue to send messages from.
	 */
	public Sender(Configuration config, HttpConnection conn, BlockingQueue<Event> queue) {
		this.config = config;
		this.conn = conn;
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

			// Try to send message
			int waitBeforeRetry = 1;

			// Never give up, unless interrupted
			while (!conn.sendMessage(message) && !Thread.interrupted()) {
				// If message could not be sent, the endpoint is likely down, so
				// wait before retrying.
				try {
					TimeUnit.SECONDS.sleep(waitBeforeRetry);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					// Thread has been interrupted, so give up. This likely results in lost events,
					// but there is not much that can be done to avoid that.
					log.error("Received interrupt while still trying to send events");
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
		for (int i = 0; i < config.getTarget().getEventsPerMessage(); i++) {
			// Get an event from the queue. At least one should be in it,
			// otherwise we wouldn't have made it here. Wait up to 100 ms
			// for further events to avoid sending single event, like Nagle's
			// algorithm.
			Event event = null;
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

			message.append(config.getTarget().getMessage(config, event));
		}
		message.append(']');

		return message.toString();
	}

}
