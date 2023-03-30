package net.packet;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import javax.crypto.SecretKey;

import net.NetworkManager.MalformedPacketException;

/**
 * Base of packets in both bounds.
 * This contains data as content of default TCP connection.
 */
public abstract class PacketBase { 
    
    protected final ByteBuffer content;
    protected int packetState = 0;
    public abstract byte getPacketID();
    public abstract boolean isServerBound();
    protected final int connectionId;

    protected PacketBase(ByteBuffer data, int clientId) {
        this.content = data;
        this.connectionId = clientId;
    }

    public int getPacketState() {
        return this.packetState;
    }

    public ByteBuffer getPacketContent() {
        return this.content;
    }

    protected final ByteArrayOutputStream prepareByteArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(this.getPacketID());
        baos.write(this.packetState);
        return baos;
    }

    public int getConnectionId() {
        return this.connectionId;
    }

}