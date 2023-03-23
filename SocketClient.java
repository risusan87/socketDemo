import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import net.NetworkManager;

public class SocketClient {
    public static void main(String[] args) throws Exception {
        
        System.out.println("Connectiong...");
        Socket client = new Socket("localhost", 1234);
        BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
        for (char c : "hello".toCharArray())
            bos.write(c);
        bos.flush();
        client.close();
        
    }

    public static Socket connect() throws UnknownHostException, IOException {
        Socket client = new Socket("10.0.0.125", 1234);
        return client;
    }

}
