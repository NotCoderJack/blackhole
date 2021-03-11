package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.ssh.SimpleScp;
import tech.geekcity.blackhole.lib.ssh.SshCommander;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@FreeBuilder
@JsonDeserialize(builder = DockerEngineInstaller.Builder.class)
public abstract class DockerEngineInstaller extends Installer implements Configurable {
    private transient boolean configured = false;
    private transient SshConnector sshConnector;
    private transient SshCommander sshCommander;
    private transient SimpleScp simpleScp;

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
        simpleScp.upload(
                Collections.singletonList(
                        dockerCeRepoFile.getAbsolutePath()),
                "/etc/yum.repos.d/centos.7.aliyun.repo");
        dockerCeRepoFile.delete();
        ImmutableList.of(
                "yum install -y yum-utils device-mapper-persistent-data lvm2",
                "yum makecache fast",
                "yum -y install docker-ce",
                "systemctl enable docker",
                "systemctl start docker"
        ).forEach(super::runSingleCommand);
    }

    private String dockerCeRepo() {
        return this.getClass()
                .getResourceAsStream("blackhole.army.knife/docker.ce.aliyun.repo")
                .toString();
    }
}
