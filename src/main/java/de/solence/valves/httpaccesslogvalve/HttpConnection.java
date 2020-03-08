package de.solence.valves.httpaccesslogvalve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.catalina.AccessLog;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Handles the HTTP(S) connection to the logging endpoint.
 * 
 * @author Robin Seggelmann
 *
 */
public class HttpConnection {
	private static final Log log = LogFactory.getLog(AccessLog.class);
	private Configuration config;
	private Target target;

	public HttpConnection(Configuration config) {
		this.config = config;
		this.target = config.getTarget();
	}

	public boolean sendMessage(String message) {
		try {
			HttpURLConnection conn = (HttpURLConnection) config.getEndpointUrl().openConnection();

			// Connection properties
			conn.setConnectTimeout(config.getTimeout());
			conn.setReadTimeout(config.getTimeout());
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
			log.error(e.getMessage(), e);
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
