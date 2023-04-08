import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class Client {

    public Client(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println(
                    "Connected to server host: " + host +
                            " port: " + port + "\nPrint something to start work");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            Scanner sc = new Scanner(System.in);
            String line = null;
            while (!socket.isOutputShutdown()) {
                System.out.println("Print your message: ");
                line = sc.nextLine();

                System.out.println("Sending message to the server.. ");
                out.println(line);
                System.out.println(br.readLine());

                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Exit command entered, closing application");
                    System.exit(0);
                    sc.close();
                }
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}