package de.solence.valves.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class TestSocket implements Runnable {
	private final int port;
	private final long timeout;

	public TestSocket(int port, long timeout) {
		this.port = port;
		this.timeout = timeout;
	}

	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			Socket clientSocket = serverSocket.accept();

			TimeUnit.MILLISECONDS.sleep(timeout);

			clientSocket.close();
			serverSocket.close();
		} catch (IOException | InterruptedException e) {
			// Nothing to do here
		}
	}

}
