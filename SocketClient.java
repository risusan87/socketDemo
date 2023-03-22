import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import net.NetworkManager;

public class SocketClient {
    public static void main(String[] args) throws UnknownHostException, IOException {
        
        System.out.println("Connectiong...");
        NetworkManager net = NetworkManager.startSocketClient("174.114.126.229", 1234, 5000);
        if (net == null)
            return;
        
    }

    public static Socket connect() throws UnknownHostException, IOException {
        Socket client = new Socket("10.0.0.125", 1234);
        return client;
    }

}
