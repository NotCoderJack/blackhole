package tech.geekcity.blackhole.lib.docker.image;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.docker.DockerProxy;
import tech.geekcity.blackhole.lib.docker.util.DockerUtil;
import tech.geekcity.blackhole.lib.core.Configurable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

@FreeBuilder
@JsonDeserialize(builder = CentOsBase.Builder.class)
public abstract class CentOsBase implements Configurable {
    public static final String IMAGE_NAME = DockerUtil.camelToSnake(CentOsBase.class.getSimpleName());
    private transient DockerProxy dockerProxy;
    private transient File buildDirectory;
    private transient File dockerFile;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link CentOsBase}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link CentOsBase} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends CentOsBase_Builder {
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

        public CentOsBase parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CentOsBase.class);
        }
    }

    public abstract String tag();

    @Override
    public void configure() throws IOException {
        dockerProxy = DockerProxy.Builder.newInstance()
                .build();
        dockerProxy.configure();
        buildDirectory = Files.createTempDirectory("centos.base.build.").toFile();
        dockerFile = File.createTempFile("centos.base.", ".dockerfile", buildDirectory);
        String dockerFileContent = IOUtils.toString(
                Objects.requireNonNull(getClass()
                        .getClassLoader()
                        .getResource("environment/centos/centos.base.dockerfile")),
                StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(dockerFile, dockerFileContent, StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws IOException {
        if (null != dockerProxy) {
            dockerProxy.close();
            dockerProxy = null;
        }
        if (null != buildDirectory) {
            FileUtils.deleteDirectory(buildDirectory);
        }
    }

    public String buildImage() {
        return dockerProxy.buildImage(dockerFile, buildDirectory, IMAGE_NAME, tag());
    }
}
