import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

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
