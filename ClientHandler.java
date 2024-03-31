package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientHandler extends ThreadHandler {
	// Constructor
	public ClientHandler(Server server, ObjectInputStream input, ObjectOutputStream output) {
		super(server, input, output);
	}

	@Override
	public void run() {
		// loop to listen incomming command from client
		boolean keepAlive = true;
		while (keepAlive) {
			// wait for Worker input
			try {
				Object response = input.readObject();
				if (response instanceof Command) {
					Command command = (Command) response;
					if(command.getInvokerId() == null) {
						command.setInvokerId(getId());
					}
					server.handleCommand(command);
				}
			} catch (IOException e) {
				e.printStackTrace();
				keepAlive = false;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
