package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

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
        File dockerCeRepoFile = File.createTempFile("docker.ce.aliyun.", ".repo");
        FileUtils.writeStringToFile(dockerCeRepoFile, dockerCeRepo(), StandardCharsets.UTF_8);
        super.simpleScp().upload(
                Collections.singletonList(
                        dockerCeRepoFile.getAbsolutePath()),
                "/etc/yum.repos.d/docker.ce.aliyun.repo");
        dockerCeRepoFile.delete();
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

    private String dockerCeRepo() throws IOException {
        String dockerCeRepoPath = dockerCeRepoPath();
        if (null != dockerCeRepoPath) {
            return FileUtils.readFileToString(new File(dockerCeRepoPath), StandardCharsets.UTF_8);
        }
        return IOUtils.toString(
                Objects.requireNonNull(
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("blackhole.army.knife/docker.ce.aliyun.repo")
                ));
    }
}
