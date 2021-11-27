package com.company;

import java.util.Scanner;

public class UserHandler implements Runnable {
    private final Server server;
    private final User user;

    public UserHandler(Server server, User user) {
        this.server = server;
        this.user = user;
        this.server.broadcastAllUsers();
    }

    public void run() {
        String message;

        Scanner sc = new Scanner(this.user.getInputStream());
        while (sc.hasNextLine()) {
            message = sc.nextLine();


            if (message.charAt(0) == '@') {
                if (message.contains(" ")) {
                    System.out.println("private msg : " + message);
                    int firstSpace = message.indexOf(" ");
                    String userPrivate = message.substring(1, firstSpace);
                    server.sendMessageToUser(
                            message.substring(
                                    firstSpace + 1
                            ), user, userPrivate
                    );
                }

            } else if (message.charAt(0) == '#') {
                user.changeColor(message);
                this.server.broadcastAllUsers();
            } else {
                server.broadcastMessages(message, user);
            }
        }
        server.removeUser(user);
        this.server.broadcastAllUsers();
        sc.close();
    }
}
