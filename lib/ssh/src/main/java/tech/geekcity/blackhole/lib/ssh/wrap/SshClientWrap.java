package tech.geekcity.blackhole.lib.ssh.wrap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.KeyPair;

@FreeBuilder
@JsonDeserialize(builder = SshClientWrap.Builder.class)
public abstract class SshClientWrap implements Configurable {
    private transient SshClient sshClient;
    private transient ClientSession clientSession;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link SshClientWrap}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link SshClientWrap} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SshClientWrap_Builder {
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

        public SshClientWrap parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SshClientWrap.class);
        }
    }

    public abstract String username();

    public abstract String host();

    public abstract int port();

    // TODO remove
    public abstract KeyPair keyPair();

    @Nullable
    public abstract String password();

    @Nullable
    public abstract RsaKeyPairWrap rsaKeyPairWrap();

    @Override
    public void configure() throws IOException {
        sshClient = SshClient.setUpDefaultClient();
        sshClient.setServerKeyVerifier(new DefaultKnownHostsServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE));
        sshClient.start();
        clientSession = sshClient.connect(username(), host(), port())
                .verify()
                .getSession();
        String password = password();
        RsaKeyPairWrap rsaKeyPairWrap = rsaKeyPairWrap();
        // TODO remove
        if (null == password && null == rsaKeyPairWrap) {
            clientSession.addPublicKeyIdentity(keyPair());
        }
        // TODO active
        // Preconditions.checkArgument(null != password && null != rsaKeyPairWrap);
        if (null != password) {
            clientSession.addPasswordIdentity(password);
        }
        if (null != rsaKeyPairWrap) {
            clientSession.addPublicKeyIdentity(rsaKeyPairWrap.keyPair());
        }
        clientSession.auth().verify();
    }

    @Override
    public void close() throws IOException {
        if (null != clientSession) {
            clientSession.close();
        }
        if (null != sshClient) {
            sshClient.stop();
            sshClient.close();
        }
    }

    public ClientSession clientSession() {
        return clientSession;
    }
}
