package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nullable;
import java.io.IOException;

@FreeBuilder
@JsonDeserialize(builder = AliyunRepoInstaller.Builder.class)
public abstract class AliyunRepoInstaller extends Installer implements Configurable {
    /**
     * Returns a new {@link Builder} with the same property values as this {@link AliyunRepoInstaller}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link AliyunRepoInstaller} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends AliyunRepoInstaller_Builder {
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

        public AliyunRepoInstaller parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, AliyunRepoInstaller.class);
        }
    }

    @Override
    public abstract SshConnector sshConnector();

    @Nullable
    public abstract String centos7RepoPath();

    @Override
    public void configure() throws IOException {
        super.configure();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    protected void doInstall() throws IOException {
        super.runSingleCommand("rm -rf /etc/yum.repos.d/*");
        super.createTempFileAndUpload(
                "centos.7.aliyun.",
                ".repo",
                centos7RepoString(),
                "/etc/yum.repos.d/centos.7.aliyun.repo");
    }

    private String centos7RepoString() throws IOException {
        return super.contentFromFileOrResource(
                centos7RepoPath(),
                "blackhole.army.knife/centos.7.aliyun.repo"
        );
    }
}
