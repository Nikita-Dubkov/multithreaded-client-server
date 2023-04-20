import java.util.Scanner;

public class Application {

    private static final byte SERVERMODE = 1;
    private static final byte CLIENTMODE = 2;
    private static final String adress = "127.0.0.1";
    private static final int port = 8080;

    public static void main(String[] args) {

        System.out.println("Please, select which application do you want to start: ");
        System.out.println("Print '1' - server");
        System.out.println("Print '2' - client");
        Scanner sc = new Scanner(System.in);
        byte mode = sc.nextByte();
        if (mode == SERVERMODE) {
            System.out.println("Creating SERVER application");
            new Server(port);
            sc.close();
        } else if (mode == CLIENTMODE) {
            System.out.println("Creating CLIENT application");
            new Client(adress, port);
            sc.close();
        } else {
            System.out.println("Wrong input, closing application");
            sc.close();
            System.exit(-1);
        }
    }

}
