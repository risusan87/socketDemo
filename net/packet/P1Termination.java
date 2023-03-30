package net.packet;

import java.nio.ByteBuffer;

public class P1Termination extends PacketBase {

    protected P1Termination(ByteBuffer data, int clientId) {
        super(data, clientId);
    }

    @Override
    public byte getPacketID() {
        return 0x01;
    }

    @Override
    public boolean isServerBound() {
        return this.packetState == 0;
    }
    
}
