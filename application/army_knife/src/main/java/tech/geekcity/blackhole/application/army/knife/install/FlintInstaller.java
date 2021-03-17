package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.image.FlintImage;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.core.ResourceManager;
import tech.geekcity.blackhole.lib.docker.DockerProxy;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@FreeBuilder
@JsonDeserialize(builder = FlintInstaller.Builder.class)
public abstract class FlintInstaller extends Installer implements Configurable {
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
        runSingleCommand("kubectl label nodes `hostname` flint=true --overwrite");
        DockerProxy dockerProxy = DockerProxy.Builder.newInstance()
                .build();
        File dockerImageFile = File.createTempFile("flint.image.", ".dim");
        dockerProxy.saveImage(
                String.format("%s:%s", FlintImage.IMAGE_NAME, flintImageTag()),
                dockerImageFile
        );
        sshConnector().validateSimpleScp()
                .upload(Collections.singletonList(dockerImageFile.getAbsolutePath()),
                        "/tmp/blackhole/docker_image/flint.image.dim");
        dockerImageFile.delete();
        super.runSingleCommand("docker image load -i /tmp/blackhole/docker_image/flint.image.dim");
        // specific namespace and create or not by user
        super.runSingleCommand("kubectl create namespace blackhole --dry-run=client -o yaml | kubectl apply -f -");
        super.runSingleCommand("kubectl -n blackhole create secret generic kube-config --from-file=.kube/");
        super.createTempFileAndUpload("flint.k8s.", ".yaml",
                ResourceManager.contentFromResource(
                        this.getClass(),
                        "blackhole.army.knife/flint/flint.k8s.yaml"),
                "/tmp/blackhole/k8s/flint.k8s.yaml");
        super.runSingleCommand("kubectl -n blackhole apply -f /tmp/blackhole/k8s/flint.k8s.yaml");
    }
}
