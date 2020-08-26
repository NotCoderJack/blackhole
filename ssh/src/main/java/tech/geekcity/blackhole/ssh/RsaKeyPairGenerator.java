package tech.geekcity.blackhole.ssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.core.Configurable;
import tech.geekcity.blackhole.core.exception.BugException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@FreeBuilder
@JsonDeserialize(builder = RsaKeyPairGenerator.Builder.class)
public abstract class RsaKeyPairGenerator implements Configurable {
    private transient KeyPair keyPair;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link RsaKeyPairGenerator}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link RsaKeyPairGenerator} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends RsaKeyPairGenerator_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder() {
            keySize(2048);
            user("auto-gen-user");
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

        public RsaKeyPairGenerator parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, RsaKeyPairGenerator.class);
        }
    }

    @Override
    public void open() throws IOException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize());
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw BugException.wrap(e);
        }
    }

    public abstract int keySize();

    public abstract String user();

    @Override
    public void close() throws IOException {
        keyPair = null;
    }

    public KeyPair keyPair() {
        return keyPair;
    }

    public PrivateKey privateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey publicKey() {
        return keyPair.getPublic();
    }

    public String publicKeyAsString() {
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
            return String.format(
                    "%s %s %s",
                    algorithmDescriptor, Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()), user());
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
