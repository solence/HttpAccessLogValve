package de.solence.valves.httpaccesslogvalve;

import java.time.LocalDateTime;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * Stores a logging event.
 * 
 * @author Robin Seggelmann
 *
 */
public class Event {
	private final LocalDateTime time;
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
	 * @param request  The incoming {@link Request} providing client data.
	 * @param response The outgoing {@link Response} providing server data.
	 */
	public Event(Request request, Response response, long processingTime) {
		this.time = LocalDateTime.now();
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
