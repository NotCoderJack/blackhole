package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

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

    @Override
    public void configure() throws IOException {
        super.configure();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    public void install() throws IOException {
        super.runSingleCommand("rm -rf /etc/yum.repos.d/*");
        File centos7RepoFile = File.createTempFile("centos.7.aliyun.", ".repo");
        FileUtils.writeStringToFile(centos7RepoFile, centos7Repo(), StandardCharsets.UTF_8);
        super.simpleScp().upload(
                Collections.singletonList(
                        centos7RepoFile.getAbsolutePath()),
                "/etc/yum.repos.d/docker.ce.aliyun.repo");
        centos7RepoFile.delete();
    }

    private String centos7Repo() {
        return this.getClass()
                .getResourceAsStream("blackhole.army.knife/centos.7.aliyun.repo")
                .toString();
    }
}
