package tech.geekcity.blackhole.ssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.core.Configurable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.util.EnumSet;

@FreeBuilder
@JsonDeserialize(builder = SshCommander.Builder.class)
public abstract class SshCommander implements Configurable {
    private transient SshClient client;
    private transient ClientSession session;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link SshCommander}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link SshCommander} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SshCommander_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder() {
            standardOutput(new ByteArrayOutputStream());
            errorOutput(new ByteArrayOutputStream());
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

        public SshCommander parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SshCommander.class);
        }
    }

    public abstract String username();

    public abstract String host();

    public abstract int port();

    public abstract KeyPair keyPair();

    public abstract OutputStream standardOutput();

    public abstract OutputStream errorOutput();

    @Override
    public void open() throws IOException {
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(new DefaultKnownHostsServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE));
        client.start();
        session = client.connect(username(), host(), port())
                .verify()
                .getSession();
        session.addPublicKeyIdentity(keyPair());
        session.auth().verify();
    }

    @Override
    public void close() throws IOException {
        if (null != client) {
            client.stop();
            client.close();
        }
        if (null != session) {
            session.close();
        }
    }

    public int run(String command) throws IOException {
        return run(command, 0L);
    }

    public int run(String command, long timeoutInMilliseconds) throws IOException {
        try (ClientChannel channel = session.createExecChannel(command)) {
            channel.setOut(System.out);
            channel.setErr(System.err);
            channel.open().verify();
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeoutInMilliseconds);
        }
        return 0;
    }
}
