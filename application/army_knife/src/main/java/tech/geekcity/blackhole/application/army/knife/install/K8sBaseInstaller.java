package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.docker.DockerProxy;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@FreeBuilder
@JsonDeserialize(builder = K8sBaseInstaller.Builder.class)
public abstract class K8sBaseInstaller extends Installer implements Configurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sBaseInstaller.class);

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
        String targetPath = "/tmp/blackhole/docker_image";
        Map<String, String> imageMeta = ImmutableMap.<String, String>builder()
                .put("calico/node:v3.18.1", "calico.node.v3.18.1")
                .put("calico/pod2daemon-flexvol:v3.18.1", "calico.pod2daemon-flexvol.v3.18.1")
                .put("calico/cni:v3.18.1", "calico.cni.v3.18.1")
                .put("calico/kube-controllers:v3.18.1", "calico.kube-controllers")
                .put("registry.aliyuncs.com/google_containers/kube-proxy:v1.20.2",
                        "registry.aliyuncs.com.google_containers.kube-proxy.v1.20.2")
                .put("registry.aliyuncs.com/google_containers/kube-controller-manager:v1.20.2",
                        "registry.aliyuncs.com.google_containers.kube-controller-manager.v1.20.2")
                .put("registry.aliyuncs.com/google_containers/kube-apiserver:v1.20.2",
                        "registry.aliyuncs.com.google_containers.kube-apiserver.v1.20.2")
                .put("registry.aliyuncs.com/google_containers/kube-scheduler:v1.20.2",
                        "registry.aliyuncs.com.google_containers.kube-scheduler.v1.20.2")
                .put("registry.aliyuncs.com/google_containers/etcd:3.4.13-0",
                        "registry.aliyuncs.com.google_containers.etcd.3.4.13-0")
                .put("registry.aliyuncs.com/google_containers/coredns:1.7.0",
                        "registry.aliyuncs.com.google_containers.coredns.1.7.0")
                .put("registry.aliyuncs.com/google_containers/pause:3.2",
                        "registry.aliyuncs.com.google_containers.pause.3.2")
                .build();
        for (Map.Entry<String, String> entry : imageMeta.entrySet()) {
            pullImageAndLoadAtRemote(entry.getKey(), targetPath, entry.getValue());
        }
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

    private void pullImageAndLoadAtRemote(
            String imageNameWithTag,
            String targetPath,
            String targetFileName
    ) throws IOException {
        LOGGER.info("pull and load image({})...", imageNameWithTag);
        super.runSingleCommand(String.format("mkdir -p %s", targetPath));
        File dockerImageFile = File.createTempFile(String.format("%s.", targetFileName), ".dim");
        try (DockerProxy dockerProxy = DockerProxy.Builder.newInstance()
                .build()) {
            dockerProxy.configure();
            dockerProxy.pullImage(imageNameWithTag);
            dockerProxy.saveImage(imageNameWithTag, dockerImageFile);
        }
        String targetImageFilePath = String.format("%s/%s.dim", targetPath, targetFileName);
        sshConnector().validateSimpleScp()
                .upload(Collections.singletonList(dockerImageFile.getAbsolutePath()), targetImageFilePath);
        super.runSingleCommand(String.format("docker image load -i %s", targetImageFilePath));
        dockerImageFile.delete();
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
