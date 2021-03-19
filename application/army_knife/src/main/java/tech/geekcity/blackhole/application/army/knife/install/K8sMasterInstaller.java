package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nullable;
import java.io.IOException;

@FreeBuilder
@JsonDeserialize(builder = K8sMasterInstaller.Builder.class)
public abstract class K8sMasterInstaller extends Installer implements Configurable {
    private transient K8sBaseInstaller k8sBaseInstaller;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link K8sMasterInstaller}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link K8sMasterInstaller} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends K8sMasterInstaller_Builder {
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

        public K8sMasterInstaller parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, K8sMasterInstaller.class);
        }
    }

    @Override
    public abstract SshConnector sshConnector();

    @Nullable
    public abstract String calicoYamlPath();

    @Override
    public void configure() throws IOException {
        super.configure();
        k8sBaseInstaller = K8sBaseInstaller.Builder.newInstance()
                .sshConnector(sshConnector())
                .build();
        k8sBaseInstaller.configure();
    }

    @Override
    public void close() throws IOException {
        if (null != k8sBaseInstaller) {
            k8sBaseInstaller.close();
            k8sBaseInstaller = null;
        }
        super.close();
    }

    @Override
    protected void doInstall() throws IOException {
        k8sBaseInstaller.install();
        super.createTempFileAndUpload(
                "calico.",
                ".yaml",
                calicoYamlString(),
                "/root/calico.yaml");
        ImmutableList.of(
                "kubeadm init --kubernetes-version=v1.20.2 --pod-network-cidr=172.21.0.0/20 --image-repository registry.aliyuncs.com/google_containers",
                // TODO remove
                "sed -i -Ee \"s/^([^#].*--port=0.*)/#\\1/g\" /etc/kubernetes/manifests/kube-scheduler.yaml",
                // TODO remove
                "sed -i -Ee \"s/^([^#].*--port=0.*)/#\\1/g\" /etc/kubernetes/manifests/kube-controller-manager.yaml",
                "systemctl restart kubelet",
                "mkdir -p $HOME/.kube",
                "cp /etc/kubernetes/admin.conf $HOME/.kube/config",
                "chown $(id -u):$(id -g) $HOME/.kube/config",
                "kubectl create namespace blackhole",
                "kubectl -n blackhole create secret generic kube-config --from-file=.kube/",
                "kubectl apply -f /root/calico.yaml"
        ).forEach(super::runSingleCommand);
    }

    private String calicoYamlString() throws IOException {
        return super.contentFromFileOrResource(
                calicoYamlPath(),
                "blackhole.army.knife/calico.yaml"
        );
    }
}
