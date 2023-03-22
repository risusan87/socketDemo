package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class NetworkManager {

    public final boolean isServer;
    private ServerSocket server;
    private Socket client;

    private NetworkManager(ServerSocket server) {
        this.server = server;
        this.isServer = true;
        this.client = null;
    }

    private NetworkManager(Socket client) {
        this.client = client;
        this.server = null;
        this.isServer = false;
    }

    /**
     * 
     * @param port
     * @return
     * @see ServerSocket
     * @throws IOException  When I/O error occured
     * @throws IllegalArgumentException  if the port is not in between 0 - 65535
     */
    public static NetworkManager startSocketServer(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);
        return new NetworkManager(server);
    }

    /**
     * 
     * @param ipv4
     * @param port
     * @return
     * @throws IOException
     * @throws UnknownHostException
     */
    public static NetworkManager startSocketClient(String ipv4, int port, int timeoutMillis) throws UnknownHostException, IOException {
        Socket client = new Socket();
        try {
            client.connect(new InetSocketAddress(ipv4, port), timeoutMillis);
        } catch (SocketTimeoutException e) {
            System.out.println("Connection timeout.");
            client.close();
            return null;
        }
        return new NetworkManager(client);
    }

}
