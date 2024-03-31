package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

public class WorkerHandler extends ThreadHandler {

	private int nbThread;

	// Constructor
	public WorkerHandler(Server server, int nbThread, ObjectInputStream input, ObjectOutputStream output) {
		super(server, input, output);
		this.nbThread = nbThread;
	}

	public int getNbThread() {
		return nbThread;
	}
	
	@Override
	public void run() {
		// worker live loop
		// loop to listen incomming executed command from worker
		//ecouter les demandes 
		//pour maintenir la communication entre le worker et le server 
		boolean keepAlive = true;
		while (keepAlive) {
			try {
				//cet objet va rester bloquer (en ecoute)
				//on doit s'ocuuuper de plusieurs workers
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
