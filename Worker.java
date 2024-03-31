package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

public class Worker implements Serializable, Runnable {

    private static final long serialVersionUID = 1L;
    //private static final int REMOTE_PORT = 1028;
    //private static final String REMOTE_HOST = "127.0.0.1";
    private int remotePort;
    private String remoteHost;

    private int nbAvailableExecutor;
    private int nbTaskInExecution = 0;
    //transient because ObjectOutputStream is not serializable
    //tu ne peux pas reconstruire un stream que tu es entrain d'utiliser

    private transient ObjectOutputStream outputStream;

    // nbAvailableTask nombre de tache que le worker peut executer en parrallele
    public Worker(String hostServeur, int portServeur) {
        //le nombre de coeur de la machine sur laquelle on exécute
        this.nbAvailableExecutor = Runtime.getRuntime().availableProcessors();
        //System.out.println(" le worker aura " + nbAvailableExecutor + " à sa disposition ");
        this.remoteHost = hostServeur;
        this.remotePort = portServeur;
    }

    private void connectToServer() throws UnknownHostException, IOException {
        System.out.println("Registering to :" + remotePort);
        try (Socket socket = new Socket(remoteHost, remotePort)) {

            // we send to the server this worker
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(this);
            // we start listening for incoming request
            boolean keepAlive = true;
            while (keepAlive) {
                Command command = (Command) inputStream.readObject();
                System.out.println("new command received");
                handleCommand(command, outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(Command command, ObjectOutputStream outputStream) {
        if (nbTaskInExecution < nbAvailableExecutor) {
            setTaskStart();
            Thread thread = new Thread(new WorkerTask(this, command, outputStream));
            thread.start();
        } else {
            System.out.println("no more free thread");
            // no more thread available
        }
    }

    public static void main(String[] args) {
        int nbWorker = 1;
        for (int i = 0; i < nbWorker; ++i) {
            Worker worker = new Worker("localhost", 1028);
            Thread thread = new Thread(worker);
            thread.start();
        }
    }

    public void start() {
        Thread workerThread = new Thread(this);
        workerThread.start();
    }

    private void startWorker() {
        System.out.println("Starting woker ");
        try {
            connectToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        startWorker();
    }

    public int getNbAvailableTask() {
        return nbAvailableExecutor;
    }

    public synchronized void setTaskDone() {
        nbTaskInExecution--;
        System.out.println("task ended");
        try {
            outputStream.writeObject(new Command("update", String.valueOf(nbTaskInExecution)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void setTaskStart() {
        nbTaskInExecution++;
        System.out.println("starting new task");
        try {
            outputStream.writeObject(new Command("update", String.valueOf(nbTaskInExecution)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
