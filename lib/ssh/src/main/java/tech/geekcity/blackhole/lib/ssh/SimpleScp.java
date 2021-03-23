package tech.geekcity.blackhole.lib.ssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.scp.ScpClientCreator;
import org.apache.sshd.client.session.ClientSession;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.ssh.wrap.SshClientWrap;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@FreeBuilder
@JsonDeserialize(builder = SimpleScp.Builder.class)
public abstract class SimpleScp implements Configurable {

    /**
     * Returns a new {@link Builder} with the same property values as this {@link SimpleScp}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link SimpleScp} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SimpleScp_Builder {
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

        public SimpleScp parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SimpleScp.class);
        }
    }

    public abstract SshClientWrap sshClientWrap();

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

    public void upload(List<String> localFileList, String remotePath)
            throws IOException {
        upload(localFileList, remotePath, ImmutableList.of(ScpClient.Option.PreserveAttributes));
    }

    public void upload(
            List<String> localFileList,
            String remotePath,
            Collection<ScpClient.Option> options)
            throws IOException {
        ClientSession clientSession = sshClientWrap().clientSession();
        ScpClientCreator creator = ScpClientCreator.instance();
        ScpClient scpClient = creator.createScpClient(clientSession);
        scpClient.upload(
                localFileList.toArray(new String[0]),
                remotePath,
                options);
    }

    public void download(
            List<String> remoteFileList,
            String localPath)
            throws IOException {
        download(remoteFileList, localPath, ImmutableList.of(ScpClient.Option.PreserveAttributes));
    }

    public void download(
            List<String> remoteFileList,
            String localPath,
            Collection<ScpClient.Option> options)
            throws IOException {
        ClientSession clientSession = sshClientWrap().clientSession();
        ScpClientCreator creator = ScpClientCreator.instance();
        ScpClient scpClient = creator.createScpClient(clientSession);
        scpClient.download(
                remoteFileList.toArray(new String[0]),
                localPath,
                options);
    }
}
