package tech.geekcity.blackhole.ssh;

import com.google.common.base.Preconditions;
import tech.geekcity.blackhole.core.exception.BugException;

import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class RsaKeyPairWrap implements Serializable {
    private final KeyPair keyPair;

    public static RsaKeyPairWrap deserialize(byte[] keyPairDataBytes) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(keyPairDataBytes));
        Object object = objectInputStream.readObject();
        Preconditions.checkArgument(
                object instanceof KeyPair,
                "object(%s) is not an instance of %s",
                object.getClass().getName(), KeyPair.class.getName());
        return new RsaKeyPairWrap((KeyPair) object);
    }

    public RsaKeyPairWrap(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public KeyPair keyPair() {
        return keyPair;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(keyPair());
        objectOutputStream.flush();
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public PrivateKey privateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey publicKey() {
        return keyPair.getPublic();
    }

    public String publicKeyAsString(String user) {
        String algorithmDescriptor = "ssh-rsa";
        PublicKey publicKey = publicKey();
        Preconditions.checkArgument(publicKey instanceof RSAPublicKey);
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(algorithmDescriptor.getBytes().length);
            dataOutputStream.write(algorithmDescriptor.getBytes());
            dataOutputStream.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dataOutputStream.write(rsaPublicKey.getPublicExponent().toByteArray());
            dataOutputStream.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dataOutputStream.write(rsaPublicKey.getModulus().toByteArray());
            dataOutputStream.flush();
            dataOutputStream.close();
            return String.format(
                    "%s %s %s",
                    algorithmDescriptor, Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()), user);
        } catch (IOException e) {
            throw BugException.wrap(e);
        }
    }

    public String privateKeyAsString() {
        PrivateKey privateKey = privateKey();
        return String.format(
                "-----BEGIN RSA PRIVATE KEY-----\n%s\n-----END RSA PRIVATE KEY-----\n",
                Base64.getMimeEncoder().encodeToString(privateKey.getEncoded()));
    }
}
