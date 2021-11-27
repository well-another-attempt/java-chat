package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private final int port;
    private final List<User> clients;

    public static void main(String[] args) throws IOException {
        new Server(Integer.parseInt(args[0])).run();
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
    }

    public void run() throws IOException {
        ServerSocket server = new ServerSocket(port) {
            protected void finalize() throws IOException {
                this.close();
            }
        };
        System.out.println("Port " + port + " is now open.");

        while (true) {
            Socket client = server.accept();

            String nickname = (new Scanner(client.getInputStream())).nextLine();
            nickname = nickname.replace(",", "");
            nickname = nickname.replace(" ", "_");
            System.out.println("New Client: \"" + nickname + "\"\n\t     Host:" + client.getInetAddress().getHostAddress());

            User newUser = new User(client, nickname);

            this.clients.add(newUser);

            new Thread(new UserHandler(this, newUser)).start();
        }
    }

    public void removeUser(User user) {
        this.clients.remove(user);
    }

    public void broadcastMessages(String msg, User userSender) {
        for (User client : this.clients) {
            client.getOutStream().println(
                    userSender.toString() + "<span>: " + msg + "</span>");
        }
    }

    public void broadcastAllUsers() {
        for (User client : this.clients) {
            client.getOutStream().println(this.clients);
        }
    }

    public void sendMessageToUser(String msg, User userSender, String user) {
        boolean find = false;
        for (User client : this.clients) {
            if (client.getNickname().equals(user) && client != userSender) {
                find = true;
                userSender.getOutStream().println(userSender + " -> " + client + ": " + msg);
                client.getOutStream().println(
                        "(<b>Private</b>)" + userSender + "<span>: " + msg + "</span>");
            }
        }
        if (!find) {
            userSender.getOutStream().println(userSender + " -> (<b>no one!</b>): " + msg);
        }
    }
}

