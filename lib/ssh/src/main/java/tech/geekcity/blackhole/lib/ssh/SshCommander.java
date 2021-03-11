package tech.geekcity.blackhole.lib.ssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.ssh.wrap.SshClientWrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

@FreeBuilder
@JsonDeserialize(builder = SshCommander.Builder.class)
public abstract class SshCommander implements Configurable {

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

    public abstract SshClientWrap sshClientWrap();

    public abstract OutputStream standardOutput();

    public abstract OutputStream errorOutput();

    @Override
    public void configure() throws IOException {
        sshClientWrap().configure();
    }

    @Override
    public void close() throws IOException {
        SshClientWrap sshClientWrap = sshClientWrap();
        if (null != sshClientWrap) {
            sshClientWrap.close();
        }
    }

    public int run(String command) throws IOException {
        return run(command, -1L);
    }

    public int run(String command, long timeoutInMilliseconds) throws IOException {
        ClientSession clientSession = sshClientWrap().clientSession();
        try (ClientChannel channel = clientSession.createExecChannel(command)) {
            channel.setOut(standardOutput());
            channel.setErr(errorOutput());
            channel.open().verify();
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeoutInMilliseconds);
            return channel.getExitStatus();
        }
    }
}
