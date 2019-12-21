package de.solence.valves;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.apache.catalina.AccessLog;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * 
 * 
 * @author Robin Seggelmann
 *
 */
public class HttpAccessLogValve extends ValveBase implements AccessLog {
	private static final Log log = LogFactory.getLog(AccessLog.class);
	private ArrayBlockingQueue<HttpAccessLogEvent> queue;
	private ScheduledExecutorService executor;
	private HttpAccessLogConfiguration config;

	/**
	 * Constructor.
	 */
	public HttpAccessLogValve() {
		super(true);
	}

	/**
	 * Protected constructor to inject configuration for unit tests.
	 * 
	 * @param config
	 *                   The {@link HttpAccessLogConfiguration} to inject.
	 */
	protected HttpAccessLogValve(HttpAccessLogConfiguration config) {
		this.config = config;
	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		log.info("Starting");

		setState(LifecycleState.STARTING);

		if (config == null) {
			config = new HttpAccessLogConfiguration();
		}

		log.info("URL: " + config.getEndpointUrl());
		log.info("Host: " + config.getHost());
		log.info("Source: " + config.getSource());

		queue = new ArrayBlockingQueue<>(config.getQueueLength());

		executor = Executors.newSingleThreadScheduledExecutor();

		// Check every 250 ms for new events to send
		executor.scheduleWithFixedDelay(new HttpAccessLogSender(config, queue),
				250L, 250L, TimeUnit.MILLISECONDS);
	}

	@Override
	protected synchronized void stopInternal() throws LifecycleException {
		log.info("Stopping");

		setState(LifecycleState.STOPPING);

		// Shutdown and wait for termination, that is sending events still in
		// the queue.
		if (executor != null) {
			executor.shutdown();
			try {
				executor.awaitTermination(config.getTimeout(),
						TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void log(Request request, Response response, long time) {
		try {
			queue.add(new HttpAccessLogEvent(config, request, response, time));
		} catch (IllegalStateException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void invoke(Request request, Response response)
			throws IOException, ServletException {

		Valve nextValve = getNext();
		if (nextValve != null) {
			nextValve.invoke(request, response);
		}
	}

	@Override
	public void setRequestAttributesEnabled(boolean requestAttributesEnabled) {
		// not supported
	}

	@Override
	public boolean getRequestAttributesEnabled() {
		return false;
	}

}
