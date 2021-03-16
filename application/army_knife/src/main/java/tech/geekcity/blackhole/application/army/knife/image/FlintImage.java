package tech.geekcity.blackhole.application.army.knife.image;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.io.FileUtils;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.core.ResourceManager;
import tech.geekcity.blackhole.lib.docker.DockerProxy;
import tech.geekcity.blackhole.lib.docker.util.DockerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@FreeBuilder
@JsonDeserialize(builder = FlintImage.Builder.class)
public abstract class FlintImage implements Configurable {
    public static final String IMAGE_NAME = DockerUtil.camelToSnake(FlintImage.class.getSimpleName());
    private transient DockerProxy dockerProxy;
    private transient File buildDirectory;
    private transient File dockerFile;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link FlintImage}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link FlintImage} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends FlintImage_Builder {
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

        public FlintImage parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, FlintImage.class);
        }
    }

    public abstract String tag();

    @Override
    public void configure() throws IOException {
        dockerProxy = DockerProxy.Builder.newInstance()
                .build();
        dockerProxy.configure();
        buildDirectory = Files.createTempDirectory("flint.build.").toFile();
        dockerFile = File.createTempFile("flint.", ".dockerfile", buildDirectory);
        FileUtils.writeStringToFile(
                dockerFile,
                ResourceManager.contentFromResource(
                        this.getClass(), "blackhole.army.knife/flint/flint.dockerfile"),
                StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(
                new File(String.format("%s/centos.7.aliyun.repo", buildDirectory.getAbsolutePath())),
                ResourceManager.contentFromResource(
                        this.getClass(), "blackhole.army.knife/centos.7.aliyun.repo"),
                StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(
                new File(String.format("%s/kubernetes.aliyun.repo", buildDirectory.getAbsolutePath())),
                ResourceManager.contentFromResource(
                        this.getClass(), "blackhole.army.knife/kubernetes.aliyun.repo"),
                StandardCharsets.UTF_8);
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
