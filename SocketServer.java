import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

import net.NetworkManager;

public class SocketServer {

    public static void main(String[] args) throws IOException {

        NetworkManager.Server server = NetworkManager.startSocketServer(1234);
        
    }

}