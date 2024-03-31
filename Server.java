package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Scanner;

public class Server implements Runnable {

	//private final static int SERVER_REGISTRATION_PORT = 1028;
    private int serverRegistrationPort;
        

	private Hashtable<String, WorkerHandler> workerHandler;
	private Hashtable<String, ClientHandler> clientHandler;
	private Hashtable<String, Integer> workerLoads;
	private Hashtable<Integer, Integer> persistances;

	//private String id;
	// quand on aura recu 50 résultats on va sauvegardé sur le disque (pour ne pas
	// faire des écritures tt le temps)
	private final static int NB_RESULT_BEFORE_SAVE = 50;
	// un compteur pour savoir à combien de résultat on est à l'instant t
	private int saveCp;

	private final static String PATH_FILE_NAME = "persistance.txt";

	public Server(int port) {
		workerHandler = new Hashtable<>();
		clientHandler = new Hashtable<>();
		workerLoads = new Hashtable<>();
		persistances = new Hashtable<>();
                this.serverRegistrationPort=port;
		this.saveCp = 0;
		//this.id = UUID.randomUUID().toString();
	}

	private void handleIncommingConnection(Socket socket) {
		try {
			System.out.println("Assigning new thread for this connections");
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
			try {
				ThreadHandler handler = null;
				Object connection = input.readObject();
				if (connection instanceof Worker) {
					Worker worker = (Worker) connection;
					handler = new WorkerHandler(this, worker.getNbAvailableTask(), input, output);
					System.out.println("New worker added " + handler.getId() + "( " + worker.getNbAvailableTask()
							+ " taches disponibles )");
					workerHandler.put(handler.getId(), (WorkerHandler) handler);
					workerLoads.put(handler.getId(), 0);
				} else if (connection instanceof Client) {
					handler = new ClientHandler(this, input, output);
					System.out.println("New client added " + handler.getId());
					clientHandler.put(handler.getId(), (ClientHandler) handler);
				}
				if (handler != null) {
					Thread thread = new Thread(handler);
                                        //lancement du thread
					thread.start();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			System.out.println("An error has occured while trying to register worker");
		}

	}

	public void start() {
		Thread serverThread = new Thread(this);
		serverThread.start();
	}

	public int getWorkerNumber() {
		return workerHandler.size();
	}

	@Override
	public void run() {
		System.out.println("Starting server");
		if (loadDataFromFile()) {
			try (ServerSocket serverSocket = new ServerSocket(serverRegistrationPort)) {
				while (!Thread.interrupted()) {
					System.out.println(
							"Server is now listening for incoming connections on port: " + serverRegistrationPort);
					Socket socket = serverSocket.accept();
					System.out.println("New connection");
					handleIncommingConnection(socket);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void exploreNumber() {
		for (int i = 0; i < 1000; ++i) {
			executeCommand(new Command("persistance", String.valueOf(i)));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean loadDataFromFile() {
		System.out.println("Lecture depuis le fichier stocké sur le disque");
		File file = new File(PATH_FILE_NAME);
                try {
                if (!file.exists()){
                    file.createNewFile();
                }
		Scanner scnr;
		
			scnr = new Scanner(file);
			// Reading each line of the file using Scanner class
			while (scnr.hasNextLine()) {
				String line = scnr.nextLine();
				// System.out.println("line " + lineNumber + " :" + line);
				// on récupére le nombre (partie avant les :)
				int number = Integer.valueOf(line.split(":")[0]);
				// on récupére la persistance (partie après :)
				int persistance = Integer.valueOf(line.split(":")[1]);
				persistances.put(number, persistance);
			}
			System.out.println(persistances.size() + " lignes ont été chargées depuis le fichier");
			return true;
		} catch (Exception e) {
			System.out.println("Le fichier n'a pu être ni lu ni créé, le programme va donc s'arréter");
			return false;
		}

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = new Server(1028);
		server.start();
                //pour calculer les persistances des nombres jusqu'a 1000
               /* for (int i = 0; i < 1000; ++i) {
			server.executeCommand(new Command("persistance", String.valueOf(i)));
                        //appel de la fonction sleep pour patienter 100 milisecondes sinon c'est trés rapide (permet de visualiser le résultat au niveau de la console)
			Thread.sleep(100);
		}*/
	}
//trouver le worker avec moins de taches
	private WorkerHandler getLightestWOrker() {
		String worker = null;
		int previousLoad = 0;
		for (String workerId : workerLoads.keySet()) {
			int load = getWorkerLoad(workerHandler.get(workerId).getNbThread(), workerLoads.get(workerId));
			if (load > previousLoad) {
				previousLoad = load;
				worker = workerId;
			}

		}
		if (worker == null || previousLoad == 0) {
			// no free worker
			return null;
		}
		return workerHandler.get(worker);
	}

	private int getWorkerLoad(int availableWorkerThread, int activThread) {
		return availableWorkerThread - activThread;
	}

	public void executeCommand(Command command) {
		try {
			if (getLightestWOrker() == null) {
				System.out.println("no worker available");
				// on attend qu'un worker soit dispo
				while (getLightestWOrker() == null) {
					try {
						// pour ne pas attendre à l'infinie qu'un thread soit dispo
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else {
				getLightestWOrker().sendCommand(command);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getServerStatus() {
		String status = workerHandler.size() + " Worker actif";
		for (WorkerHandler worker : workerHandler.values()) {
			status += "\n worker " + worker.getId() + " " + worker.getNbThread() + " taches possibles en paralléle  "
					+ workerLoads.get(worker.getId()) + " taches en cours d'exécution";
		}
		return status;
	}
//methode pour gérer les commandes
	public void handleCommand(Command command) {
		try {
			
			// mettre à jour la charge d'un worker
			if (command.getCommand().equals("update")) {
				int load = Integer.valueOf(command.getParameter());
				System.out.println("Update load of worker: " + command.getInvokerId() + " " + load);
				workerLoads.put(command.getInvokerId(), load);
			} else if (command.getCommand().equals("status")) {
				String status = getServerStatus();
				command.setResult(status);
				clientHandler.get(command.getInvokerId()).sendCommand(command);
			} else if (command.getCommand().equals("average")) {
				String status = String.valueOf(MathUtils.getAverage(persistances));
				command.setResult(status);
				clientHandler.get(command.getInvokerId()).sendCommand(command);
			} else if (command.getCommand().equals("occurence")) {
				String status = String.valueOf(MathUtils.nbOccOfEachPersistance(persistances));
				command.setResult(status);
				clientHandler.get(command.getInvokerId()).sendCommand(command);
			} else if (command.getCommand().equals("mediane")) {
				String status = String.valueOf(MathUtils.getMediane(persistances));
				command.setResult(status);
				clientHandler.get(command.getInvokerId()).sendCommand(command);
			}
			else if (command.getCommand().equals("persistance")) {
				if (command.getResult() == null) {
					// on verifie si on a pas déja calculé la persistance
					String param = command.getParameter();
                                        //si on donne un intervalle en entrée
					if (param.contains(":")) {
						int from = Integer.valueOf(param.split(":")[0]);
						int to = Integer.valueOf(param.split(":")[1]);
						boolean allPersistanceHasBeenComputed = true;
						String result = "";
						for (int i = from; i <= to; i++) {
							if (!persistances.containsKey(i)) {
                                                            //ils ont pas été calculé
								allPersistanceHasBeenComputed = false;
                                                                //on sort de la boucle
								break;
							} else {
								result += i + ":" + persistances.get(i) + ";";
							}
						}
						//send back ezsult to client
						if (allPersistanceHasBeenComputed) {
							command.setResult(result);
							command.setExecuted();
							String invokerId = command.getInvokerId();
							if (invokerId != null && clientHandler.containsKey(invokerId)) {
								// send command back to client with the result
								clientHandler.get(command.getInvokerId()).sendCommand(command);
							}
						} else {
							executeCommand(command);
						}
						// result = String.valueOf(MathUtils.computePersistance(from, to));
					} else {
						if (persistances.containsKey(Integer.valueOf(param))) {
							command.setResult(String.valueOf(persistances.get(Integer.valueOf(param))));
							command.setExecuted();
							String invokerId = command.getInvokerId();
							if (invokerId != null && clientHandler.containsKey(invokerId)) {
								// send command back to client with the result
								clientHandler.get(command.getInvokerId()).sendCommand(command);
							}
						} else {
							executeCommand(command);
						}
					}
				} else {
					String number = command.getParameter();
					String persistance = command.getResult();
					handlePersistantResult(number, persistance);
					String invokerId = command.getInvokerId();
					if (invokerId != null && clientHandler.containsKey(invokerId)) {
						// send command back to client with the result
						clientHandler.get(command.getInvokerId()).sendCommand(command);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handlePersistantResult(String number, String persistance) {
		// si persistance contiens : alors c'est une plage de nombre
		if (persistance.contains(":")) {
			for (String result : persistance.split(";")) {
				String numberToadd = result.split(":")[0];
				String persistanceToAdd = result.split(":")[1];
				persistances.put(Integer.valueOf(numberToadd), Integer.valueOf(persistanceToAdd));
                                this.saveCp++;

			}
                        //pas d'intervalle (un seul nombre)
		} else {
                    
			persistances.put(Integer.valueOf(number), Integer.valueOf(persistance));
                        this.saveCp++;
		}
                //on ajoute les résulats des persistances que quand on a calculé au moins 50
		if (saveCp >= NB_RESULT_BEFORE_SAVE) {
			saveResultToFile();
			saveCp = 0;
		}
	}

	private void saveResultToFile() {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(PATH_FILE_NAME))) {
			for (Integer number : persistances.keySet()) {
				bw.write(number + ":" + persistances.get(number));
				bw.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
