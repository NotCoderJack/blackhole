package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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

    public void install() throws IOException {
        super.runSingleCommand("rm -rf /etc/yum.repos.d/*");
        File centos7RepoFile = File.createTempFile("centos.7.aliyun.", ".repo");
        FileUtils.writeStringToFile(centos7RepoFile, centos7RepoString(), StandardCharsets.UTF_8);
        super.simpleScp().upload(
                Collections.singletonList(
                        centos7RepoFile.getAbsolutePath()),
                "/etc/yum.repos.d/centos.7.aliyun.repo");
        centos7RepoFile.delete();
    }

    private String centos7RepoString() throws IOException {
        String centos7RepoPath = centos7RepoPath();
        if (null != centos7RepoPath) {
            return FileUtils.readFileToString(new File(centos7RepoPath), StandardCharsets.UTF_8);
        }
        return IOUtils.toString(
                Objects.requireNonNull(
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("blackhole.army.knife/centos.7.aliyun.repo")
                ));
    }
}
