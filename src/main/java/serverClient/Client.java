package serverClient;
import java.io.*;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("localhost", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler); // thread was instead of pool used here because we only have one thread
            thread.start();
            String inMessage;
            while((inMessage = in.readLine()) != null) {
                System.out.println(inMessage);
            }
        } catch (Exception e) {
            //TODO handle
        }
    }

    private void shutdown() {
        done = true;
        try {
            out.close();
            in.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    // this class will constantly read input from the reader
    public class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.startsWith("/quit")) {
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (Exception e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
