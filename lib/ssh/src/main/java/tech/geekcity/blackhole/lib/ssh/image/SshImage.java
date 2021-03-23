package tech.geekcity.blackhole.lib.ssh.image;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.docker.DockerProxy;
import tech.geekcity.blackhole.lib.docker.image.CentOsBase;
import tech.geekcity.blackhole.lib.docker.util.DockerUtil;
import tech.geekcity.blackhole.render.RenderEngine;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@FreeBuilder
@JsonDeserialize(builder = SshImage.Builder.class)
public abstract class SshImage implements Configurable {
    public static final Logger LOGGER = LoggerFactory.getLogger(SshImage.class);
    public static final String IMAGE_NAME = DockerUtil.camelToSnake(SshImage.class.getSimpleName());
    private transient DockerProxy dockerProxy;
    private transient File buildDirectory;
    private transient File dockerFile;
    private transient String centOsBaseImage;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link SshImage}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link SshImage} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SshImage_Builder {
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

        public SshImage parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SshImage.class);
        }
    }

    @Nullable
    public abstract String centOsBaseImage();

    public abstract String tag();

    @Override
    public void configure() throws IOException {
        dockerProxy = DockerProxy.Builder.newInstance()
                .build();
        dockerProxy.configure();
        buildDirectory = Files.createTempDirectory("ssh.build.").toFile();
        dockerFile = File.createTempFile("ssh.", ".dockerfile", buildDirectory);
        String dockerFileTemplateContent = IOUtils.toString(
                Objects.requireNonNull(getClass()
                        .getClassLoader()
                        .getResource("environment/ssh/ssh.dockerfile.template")),
                StandardCharsets.UTF_8);
        File dockerFileTemplate = File.createTempFile("ssh.docker", ".template");
        FileUtils.writeStringToFile(dockerFileTemplate, dockerFileTemplateContent, StandardCharsets.UTF_8);
        centOsBaseImage = centOsBaseImage();
        if (null == centOsBaseImage) {
            String tag = tag();
            LOGGER.info(
                    "centOsBaseImage not set, building default image({}) with tag({})...",
                    CentOsBase.IMAGE_NAME, tag);
            buildCentOsBaseImage(tag);
            centOsBaseImage = String.format("%s:%s", CentOsBase.IMAGE_NAME, tag);
        }
        Preconditions.checkNotNull(
                dockerProxy.findImageByRepoTag(centOsBaseImage),
                "image(%s) not found", centOsBaseImage);
        renderSingleFile(
                ImmutableMap.of("CENT_OS_BASE_IMAGE", centOsBaseImage),
                dockerFileTemplate,
                dockerFile);
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

    public void startContainer(
            String containerName,
            File authorizedKeysFile,
            int bindPort) {
        // TODO check permission bits of authorizedKeysFile
        dockerProxy.startContainer(
                String.format("%s:%s", IMAGE_NAME, tag()),
                containerName,
                null,
                Collections.singletonList(
                        String.format("%s:/root/.ssh/authorized_keys", authorizedKeysFile.getAbsolutePath())),
                Collections.singletonList(String.format("%s:22", bindPort)),
                true,
                true
        );
    }

    public void stopContainer(String containerName){
        dockerProxy.stopContainer(containerName);
    }

    private void renderSingleFile(Map<String, String> config, File templateFile, File outputFile)
            throws IOException {
        Properties properties = new Properties();
        properties.putAll(config);
        try (RenderEngine renderEngine = RenderEngine.Builder.newInstance()
                .templatePath(templateFile.getParentFile().getAbsolutePath())
                .build()) {
            renderEngine.configure();
            ByteArrayOutputStream byteArrayOutputStream = renderEngine.render(
                    properties.entrySet().stream().collect(Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            entry -> entry.getValue().toString()
                    )),
                    templateFile.getName());
            FileUtils.writeStringToFile(outputFile, byteArrayOutputStream.toString(), StandardCharsets.UTF_8);
        }
    }

    private void buildCentOsBaseImage(String tag) throws IOException {
        try (CentOsBase centOsBase = CentOsBase.Builder.newInstance()
                .tag(tag)
                .build()) {
            centOsBase.configure();
            centOsBase.buildImage();
        }
    }
}
