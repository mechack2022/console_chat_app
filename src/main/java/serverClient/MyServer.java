package serverClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServer implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    ServerSocket serverSocket;
    private boolean done;
    ExecutorService pool;

    public MyServer() {
        connections = new ArrayList<>();
        done = false;

    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler); // this is going to run the run function anytime we hve a new connection;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (Exception e) {
            shutdown(); // shutdown no matter which exception is thrown
        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        private BufferedReader reader;
        private PrintWriter writer;
        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                writer = new PrintWriter(client.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer.println("Please enter a nickname : ");
                nickname = reader.readLine();
                System.out.println(nickname + " connected!");
                broadcast(nickname + "  joined the chat!");
                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("/nick")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(messageSplit[1] + " renamed themselves to " + messageSplit[1]);
                            System.out.println(messageSplit[1] + " renamed themselves to " + message);
                            nickname = messageSplit[1];
                            writer.println("successfully changed nickname to " + nickname);
                        } else {
                            writer.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickname + " left the chat!");
                        shutdown();
                    } else {
                        broadcast(nickname + " : " + message);
                    }
                }

            } catch (Exception e) {
                //TODO handle
            }
        }

        //        to send message to the client via the handler
        public void sendMessage(String message) {
            writer.println(message);
        }

        public void shutdown() {
            try {
                writer.close();
                reader.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }

    }

    public static void main(String[] args) {
        MyServer myServer = new MyServer();
        myServer.run();
    }
}
