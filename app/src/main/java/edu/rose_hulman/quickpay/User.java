package edu.rose_hulman.quickpay;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import ch.uepaa.p2pkit.discovery.Peer;

/**
 * Created by Zane on 9/27/2015.
 */
public class User {
    public UUID nodeId;
    public String accountNumber;
    public String name;
    public String bankCode;

    public User(Peer peer) {
        this.nodeId = peer.getNodeId();
        try {
            ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(peer.getDiscoveryInfo()));
            Data data = (Data) stream.readObject();

            accountNumber = data.accountNumber;
            name = data.name;
            bankCode = data.bankCode;
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }

    public User() {
    }

    public byte[] write() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(512);
            ObjectOutputStream stream = new ObjectOutputStream(bytes);

            Data data = new Data();
            data.accountNumber = accountNumber;
            data.name = name;
            data.bankCode = bankCode;

            stream.writeObject(data);
            return bytes.toByteArray();
        } catch (IOException e) {
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof User)) {
            return false;
        }

        User other = (User) o;

        return nodeId.equals(other.nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }

    private class Data implements Serializable {
        private String accountNumber;
        private String name;
        private String bankCode;
    }
}
