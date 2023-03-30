package net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.packet.P0Handshake;
import net.packet.P1Termination;
import net.packet.PacketBase;

/**
 * The core class represents the socket connection for both client and server.
 * It is in renponse of managing data flow over the network.
 * 
 */
public abstract class NetworkManager {

    protected final Socket socketConnection;
    protected SecretKey sharedSecret = null;
    private Thread timeout = null;
    protected int connectionId;
    
    private final BufferedInputStream input;
    private final BufferedOutputStream output;

    private static PrintStream outStream = null; 

    private NetworkManager(Socket socket) throws IOException {

        this.socketConnection = socket;

        this.input = new BufferedInputStream(this.socketConnection.getInputStream());
        this.output = new BufferedOutputStream(this.socketConnection.getOutputStream());
        
        this.connectionId = -1;

        new Thread(() -> {
            try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ) {
                while (true) {
                    int bytesRead = -1;
                    byte[] buff = new byte[128];
                    while ((bytesRead = this.input.read(buff)) != -1) {
                        if (this.timeout != null)
                            this.timeout.interrupt();
                        baos.write(buff, 0, bytesRead);
                        if (this.input.available() == 0)
                            break;
                    }
                    
                    if (baos.size() != 0) {
                        ByteBuffer buffer = ByteBuffer.wrap(baos.toByteArray());
                        try {
                            synchronized (this) {
                                // NetworkManager.logPrintf("[ID: %d]: Received %d byte(s) of data.\n", this.connectionId, buffer.capacity());
                                this.onDataReceived(buffer);
                            }
                        } catch (MalformedPacketException e) {
                            e.printStackTrace();
                            close();
                        }
                        baos.reset();
                    }
                }
            } catch (IOException e) {
            }
            finally {
                NetworkManager.logPrintf("[ID: %d]: [Network IO Pipeline]: Input pipeline terminated.\n", this.connectionId);
            } 
        }).start();

    }

    /**
     * Start a timer that if there were no I/O communications within this time, 
     * the connection forcefully gets closed.
     * 
     * @param timeoutMillis
     */
    public void waitForTimeout(int timeoutMillis) {
        this.timeout = new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis);
                // send termination packet
                NetworkManager.logPrintf("[ID:%d]: Connection timed out.\n", this.connectionId);
                close();
            } catch (InterruptedException e) {}
        });
        this.timeout.start();
    }

    public void close() {
        try {
            this.input.close();
            this.output.close();
            this.socketConnection.close();
            NetworkManager.logPrintf("[ID:%d]: Connection closed.\n", this.connectionId);
        } catch (IOException e) {
            e.printStackTrace();
        }   
    }

    protected ByteBuffer encryptData(ByteBuffer rawData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, this.sharedSecret);
            byte[] finalBytes = cipher.doFinal(rawData.array());
            return ByteBuffer.wrap(finalBytes);
        } catch ( NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected ByteBuffer decryptData(ByteBuffer secureData) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, this.sharedSecret);
            byte[] finalBytes = cipher.doFinal(secureData.array());
            return ByteBuffer.wrap(finalBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract void onDataReceived(ByteBuffer incomingData) throws MalformedPacketException;

    protected synchronized void sendData(ByteBuffer outgoingData) {
        try {
            this.output.write(outgoingData.array());
            this.output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exception thrown when there was unexpected packet within connections.
     */
    public static class MalformedPacketException extends Exception {
        public MalformedPacketException(String msg) {
            super(msg);
        }
        public MalformedPacketException() {
            super();
        }
    }

    /**
     * Enhanced implementation of ServerSocket class for this protocol.
     * This is the representation of the server that waits for client connections
     * to open socket connections.
     * 
     */
    public static class Server extends ServerSocket {

        private final Map<Integer, NetworkManager> clients;

        private Server(int port, int maxClient) throws IOException {

            super(port);
            this.clients = Collections.synchronizedMap(new HashMap<Integer, NetworkManager>());

            Thread entrance = new Thread(() -> {
                boolean close = false;
                while (!close) {
                    try {   
                        NetworkManager clientBoundConnection = this.acceptClientConnection();
                        synchronized (clients) {
                            clients.put(clientBoundConnection.connectionId, clientBoundConnection);
                        }
                    } catch (IOException e) {
                        close = true;
                    }
                }
            });
            entrance.start();

        }

        private synchronized void packetArrival(PacketBase packet) throws MalformedPacketException {
            
            if (packet instanceof P0Handshake) {
                P0Handshake handshake = (P0Handshake) packet;
                // Packet state other than 0 is not supposed to be a server bound.
                if (handshake.getPacketState() != 0)
                    throw new MalformedPacketException();

                // NetworkManager.logPrintf("[ID: %d]: Received encryption request.", packet.getConnectionId());    
                NetworkManager targetConnection = null;
                synchronized (this.clients) {
                    targetConnection = this.clients.get(handshake.getConnectionId());
                }
                // shared key gen
                KeyGenerator gen = null;
                try {
                    gen = KeyGenerator.getInstance("AES");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                gen.init(128);
                targetConnection.sharedSecret = gen.generateKey();
                ByteBuffer response = handshake.pCs1(targetConnection.sharedSecret.getEncoded());
                targetConnection.sendData(response);
                ByteBuffer s2response = handshake.pCs2();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                targetConnection.sendData(targetConnection.encryptData(s2response));
                // NetworkManager.logPrintf("[ID: %d]: Encryption response sent.\n", packet.getConnectionId());
            }

        }

        private NetworkManager acceptClientConnection() throws IOException {
            // this is the server side connection
            final NetworkManager clientBoundConnection = new NetworkManager(this.accept()) {

                {
                    this.connectionId = new SecureRandom().nextInt();
                }

                @Override
                protected void onDataReceived(ByteBuffer incomingData) throws MalformedPacketException {
                    if (this.sharedSecret == null) {
                        if (incomingData.get(0) != 0x00)
                            throw new MalformedPacketException();
                        P0Handshake handshake = new P0Handshake(incomingData, connectionId);
                        Server.this.packetArrival(handshake);
                        return;
                    }
                }

            };
            clientBoundConnection.waitForTimeout(3000);
            return clientBoundConnection;

        }

    }

    /**
     * Starts a socket server via TCP connection hosted on this physical machine.
     * This method may return null on failure.
     * 
     * @param port
     * @return NetworkManager prepared for this protocol, or null if it fails to listen on the port by any chance.
     */
    public static Server startSocketServer(int port) {
        try {
            return new Server(port, 5);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NetworkManager openServerBoundConnection(String host, int port) throws UnknownHostException, IOException {

        NetworkManager.logPrintf("[ID: Await]: Attempting the connection to the server at %s:%d\n", host, port);
        final NetworkManager serverBoundConnection = new NetworkManager(new Socket(host, port)) {

            // send encryption request
            private KeyPair keys = null;
            {
                NetworkManager.logPrintf("[ID: Await]: Attempting the encryption request...\n");
                try {
                    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                    keyGen.initialize(2048);
                    keys = keyGen.generateKeyPair();
                    ByteBuffer request = new P0Handshake(null, -1).pSs0(keys.getPublic().getEncoded());
                    this.sendData(request);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                
            }

            @Override
            protected void onDataReceived(ByteBuffer incomingData) throws MalformedPacketException {
                
                ByteBuffer packetContent = null;
                if (this.sharedSecret == null) {
                    // decrypt received packet
                    try {
                        Cipher cipher = Cipher.getInstance("RSA");
                        cipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());
                        packetContent = ByteBuffer.wrap(cipher.doFinal(incomingData.array()));
                    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                        this.close();
                    } 
                } else {
                    packetContent = this.decryptData(incomingData);
                }

                switch (packetContent.get(0)) {
                    case 0x00: 
                        P0Handshake receivedPacket = new P0Handshake(packetContent, this.connectionId); 
                        if (receivedPacket.getPacketState() == 1) {
                            byte[] encodedKey = receivedPacket.pCs1readSecret();
                            this.sharedSecret = new SecretKeySpec(encodedKey, "AES");
                            NetworkManager.logPrintf("[ID: Await]: Received encryption protocol.\n");
                        } else if (receivedPacket.getPacketState() == 2) {
                            this.connectionId = receivedPacket.pCs2readConnectionId();
                            NetworkManager.logPrintf("[ID: Await]: Received connection ID.\n");
                            NetworkManager.logPrintf("[ID: %d]: Server connection established.\n", this.connectionId);
                        }
                        
                        break;
                    default: throw new MalformedPacketException();
                }

            }
            
        };
        
        return serverBoundConnection;

    }

    /**
     * Simple logger implementation to be mainly used in Swing components to share output.<br>
     * NetworkManager provides a method to print log on System.out, and PrintStream if set:<br>
     * <code>NetworkManager.logPrintf()</code>.<br>
     * If this outStream is not set, it is equivalent to <code>PrintStream.printf()</code>.
     * @param out
     */
    public static void setSharedOutDestination(PrintStream out) {
        outStream = out;
    }

    /**
     * 
     * @return
     */
    public static void logPrintf(String f, Object... stuff) {
        if (stuff.length == 0) {
            System.out.printf(f);
            if (outStream != null)
                outStream.printf(f);
        } else {
            System.out.printf(f, stuff);
            if (outStream != null)
                outStream.printf(f, stuff);
        }
    }

}
