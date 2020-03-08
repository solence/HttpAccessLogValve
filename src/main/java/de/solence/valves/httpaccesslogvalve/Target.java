package de.solence.valves.httpaccesslogvalve;

/**
 * Interface to support different logging systems as targets. Allows to
 * implement different message formats and authentication methods, depending on
 * the requirements of the endpoint of a logging system.
 * 
 * @author Robin Seggelmann
 *
 */
public interface Target {

	/**
	 * The content type used for an event message in the HTTP header.
	 * 
	 * @return The content type string.
	 */
	public String getContentType();

	/**
	 * The content of the Authentciation header to be used in the HTTP header.
	 * 
	 * @param token The authentication token to include.
	 * @return The authentication string
	 */
	public String getAuthenticationHeader(String token);

	/**
	 * Returns the maximum number of events that can be bundled into a single
	 * message.
	 * 
	 * @return The number of events per message.
	 */
	public int getEventsPerMessage();

	/**
	 * Creates a message from an event with the format expected by the logging
	 * system.
	 * 
	 * @param event The {@link Event} to send.
	 * @return A string with a compatible message for the endpoint.
	 */
	public String getMessage(Event event);

	/**
	 * Check if the response of the logging system indicates successful delivery of
	 * a message.
	 * 
	 * @param status  The returned HTTP status.
	 * @param content The returned content as a string.
	 * @return True if message delivery was successful.
	 */
	public boolean isResponseOk(int status, String content);

}
