import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

import net.NetworkManager;

public class SocketServer {

    public static void main(String[] args) throws IOException {

        NetworkManager.ServerConnection server = NetworkManager.startSocketServer(1234);
        
    }

    
    private static byte[] genSharedSecret(int size) {
        SecureRandom random = new SecureRandom();
        byte[] secret = new byte[size];
        random.nextBytes(secret);
        return secret;
    }

}