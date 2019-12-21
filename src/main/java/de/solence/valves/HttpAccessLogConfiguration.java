package de.solence.valves;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.catalina.AccessLog;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Reads and stores the configuration from JVM parameters or environment
 * variables. Any configuration value can be provided either way, but JVM
 * parameters are evaluated first.
 * <p>
 * The following are the configurable values:
 * <p>
 * <ul>
 * <li>endpointUrl - The HTTP endpoint to transmit the data to.
 * <li>authToken - The token used to authenticate against the endpoint.
 * <li>host - The name of the logging host, defaults to local hostname.
 * <li>index - The index to log to, optional.
 * <li>source - The name of the logging source, defaults to
 * "HttpAccessLogValve".
 * <li>queueLength - The length of the message queue, defaults to 1000.
 * <li>timeout - The time to wait for events to be sent after initiating
 * shutdown, default to 1 minute.
 * </ul>
 * 
 * @author Robin Seggelmann
 *
 */
public class HttpAccessLogConfiguration {
	private static final Log log = LogFactory.getLog(AccessLog.class);
	private final HttpAccessLogTarget target;
	private final URL endpointUrl;
	private final String authToken;
	private final String host;
	private final String index;
	private final String source;
	private final int queueLength;
	private final int timeout;

	/**
	 * Constructor.
	 * <p>
	 * Processes the provided configuration.
	 * 
	 * @throws LifecycleException
	 *                                Thrown for invalid or missing
	 *                                configuration values.
	 */
	public HttpAccessLogConfiguration() throws LifecycleException {
		// Currently only Splunk is supported
		target = new HttpAccessLogSplunk();

		// Read configured endpoint URL and store it as an URL object. This
		// fails in case a malformed URL is provided.
		try {
			endpointUrl = new URL(getJvmOrEnvValue("url", null, true));
		} catch (MalformedURLException e) {
			throw new LifecycleException(e);
		}

		// Check if protocol is either HTTP or HTTPS
		if (!"http".equals(endpointUrl.getProtocol())
				&& !"https".equals(endpointUrl.getProtocol())) {
			throw new LifecycleException(new IllegalStateException("Protocol "
					+ endpointUrl.getProtocol() + " not supported"));
		}

		// Get default value for host, either local hostname or if that fails,
		// default string
		// If no host has been configured, fall back to local host name
		String hostDefaultString;
		try {
			hostDefaultString = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.warn(e.getMessage(), e);
			hostDefaultString = "UnknownHost";
		}

		authToken = getJvmOrEnvValue("token", null, true);

		host = getJvmOrEnvValue("host", hostDefaultString, false);

		index = getJvmOrEnvValue("index", null, false);

		source = getJvmOrEnvValue("source", "HttpAccessLogValve", false);

		queueLength = Integer
				.parseUnsignedInt(getJvmOrEnvValue("queue", "1000", false));
		timeout = Integer
				.parseUnsignedInt(getJvmOrEnvValue("timeout", "30", false));
	}

	private String getJvmOrEnvValue(String name, String defaultValue,
			boolean mandatory) throws LifecycleException {

		String jvmName = "httpaccesslogvalve." + name.toLowerCase();
		String envName = "HTTPACCESSLOGVALVE_" + name.toUpperCase();

		// Try to read JVM parameter
		if (System.getProperties().containsKey(jvmName)
				&& !System.getProperty(jvmName).isEmpty()) {
			return System.getProperty(jvmName);
		}

		// Try to read environment parameter
		if (System.getenv().containsKey(envName)
				&& !System.getenv(envName).isEmpty()) {
			return System.getenv(envName);
		}

		if (mandatory) {
			throw new LifecycleException(new IllegalStateException(
					"Cannot continue without either JVM parameter " + jvmName
							+ " or environment variable " + envName
							+ " configured"));
		}

		return defaultValue;
	}

	/**
	 * Returns the implementation object of the target logging system. Allows to
	 * support different message formats and authentication methods for
	 * different systems.
	 * 
	 * @return The implementation of the target system.
	 */
	public HttpAccessLogTarget getTarget() {
		return target;
	}

	/**
	 * Returns the URL of the HTTP endpoint.
	 * <p>
	 * Must be configured with JVM parameter <code>httpaccesslogvalve.url</code>
	 * or environment variable <code>HTTPACCESSLOGVALVE_URL</code>.
	 * 
	 * @return The URL of the HTTP endpoint.
	 */
	public URL getEndpointUrl() {
		return endpointUrl;
	}

	/**
	 * Returns the token used to authenticate against the HTTP endpoint.
	 * <p>
	 * Must be configured with JVM parameter
	 * <code>httpaccesslogvalve.token</code> or environment variable
	 * <code>HTTPACCESSLOGVALVE_TOKEN</code>.
	 * 
	 * @return The token to authenticate with.
	 */
	public String getAuthToken() {
		return authToken;
	}

	/**
	 * Returns the name of the logging host.
	 * <p>
	 * Can be configured with JVM parameter <code>httpaccesslogvalve.host</code>
	 * or environment variable <code>HTTPACCESSLOGVALVE_TOKEN</code>. If no
	 * value is provided, it defaults to local hostname.
	 * 
	 * @return The name of the logging host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the index to log to.
	 * <p>
	 * Can be configured with JVM parameter
	 * <code>httpaccesslogvalve.index</code> or environment variable
	 * <code>HTTPACCESSLOGVALVE_INDEX</code>. If no value is provided, this
	 * value is <code>null</code> and the default index will be used.
	 * 
	 * @return The name of the logging host.
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * Returns the name of the source of the logged data.
	 * <p>
	 * Can be configured with JVM parameter
	 * <code>httpaccesslogvalve.source</code> or environment variable
	 * <code>HTTPACCESSLOGVALVE_SOURCE</code>. If no value is provided, it
	 * defaults to "HttpAccessLogValve".
	 * 
	 * @return The name of the logging host.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Returns the length of the queue. A longer queue reduces the probability
	 * of discarded messages, but consumes more memory.
	 * <p>
	 * Can be configured with JVM parameter
	 * <code>httpaccesslogvalve.queue</code> or environment variable
	 * <code>HTTPACCESSLOGVALVE_QUEUE</code>. If no value is provided, it
	 * defaults to 1000.
	 * 
	 * @return The name of the logging host.
	 */
	public int getQueueLength() {
		return queueLength;
	}

	/**
	 * Returns the time to wait for outstanding events to be sent after a
	 * shutdown has been initiated, in seconds.
	 * <p>
	 * Can be configured with JVM parameter
	 * <code>httpaccesslogvalve.timeout</code> or environment variable
	 * <code>HTTPACCESSLOGVALVE_TIMEOUT</code>. If no value is provided, it
	 * defaults to 30 seconds.
	 * 
	 * @return The name of the logging host.
	 */
	public int getTimeout() {
		return timeout;
	}

}
