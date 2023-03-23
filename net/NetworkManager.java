package net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import net.packet.PacketBase;

public abstract class NetworkManager {

    protected final Socket socketConnection;
    

    private NetworkManager(Socket connection) {
        this.socketConnection = connection;
    }

    protected abstract void onPacketReceived(PacketBase incomingPacket);

    /**
     * Exception thrown when there was unexpected packet within connections.
     */
    public static class PacketMalformedException extends Exception {
        public PacketMalformedException(String msg) {
            super(msg);
        }
        public PacketMalformedException() {
            super();
        }
    }

    /**
     * Enhenced implementation of ServerSocket class for this protocol.
     * 
     * @author RK
     */
    public static class ServerConnection extends ServerSocket {

        private final ThreadPoolExecutor executor;

        private ServerConnection(int port, int maxClient) throws IOException {

            super(port);
            this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxClient);

            Thread entrance = new Thread(() -> {
                boolean close = false;
                while (!close) {
                    try {   
                        this.acceptClientConnection();
                    } catch (IOException e) {
                        close = true;
                    }
                }
            });
            entrance.start();

        }

        public void acceptClientConnection() throws IOException {
            final NetworkManager clientBoundConnection = new NetworkManager(this.accept()) {
                @Override
                protected void onPacketReceived(PacketBase incomingPacket) {

                }
            };
            System.out.println("current clients: " + executor.getPoolSize());
            ServerConnection.this.executor.submit(() -> {
                try (
                    BufferedInputStream bis = new BufferedInputStream(
                        clientBoundConnection.socketConnection.getInputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(
                        clientBoundConnection.socketConnection.getOutputStream());
                ) {
                    while (!ServerConnection.this.isClosed()) {
                        if (bis.available() != 0) {
                            byte[] readData = new byte[bis.available()];
                            bis.read(readData);
                            for (byte b : readData)
                                System.out.println(b);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        public void setClosed() {

        }

        public void setClosedAndWait() {

        }

    }

    /**
     * Starts a socket server via TCP connection hosted on this physical machine.
     * This method may return null on failure.
     * 
     * @param port
     * @return NetworkManager prepared for this protocol, or null if it fails to listen on the port by any chance.
     */
    public static ServerConnection startSocketServer(int port) {
        try {
            return new ServerConnection(port, 5);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
