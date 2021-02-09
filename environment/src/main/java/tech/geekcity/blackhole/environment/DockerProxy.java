package tech.geekcity.blackhole.environment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.inferred.freebuilder.FreeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@FreeBuilder
@JsonDeserialize(builder = DockerProxy.Builder.class)
public abstract class DockerProxy implements Closeable {
    private transient DockerClient dockerClient;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link DockerProxy}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link DockerProxy} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends DockerProxy_Builder {
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

        public DockerProxy parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, DockerProxy.class);
        }
    }

    public void open() {
        DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
        dockerClient = DockerClientImpl.getInstance(dockerClientConfig, dockerHttpClient);
    }

    @Override
    public void close() throws IOException {

    }

    public String apiVersion() {
        VersionCmd versionCmd = dockerClient.versionCmd();
        Version version = versionCmd.exec();
        return version.getApiVersion();
    }

    public String buildImage(
            @Nonnull File dockerFile,
            @Nullable File baseDirectory,
            @Nonnull String imageName,
            @Nonnull String tag
    ) {
        // TODO there is a bug with docker-java:
        // https://github.com/docker-java/docker-java/issues/1537
        // dockerfile should in baseDirectory
        Preconditions.checkArgument(
                null == baseDirectory || dockerFile.getParentFile().equals(baseDirectory),
                "there is a bug with docker-java lib: %s",
                "https://github.com/docker-java/docker-java/issues/1537");
        BuildImageCmd buildImageCmd = dockerClient.buildImageCmd()
                .withTags(Collections.singleton(String.format("%s:%s", imageName, tag)))
                .withBaseDirectory(null != baseDirectory ? baseDirectory : dockerFile.getParentFile())
                .withDockerfile(dockerFile);
        return buildImageCmd.exec(new BuildImageResultCallback()).awaitImageId();
    }

    public List<String> listImageId() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        return imageList.stream()
                .map(Image::getId)
                .collect(Collectors.toList());
    }

    public List<String> listImage() {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        return imageList.stream()
                .flatMap(image -> Arrays.stream(image.getRepoTags()))
                .collect(Collectors.toList());
    }

    public void startContainer(String imageNameWithTag, String containerName, List<String> cmd) {
        dockerClient.startContainerCmd(
                dockerClient.createContainerCmd(imageNameWithTag)
                        .withName(containerName)
                        .withHostConfig(HostConfig.newHostConfig()
                                .withPrivileged(true)
                                .withAutoRemove(true))
                        .withCmd(cmd)
                        .exec()
                        .getId()
        ).exec();
    }

    public void stopContainer(String containerName) {
    }

    public String exec(String containerName, String command) {
        return "response";
    }
}
