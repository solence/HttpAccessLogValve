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

import de.solence.valves.httpaccesslogvalve.Configuration;
import de.solence.valves.httpaccesslogvalve.Event;
import de.solence.valves.httpaccesslogvalve.Sender;

/**
 * The main class of the HTTP Access Log Valve, implementing the necessary
 * interface to Tomcat.
 * 
 * @author Robin Seggelmann
 *
 */
public class HttpAccessLogValve extends ValveBase implements AccessLog {
	private static final Log log = LogFactory.getLog(AccessLog.class);
	private ArrayBlockingQueue<Event> queue;
	private ScheduledExecutorService executor;
	private Configuration config;

	/**
	 * Constructor.
	 */
	public HttpAccessLogValve() {
		super(true);
	}

	/**
	 * Protected constructor to inject configuration for unit tests.
	 * 
	 * @param config The {@link Configuration} to inject.
	 */
	protected HttpAccessLogValve(Configuration config) {
		this.config = config;
	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		log.info("Starting");

		setState(LifecycleState.STARTING);

		if (config == null) {
			config = new Configuration();
		}

		log.info("URL: " + config.getEndpointUrl());
		log.info("Host: " + config.getHost());
		log.info("Source: " + config.getSource());

		queue = new ArrayBlockingQueue<>(config.getQueueLength());

		executor = Executors.newSingleThreadScheduledExecutor();

		// Check every 250 ms for new events to send
		executor.scheduleWithFixedDelay(new Sender(config, queue), 250L, 250L, TimeUnit.MILLISECONDS);
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
				executor.awaitTermination(config.getShutdownTimeout(), TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void log(Request request, Response response, long time) {
		try {
			queue.add(new Event(config, request, response, time));
		} catch (IllegalStateException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {

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
