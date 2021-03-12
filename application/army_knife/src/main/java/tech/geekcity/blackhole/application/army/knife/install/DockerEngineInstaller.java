package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nullable;
import java.io.IOException;

@FreeBuilder
@JsonDeserialize(builder = DockerEngineInstaller.Builder.class)
public abstract class DockerEngineInstaller extends Installer implements Configurable {
    /**
     * Returns a new {@link Builder} with the same property values as this {@link DockerEngineInstaller}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link DockerEngineInstaller} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends DockerEngineInstaller_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder() {
            start(true);
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

        public DockerEngineInstaller parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, DockerEngineInstaller.class);
        }
    }

    @Override
    public abstract SshConnector sshConnector();

    @Nullable
    public abstract String dockerCeRepoPath();

    @Nullable
    public abstract String dockerDaemonJsonPath();

    public abstract boolean start();

    @Override
    public void configure() throws IOException {
        super.configure();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    public void install() throws IOException {
        super.createTempFileAndUpload(
                "docker.ce.aliyun.",
                ".repo",
                dockerCeRepoString(),
                "/etc/yum.repos.d/docker.ce.aliyun.repo");
        super.runSingleCommand("mkdir -p /etc/docker");
        super.createTempFileAndUpload(
                "docker.daemon.ustc.",
                ".json",
                dockerDaemonJson(),
                "/etc/docker/daemon.json");
        ImmutableList.of(
                "yum install -y yum-utils device-mapper-persistent-data lvm2",
                "yum makecache fast",
                "yum -y install docker-ce",
                "systemctl enable docker"
        ).forEach(super::runSingleCommand);
        if (start()) {
            super.runSingleCommand("systemctl start docker");
        }
    }

    private String dockerCeRepoString() throws IOException {
        return super.contentFromFileOrResource(
                dockerCeRepoPath(),
                "blackhole.army.knife/docker.ce.aliyun.repo"
        );
    }

    private String dockerDaemonJson() throws IOException {
        return super.contentFromFileOrResource(
                dockerDaemonJsonPath(),
                "blackhole.army.knife/docker.daemon.json"
        );
    }
}
