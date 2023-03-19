import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import javax.xml.bind.DatatypeConverter;

public class SocketServer {

    public static void main(String[] args) throws IOException {

        ServerSocket server = new ServerSocket(1234);
        System.out.println("Waiting connection");
        Socket clientConnection = server.accept();
        System.out.println("Connected");
        clientConnection.close();
        server.close();
        
    }

    
    private static byte[] genSharedSecret(int size) {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[size];
        random.nextBytes(secret);
        return secret;
    }

}