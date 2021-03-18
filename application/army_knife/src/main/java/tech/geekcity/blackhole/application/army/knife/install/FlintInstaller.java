package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.application.army.knife.image.FlintImage;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.core.ResourceManager;
import tech.geekcity.blackhole.lib.docker.DockerProxy;
import tech.geekcity.blackhole.render.RenderEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;

@FreeBuilder
@JsonDeserialize(builder = FlintInstaller.Builder.class)
public abstract class FlintInstaller extends Installer implements Configurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlintInstaller.class);

    /**
     * Returns a new {@link Builder} with the same property values as this {@link FlintInstaller}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link FlintInstaller} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends FlintInstaller_Builder {
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

        public FlintInstaller parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, FlintInstaller.class);
        }
    }

    @Override
    public abstract SshConnector sshConnector();

    public abstract List<SshConnector> sshConnectorToPushFlintImageList();

    public abstract String flintImageTag();

    @Override
    public void configure() throws IOException {
        super.configure();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    @Override
    protected void doInstall() throws IOException {
        // save, upload and load flint.image.dim
        File dockerImageFile = File.createTempFile("flint.image.", ".dim");
        try (DockerProxy dockerProxy = DockerProxy.Builder.newInstance()
                .build()) {
            dockerProxy.configure();
            dockerProxy.saveImage(
                    String.format("%s:%s", FlintImage.IMAGE_NAME, flintImageTag()),
                    dockerImageFile
            );
        }
        sshConnectorToPushFlintImageList()
                .forEach(sshConnectorToPush -> {
                    LOGGER.info("push flint image to {}:{}",
                            sshConnectorToPush.host(), sshConnectorToPush.port());
                    try {
                        sshConnectorToPush.configure();
                        sshConnectorToPush.validateSshCommander()
                                .runAndCheckReturn("mkdir -p /tmp/blackhole/docker_image");
                        sshConnectorToPush.validateSimpleScp()
                                .upload(Collections.singletonList(dockerImageFile.getAbsolutePath()),
                                        "/tmp/blackhole/docker_image/flint.image.dim");
                        sshConnectorToPush.validateSshCommander()
                                .runAndCheckReturn("docker image load -i /tmp/blackhole/docker_image/flint.image.dim");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        dockerImageFile.delete();
        // upload and apply flint.k8s.yaml
        super.runSingleCommand("mkdir -p /tmp/blackhole/k8s");
        super.createTempFileAndUpload("flint.k8s.", ".yaml",
                flintK8sYaml(FlintImage.IMAGE_NAME, flintImageTag()),
                "/tmp/blackhole/k8s/flint.k8s.yaml");
        super.runSingleCommand("kubectl -n blackhole apply -f /tmp/blackhole/k8s/flint.k8s.yaml");
    }

    private String flintK8sYaml(String flintImageName, String flintImageTag) throws IOException {
        File templateFile = File.createTempFile("flint.k8s.", ".yaml.template");
        FileUtils.writeStringToFile(
                templateFile,
                ResourceManager.contentFromResource(
                        this.getClass(),
                        "blackhole.army.knife/flint/flint.k8s.yaml.template")
        );
        try (RenderEngine renderEngine = RenderEngine.Builder.newInstance()
                .templatePath(templateFile.getParentFile().getAbsolutePath())
                .build()) {
            renderEngine.configure();
            ByteArrayOutputStream byteArrayOutputStream = renderEngine.render(
                    ImmutableMap.of("flint_image_name", String.format("%s:%s", flintImageName, flintImageTag)),
                    templateFile.getName()
            );
            return byteArrayOutputStream.toString();
        }
    }
}
