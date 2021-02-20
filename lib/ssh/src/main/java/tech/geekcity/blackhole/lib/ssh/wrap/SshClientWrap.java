package tech.geekcity.blackhole.lib.ssh.wrap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

@FreeBuilder
@JsonDeserialize(builder = SshClientWrap.Builder.class)
public abstract class SshClientWrap implements Configurable {
    private transient SshClient sshClient;
    private transient ClientSession clientSession;
    private transient boolean configured = false;

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

        public Builder() {
            strictCheck(false);
            knownHostsFile(new File("/dev/null"));
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

    @Nullable
    public abstract String password();

    @Nullable
    public abstract RsaKeyPairWrap rsaKeyPairWrap();

    public abstract File knownHostsFile();

    public abstract boolean strictCheck();

    @Override
    public void configure() throws IOException {
        if (configured) {
            return;
        }
        sshClient = SshClient.setUpDefaultClient();
        sshClient.setServerKeyVerifier(
                new DefaultKnownHostsServerKeyVerifier(
                        AcceptAllServerKeyVerifier.INSTANCE,
                        true,
                        new File("/dev/null")));
        sshClient.start();
        clientSession = sshClient.connect(username(), host(), port())
                .verify()
                .getSession();
        String password = password();
        RsaKeyPairWrap rsaKeyPairWrap = rsaKeyPairWrap();
        Preconditions.checkArgument(null != password || null != rsaKeyPairWrap);
        if (null != password) {
            clientSession.addPasswordIdentity(password);
        }
        if (null != rsaKeyPairWrap) {
            clientSession.addPublicKeyIdentity(rsaKeyPairWrap.keyPair());
        }
        clientSession.auth().verify();
        configured = true;
    }

    @Override
    public void close() throws IOException {
        if (null != clientSession) {
            clientSession.close();
            clientSession = null;
        }
        if (null != sshClient) {
            sshClient.stop();
            sshClient.close();
            sshClient = null;
        }
        if (configured) {
            configured = false;
        }
    }

    public ClientSession clientSession() {
        return clientSession;
    }
}
