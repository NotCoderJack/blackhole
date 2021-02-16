package tech.geekcity.blackhole.lib.ssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.core.exception.BugException;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@FreeBuilder
@JsonDeserialize(builder = RsaKeyPairGenerator.Builder.class)
public abstract class RsaKeyPairGenerator implements Configurable {
    private transient RsaKeyPairWrap rsaKeyPairWrap;

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
    public void configure() throws IOException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            rsaKeyPairWrap = new RsaKeyPairWrap(keyPair);
        } catch (NoSuchAlgorithmException e) {
            throw BugException.wrap(e);
        }
    }

    public abstract int keySize();

    @Override
    public void close() throws IOException {
        rsaKeyPairWrap = null;
    }

    public RsaKeyPairWrap rsaKeyPairWrap() {
        return rsaKeyPairWrap;
    }
}
