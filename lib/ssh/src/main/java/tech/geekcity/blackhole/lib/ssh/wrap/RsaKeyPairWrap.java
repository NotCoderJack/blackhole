package tech.geekcity.blackhole.lib.ssh.wrap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.exception.BugException;

import java.io.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@FreeBuilder
@JsonDeserialize(builder = RsaKeyPairWrap.Builder.class)
public abstract class RsaKeyPairWrap {
    private transient KeyPair keyPair;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link RsaKeyPairWrap}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link RsaKeyPairWrap} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends RsaKeyPairWrap_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public String toJsonSilently() {
            try {
                return objectMapper.writeValueAsString(build());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public RsaKeyPairWrap parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, RsaKeyPairWrap.class);
        }

        public RsaKeyPairWrap generate(int size) throws NoSuchAlgorithmException, IOException {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(size);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return parseFromKeyPair(keyPair);
        }

        public RsaKeyPairWrap parseFromKeyPair(KeyPair keyPair) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(keyPair);
            objectOutputStream.flush();
            objectOutputStream.close();
            keyValuePairBase64(Base64.getEncoder()
                    .encodeToString(byteArrayOutputStream.toByteArray())
            );
            return build();
        }
    }

    public abstract String keyValuePairBase64();

    public PrivateKey privateKey() {
        return keyPair().getPrivate();
    }

    public PublicKey publicKey() {
        return keyPair().getPublic();
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
                    "%s %s %s\n",
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

    protected KeyPair keyPair() {
        if (null == keyPair) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(
                        new ByteArrayInputStream(
                                Base64.getDecoder()
                                        .decode(keyValuePairBase64())));
                Object object = objectInputStream.readObject();
                Preconditions.checkArgument(
                        object instanceof KeyPair,
                        "object(%s) is not an instance of %s",
                        object.getClass().getName(), KeyPair.class.getName());
                keyPair = (KeyPair) object;
            } catch (IOException | ClassNotFoundException e) {
                throw BugException.wrap(e);
            }
        }
        return keyPair;
    }
}
