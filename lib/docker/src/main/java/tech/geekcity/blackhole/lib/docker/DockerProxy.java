package tech.geekcity.blackhole.lib.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@FreeBuilder
@JsonDeserialize(builder = DockerProxy.Builder.class)
public abstract class DockerProxy implements Configurable {
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

    @Override
    public void configure() {
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

    public void pullImage(String imageNameWithTag) {
        try {
            dockerClient.pullImageCmd(imageNameWithTag)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion();
        } catch (InterruptedException e) {
            throw new DockerClientException(String.format("pull image failed: %s", e.getMessage()), e);
        }
    }

    public void saveImage(String imageNameWithTag, File targetFile) throws IOException {
        FileUtils.copyInputStreamToFile(
                dockerClient.saveImageCmd(imageNameWithTag)
                        .exec(),
                targetFile);
    }

    public boolean existsImage(String imageName, String tag) {
        return existsImage(String.format("%s:%s", imageName, tag));
    }

    public boolean existsImage(String expectedRepoTag) {
        return null != findImageByRepoTag(expectedRepoTag);
    }

    @Nullable
    public Image findImageByRepoTag(String expectedRepoTag) {
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        return imageList.stream()
                .filter(image -> Arrays.asList(image.getRepoTags())
                        .contains(expectedRepoTag))
                .findAny()
                .orElse(null);
    }

    @Nullable
    public Image findImageByName(String imageName, String tag) {
        return findImageByRepoTag(
                String.format("%s:%s", imageName, tag));
    }

    public void startContainer(String imageNameWithTag, String containerName, List<String> cmd) {
        startContainer(imageNameWithTag,
                containerName,
                cmd,
                null,
                null,
                false,
                true);
    }

    public void startContainer(
            String imageNameWithTag,
            String containerName,
            @Nullable List<String> cmd,
            @Nullable List<String> volumeBindList,
            @Nullable List<String> portBindList,
            boolean privileged,
            boolean autoRemove) {
        List<PortBinding> portBindingList = null == portBindList ? Collections.emptyList()
                : portBindList.stream()
                .map(PortBinding::parse)
                .collect(Collectors.toList());
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPrivileged(privileged)
                .withAutoRemove(autoRemove)
                .withBinds(null == volumeBindList ? Collections.emptyList()
                        : volumeBindList.stream()
                        .map(Bind::parse)
                        .collect(Collectors.toList()))
                .withPortBindings(portBindingList);
        CreateContainerCmd createContainerCmd = dockerClient.createContainerCmd(imageNameWithTag)
                .withName(containerName)
                .withHostConfig(hostConfig)
                .withExposedPorts(portBindingList.stream()
                        .map(PortBinding::getExposedPort)
                        .collect(Collectors.toList()));
        if (null != cmd) {
            createContainerCmd.withCmd(cmd);
        }
        dockerClient.startContainerCmd(
                createContainerCmd.exec()
                        .getId()
        ).exec();
    }

    public Map<String, String> listContainer() {
        return dockerClient.listContainersCmd().exec().stream()
                .flatMap(container -> Arrays.stream(container.getNames())
                        .map(containerName -> Pair.of(container.getId(), containerName)))
                .collect(Collectors.toMap(
                        Pair::getRight,
                        Pair::getLeft,
                        (oldValue, newValue) -> newValue
                ));
    }

    public void stopContainer(String containerNameOrId) {
        dockerClient.stopContainerCmd(containerNameOrId).exec();
    }

    public void exec(
            String containerNameOrId,
            OutputStream stdout, OutputStream stderr,
            String... command) throws InterruptedException {
        dockerClient.execStartCmd(
                dockerClient.execCreateCmd(containerNameOrId)
                        .withAttachStdout(true)
                        .withAttachStderr(true)
                        .withCmd(command)
                        .exec()
                        .getId()
        ).exec(new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                if (frame != null) {
                    try {
                        switch (frame.getStreamType()) {
                            case STDOUT:
                            case RAW:
                                if (stdout != null) {
                                    stdout.write(frame.getPayload());
                                    stdout.flush();
                                }
                                break;
                            case STDERR:
                                if (stderr != null) {
                                    stderr.write(frame.getPayload());
                                    stderr.flush();
                                }
                                break;
                            default:
                                throw new RuntimeException(String.format("unknown type: %s", frame.getStreamType()));
                        }
                    } catch (IOException e) {
                        onError(e);
                    }
                }
            }
        }).awaitCompletion();
    }
}
