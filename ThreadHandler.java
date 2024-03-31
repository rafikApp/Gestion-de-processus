package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

//classe qui permet de generer une connection par un thread dedie
public abstract class ThreadHandler implements Runnable {

	protected final ObjectInputStream input;
	protected final ObjectOutputStream output;
	protected final Server server;
	protected final String id;

	// Constructor
	public ThreadHandler(Server server, ObjectInputStream input, ObjectOutputStream output) {
		this.input = input;
		this.output = output;
		this.server = server;
		this.id = UUID.randomUUID().toString();
	}

	// we send a command to connected client (can be a worker or client)
	//il renvoie 
	protected void sendCommand(Command command) throws IOException {
		output.writeObject(command);
	}
	
	public String getId() {
		return id;
	}

}
