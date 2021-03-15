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
@JsonDeserialize(builder = K8sBaseInstaller.Builder.class)
public abstract class K8sBaseInstaller extends Installer implements Configurable {
    /**
     * Returns a new {@link Builder} with the same property values as this {@link K8sBaseInstaller}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link K8sBaseInstaller} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends K8sBaseInstaller_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder() {
            start(true);
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

        public K8sBaseInstaller parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, K8sBaseInstaller.class);
        }
    }

    @Override
    public abstract SshConnector sshConnector();

    @Nullable
    public abstract String dockerCeRepoPath();

    public abstract boolean start();

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
        super.createTempFileAndUpload(
                "modules.load.k8s.",
                ".conf",
                modulesLoadK8sConfig(),
                "/etc/modules-load.d/k8s.conf");
        super.createTempFileAndUpload(
                "sysctl.k8s.",
                ".conf",
                sysctlK8sConfig(),
                "/etc/sysctl.d/k8s.conf");
        super.createTempFileAndUpload(
                "kubernetes.aliyun.",
                ".repo",
                kubernetesAliyunRepo(),
                "/etc/yum.repos.d/kubernetes.aliyun.repo");
        ImmutableList.of(
                "sysctl --system",
                "setenforce 0",
                "sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config",
                "yum install -y kubelet kubeadm kubectl --disableexcludes=kubernetes",
                "sed -i -Ee 's/^([^#][^ \\t]+ swap[ \\t]+ swap[ \\t].*)/#\\1/' /etc/fstab",
                "swapoff -a",
                "systemctl enable kubelet"
        ).forEach(super::runSingleCommand);
        if (start()) {
            super.runSingleCommand("systemctl start kubelet");
        }
    }

    private String modulesLoadK8sConfig() throws IOException {
        return super.contentFromFileOrResource(
                dockerCeRepoPath(),
                "blackhole.army.knife/etc.modules.load.d.k8s.conf"
        );
    }

    private String sysctlK8sConfig() throws IOException {
        return super.contentFromFileOrResource(
                dockerCeRepoPath(),
                "blackhole.army.knife/etc.sysctl.d.k8s.conf"
        );
    }

    private String kubernetesAliyunRepo() throws IOException {
        return super.contentFromFileOrResource(
                dockerCeRepoPath(),
                "blackhole.army.knife/kubernetes.aliyun.repo"
        );
    }
}
