package de.solence.valves;

import java.time.ZoneId;
import java.util.Locale;

/**
 * Implements {@link HttpAccessLogTarget} for Splunk. Provides an authentication
 * method and message format compatible with Splunk HTTP Event Collectors (HEC).
 * <p>
 * Splunk is a registered trademark of Splunk Inc.
 * 
 * @author Robin Seggelmann
 *
 */
public class HttpAccessLogSplunk implements HttpAccessLogTarget {

	/**
	 * Returns the content type, always use JSON.
	 */
	public String getContentType() {
		return "application/json";
	}

	/**
	 * Creates a Splunk authentication header.
	 */
	public String getAuthenticationHeader(String token) {
		return "Splunk " + token;
	}

	/**
	 * Creates a Splunk event from the event data. The format is a JSON message
	 * compatible with a Splunk HTTP Event Collector (HEC).
	 * 
	 * @return A JSON message with a Splunk event.
	 */
	public String getMessage(HttpAccessLogEvent event) {
		HttpAccessLogJSONBuilder json = new HttpAccessLogJSONBuilder();

		double epoch = event.getTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000.0;

		// metadata
		json.startObject(null);
		json.append("time", String.format(Locale.US, "%.3f", epoch));
		if (event.getIndex() != null) {
			json.append("index", event.getIndex());
		}
		json.append("host", event.getHost());
		json.append("source", event.getSource());
		json.append("sourcetype", "access");
		// begin event
		json.startObject("event");
		// actual data
		json.append("remoteHost", event.getRemoteHost());
		json.append("method", event.getRequestMethod());
		json.append("uri", event.getRequestUri());
		json.append("user", (event.getRemoteUser() != null) ? event.getRemoteUser() : "-");
		json.append("sessionId", (event.getSessionId() != null) ? event.getSessionId() : "-");
		json.append("userAgent", (event.getUserAgent() != null) ? event.getUserAgent() : "-");
		json.append("status", event.getStatus());
		json.append("bytes", event.getBytes());
		json.append("processingTime", event.getProcessingTime());
		json.endObject();
		json.endObject();

		return json.toString();
	}

	/**
	 * Check if the response from Splunk indicates successful delivery of the event
	 * message.
	 */
	public boolean isResponseOk(int status, String content) {
		// Splunk should return status 200 and "text":"Success"
		return (status == 200 && content != null && content.contains("Success"));
	}

}
