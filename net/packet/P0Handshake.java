package net.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class P0Handshake extends PacketBase {

    
    @Override
    public byte getPacketID() {
        return 0x00;
    }

    @Override
    public boolean isServerBound() {
        return this.packetState == 0;
    }

    @Override
    public P0Handshake readPacket(byte[] rawData) {
        ByteBuffer buffer = ByteBuffer.allocate(rawData.length);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(rawData);
        buffer.flip();
        buffer.get();

        P0Handshake packet = new P0Handshake();
        packet.packetState = (int) buffer.get();

        switch (packet.packetState) {
            case 0:

        }

        return null;
    }

    @Override
    public byte[] writePacket() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] initials = super.writePacket();
        baos.write(initials, 0, initials.length);
        switch (this.packetState) {
            case 0:
                KeyPairGenerator gen = null;
                try {
                    gen = KeyPairGenerator.getInstance("RSA");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                gen.initialize(16 * 8);
                KeyPair keys = gen.genKeyPair();
                byte[] publicKey = keys.getPublic().getEncoded();
                baos.write(publicKey, 0, publicKey.length);
                break;
            
        }
        return baos.toByteArray();
    }

}
