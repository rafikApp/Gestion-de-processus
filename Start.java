/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.util.Scanner;

/**
 *
 * @author Samia
 */
public class Start {

    public static void main(String[] args) {
        System.out.println("Si vous voulez lancer un serveur, tapez 1");
        System.out.println("Si vous voulez lancer un worker, tapez 2");
        System.out.println("Si vous voulez lancer un client, tapez 3");

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                String inputCommand = scanner.nextLine();
                if (inputCommand.equals("1")) {
                    System.out.println("Vous voulez lancer le serveur sur quel port?");
                    int port = scanner.nextInt();
                    Server server = new Server(port);
                    server.start();

                }
                if (inputCommand.equals("2")) {
                    System.out.println("Quel est le host du serveur");
                    String hostServeur= scanner.next();
                    System.out.println("Quel est le port du serveur");
                    int portServeur=scanner.nextInt();
                    Worker worker= new Worker(hostServeur, portServeur);
                    worker.start();
                }
                if (inputCommand.equals("3")) {
                    System.out.println("Quel est le host du serveur");
                    String hostServeur= scanner.next();
                    System.out.println("Quel est le port du serveur");
                    int portServeur=scanner.nextInt();
                    Client client= new Client(hostServeur, portServeur);
                    client.start();
                }
                

            }

        }

    }
}
