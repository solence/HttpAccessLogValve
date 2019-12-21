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
		// Use a StringBuilder here instead of a generic JSON library. Only the
		// bare minimum JSON is needed, so this avoids dependencies and
		// minimizes processing time added by generic parsers.
		StringBuilder sb = new StringBuilder();

		sb.append('{');
		// metadata
		double epoch = event.getTime().atZone(ZoneId.systemDefault())
				.toInstant().toEpochMilli() / 1000.0;

		append(sb, "time", String.format(Locale.US, "%.3f", epoch));
		sb.append(',');
		if (event.getIndex() != null) {
			append(sb, "index", event.getIndex());
			sb.append(',');
		}
		append(sb, "host", event.getHost());
		sb.append(',');
		append(sb, "source", event.getSource());
		sb.append(',');
		append(sb, "sourcetype", "access");
		sb.append(',');
		// begin event
		sb.append('"').append("event").append('"');
		sb.append(':');
		sb.append('{');
		// actual data
		append(sb, "remoteHost", event.getRemoteHost());
		sb.append(',');
		append(sb, "method", event.getRequestMethod());
		sb.append(',');
		append(sb, "uri", event.getRequestUri());
		sb.append(',');
		if (event.getRemoteUser() != null) {
			append(sb, "user", event.getRemoteUser());
		} else {
			append(sb, "user", "-");
		}
		sb.append(',');
		if (event.getUserAgent() != null) {
			append(sb, "userAgent", event.getUserAgent());
		} else {
			append(sb, "userAgent", "-");
		}
		sb.append(',');
		append(sb, "status", event.getStatus());
		sb.append(',');
		append(sb, "bytes", event.getBytes());
		sb.append(',');
		append(sb, "processingTime", event.getProcessingTime());
		sb.append('}');
		sb.append('}');

		return sb.toString();
	}

	/**
	 * Check if the response from Splunk indicates successful delivery of the
	 * event message.
	 */
	public boolean isResponseOk(int status, String content) {
		// TODO: parse response
		System.out.println(status + ": " + content);
		return true;

	}

	private void append(StringBuilder sb, String key, String value) {
		sb.append('"').append(key).append('"');
		sb.append(':');
		sb.append('"').append(value).append('"');
	}

	private void append(StringBuilder sb, String key, long value) {
		sb.append('"').append(key).append('"');
		sb.append(':');
		sb.append(value);
	}

}
