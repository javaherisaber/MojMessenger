import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Mahdi on 4/10/2017.
 * This program makes a ServerSocket and waits for client requests
 */
class TCPServer {
    
    String[] adviceList = {"Take smaller bites", "Go for the tight jeans. No they do NOT " +
            "make you look fat.", "One word: inappropriate", "Just for today, be honest. Tell your " +
            "boss what you *really* think", "You might want to rethink that haircut."};

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        server.go();
    }

    void go() {
        try {
            ServerSocket serverSock = new ServerSocket(12000);
            while(true) {
                Socket sock = serverSock.accept();
                PrintWriter writer = new PrintWriter(sock.getOutputStream());
                String advice = getAdvice();
                writer.println(advice);
                writer.close();
                System.out.println(advice);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getAdvice() {
        int random = (int) (Math.random() * adviceList.length);
        return adviceList[random];
    }
    
}
