package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Serializable {

    //private static final int REMOTE_PORT = 1028;
    //private static final String REMOTE_HOST = "127.0.0.1";
    private int remotePort;
    private String remoteHost;

    public Client(String hostServeur, int portServeur) {
        this.remoteHost = hostServeur;
        this.remotePort = portServeur;
    }

    public void start() {
        connect();
    }

    public void connect() {
        System.out.println("Registering to :" + remotePort);
        try (Socket socket = new Socket(remoteHost, remotePort)) {

            // we send to the server this worker
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream.writeObject(this);
            // we start listening for incoming request
            boolean keepAlive = true;
            while (keepAlive) {
                displayClientCommands();
                try (Scanner scanner = new Scanner(System.in)) {
                    while (scanner.hasNext()) {
                        String inputCommand = scanner.nextLine();
                        if (inputCommand.equals("q")) {
                            System.out.println("Le client va être fermé");
                            keepAlive = false;
                            break;
                        } else if (inputCommand.equals("status")) {
                            outputStream.writeObject(new Command("status", null));
                            Command command = (Command) inputStream.readObject();
                            System.out.println(command.getResult());
                        } else if (inputCommand.equals("average")) {
                            outputStream.writeObject(new Command("average", null));
                            Command command = (Command) inputStream.readObject();
                            System.out.println(command.getResult());
                        } else if (inputCommand.equals("occurence")) {
                            outputStream.writeObject(new Command("occurence", null));
                            Command command = (Command) inputStream.readObject();
                            System.out.println(command.getResult());
                        } else if (inputCommand.equals("mediane")) {
                            outputStream.writeObject(new Command("mediane", null));
                            Command command = (Command) inputStream.readObject();
                            System.out.println(command.getResult());
                        } else if (inputCommand.startsWith("persistance:")) {
                            try {
                                String number = inputCommand.substring(inputCommand.indexOf(":") + 1);
                                outputStream.writeObject(new Command("persistance", number));
                                Command command = (Command) inputStream.readObject();
                                System.out.println(command.getResult());
                            } catch (Exception e) {
                                System.out.println("Incorrect value received");
                            }
                        }
                        displayClientCommands();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayClientCommands() {
        System.out.println("Get number persistance: persistance:<number>:?<number>");
        System.out.println("Get persistance average: average");
        System.out.println("Get persistance mediane: mediane");
        System.out.println("Get persistance occurence: occurence");
        System.out.println("Get Server Status: status");
        System.out.println("Quit: q");
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 1028);
        client.start();
    }
}
