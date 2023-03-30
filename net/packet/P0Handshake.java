package net.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import net.NetworkManager;

public class P0Handshake extends PacketBase {

    public P0Handshake(ByteBuffer data, int clientId) {
        super(data, clientId);
        if (data != null) {
            this.content.get();
            this.packetState = this.content.get();
        }
    }

    /**
     * This is the starting point of P0HandShake. <br>
     * 
     * Packet bound to Server at state 0. <br>
     * Prepares encryption request to the server. <br>
     * 
     * <b>This byte sequence is the final product.</b>
     * 
     * @param publicKey
     * @return
     */
    public ByteBuffer pSs0(byte[] publicKey) {
        this.packetState = 0;
        try (
            ByteArrayOutputStream baos = this.prepareByteArray();
        ) {
            int keyLength = publicKey.length;
            baos.write(ByteBuffer.allocate(Integer.BYTES).putInt(keyLength).array());
            baos.write(publicKey);
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Packet bound to Client at state 1. <br>
     * Prepares reponse to the encryption request.<br>
     * 
     * <b>This byte sequence is the final product.</b>
     * 
     * @throws IllegalStateException  If the method was called on packet state other than 0
     * @param sharedSecret  An array of byte sequence representation of original key.
     * @return
     */
    public ByteBuffer pCs1(byte[] sharedSecret) throws IllegalStateException, NetworkManager.MalformedPacketException {

        if (this.getPacketState() != 0)
            throw new IllegalStateException();

        int keyLength = sharedSecret.length;
        byte[] resByte = null;
        this.packetState = 1;

        // can be encupslated such as preparePacket() or putData()
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ) {
            baos.write(this.getPacketID());
            baos.write(this.getPacketState());
            baos.write(ByteBuffer.allocate(Integer.BYTES).putInt(keyLength).array());
            baos.write(sharedSecret);
            resByte = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } 
        
        // prep to send back the shared secret
        try {
            Cipher cipher = Cipher.getInstance("RSA");

            // get public key reveived
            PublicKey receivedPubKey = null;
            int pubKeyLength = this.content.getInt();
            byte[] keyByte = new byte[pubKeyLength];
            this.content.get(keyByte);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyByte);
            try {
                KeyFactory keyFac = KeyFactory.getInstance("RSA");
                receivedPubKey = keyFac.generatePublic(keySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
                return null;
            }
            cipher.init(Cipher.ENCRYPT_MODE, receivedPubKey);
            byte[] finalByte = cipher.doFinal(resByte);
            return ByteBuffer.wrap(finalByte);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (BufferUnderflowException e) {
            throw new NetworkManager.MalformedPacketException("Handshake packet does not contain public key.");
        }
        return null;
    }

    /**
     * This must be called from the server side.
     * 
     * @return
     */
    public ByteBuffer pCs2() {

        if (this.packetState != 1)
            throw new IllegalStateException();

        this.packetState = 2;
        try (
            ByteArrayOutputStream baos = this.prepareByteArray();
        ) {
            baos.write(ByteBuffer.allocate(Integer.BYTES).putInt(Integer.BYTES).array());
            baos.write(ByteBuffer.allocate(Integer.BYTES).putInt(this.connectionId).array());
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * Packet receiving in Client reads shared secret.
     * 
     * @return
     */
    public byte[] pCs1readSecret() throws NetworkManager.MalformedPacketException {
        if (this.packetState != 1)
            throw new IllegalStateException();
        
        int keyLength = this.content.getInt();
        byte[] sharedKey = new byte[keyLength];
        this.content.get(sharedKey);
        return sharedKey;
    }

    public int pCs2readConnectionId() {
        if (this.packetState != 2)
            throw new IllegalStateException();
        
        this.content.getInt();
        return this.content.getInt();
    }

    @Override
    public byte getPacketID() {
        return 0x00;
    }

    @Override
    public boolean isServerBound() {
        return this.packetState == 0;
    }

}
