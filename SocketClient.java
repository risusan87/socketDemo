import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import net.NetworkManager;

public class SocketClient {
    public static void main(String[] args) throws Exception {
        
        NetworkManager.openServerBoundConnection("localhost", 1234);
        
    }

    public static Socket connect() throws UnknownHostException, IOException {
        Socket client = new Socket("10.0.0.125", 1234);
        return client;
    }

}
