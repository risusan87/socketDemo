import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

public class SocketClient {
    public static void main(String[] args) throws UnknownHostException, IOException {
        
        connect();
        
    }

    public static Socket connect() throws UnknownHostException, IOException {
        Socket client = new Socket("10.0.0.125", 1234);
        return client;
    }

}
