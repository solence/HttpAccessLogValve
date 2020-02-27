package de.solence.valves;

import java.time.LocalDateTime;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * Stores a logging event and creates an appropriate message format for the
 * target logging system. Currently, only Splunk is supported.
 * 
 * @author Robin Seggelmann
 *
 */
public class HttpAccessLogEvent {
	private final LocalDateTime time;
	private final String index;
	private final String host;
	private final String source;
	private final String remoteHost;
	private final String requestMethod;
	private final String requestUri;
	private final String remoteUser;
	private final String sessionId;
	private final String userAgent;
	private final int status;
	private final int bytes;
	private final long processingTime;

	/**
	 * Constructor.
	 * <p>
	 * Does nothing but storing the relevant data to minimize delay at runtime.
	 * 
	 * @param config
	 *                     The {@link HttpAccessLogConfiguration} providing
	 *                     configured data.
	 * @param request
	 *                     The incoming {@link Request} providing client data.
	 * @param response
	 *                     The outgoing {@link Response} providing server data.
	 */
	public HttpAccessLogEvent(HttpAccessLogConfiguration config,
			Request request, Response response, long processingTime) {
		this.time = LocalDateTime.now();
		this.index = config.getIndex();
		this.host = config.getHost();
		this.source = config.getSource();
		this.remoteHost = request.getRemoteHost();
		this.requestMethod = request.getMethod();
		this.requestUri = request.getRequestURI();
		this.remoteUser = request.getRemoteUser();
		this.sessionId = request.getRequestedSessionId();
		this.userAgent = request.getHeader("User-Agent");
		this.status = response.getStatus();
		this.bytes = response.getBufferSize();
		this.processingTime = processingTime;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public String getIndex() {
		return index;
	}

	public String getHost() {
		return host;
	}

	public String getSource() {
		return source;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public String getRequestUri() {
		return requestUri;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public int getStatus() {
		return status;
	}

	public int getBytes() {
		return bytes;
	}

	public long getProcessingTime() {
		return processingTime;
	}

}
