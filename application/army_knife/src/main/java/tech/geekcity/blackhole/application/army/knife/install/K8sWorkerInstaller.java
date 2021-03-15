package tech.geekcity.blackhole.application.army.knife.install;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

@FreeBuilder
@JsonDeserialize(builder = K8sWorkerInstaller.Builder.class)
public abstract class K8sWorkerInstaller extends Installer implements Configurable {
    private transient K8sBaseInstaller k8sBaseInstaller;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link K8sWorkerInstaller}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link K8sWorkerInstaller} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends K8sWorkerInstaller_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        @Override
        public K8sWorkerInstaller build() {
            if (null == masterSshConnector()) {
                K8sMasterInstaller masterInstaller = masterInstaller();
                Preconditions.checkArgument(
                        null != masterInstaller,
                        "masterSshConnector and masterInstaller cannot both be null"
                );
                masterSshConnector(masterInstaller.sshConnector());
            }
            return super.build();
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

        public K8sWorkerInstaller parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, K8sWorkerInstaller.class);
        }
    }

    @Override
    public abstract SshConnector sshConnector();

    /**
     * will be ignored if {@link #masterSshConnector() has been set}
     *
     * @return a master installer
     */
    @Nullable
    public abstract K8sMasterInstaller masterInstaller();

    /**
     * {@link #masterSshConnector()} and {@link #masterInstaller()} cannot both be null
     *
     * @return a {@link SshConnector} of master host
     */
    @Nullable
    public abstract SshConnector masterSshConnector();

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
        ImmutableList.of(
                "firewall-cmd --permanent --add-port=10250/tcp",
                "firewall-cmd --permanent --add-port=10251/tcp",
                "firewall-cmd --permanent --add-port=10252/tcp",
                "firewall-cmd --permanent --add-port=10255/tcp",
                "firewall-cmd --permanent --add-port=8472/udp",
                "firewall-cmd --add-masquerade --permanent",
                "firewall-cmd --permanent --add-port=30000-32767/tcp",
                "firewall-cmd --reload"
        ).forEach(super::runSingleCommand);
        super.runSingleCommand(
                generateJoinCommandFromMaster(
                        chooseMasterSshConnector()));
    }

    private SshConnector chooseMasterSshConnector() {
        SshConnector masterSshConnectorToUse;
        if (null != masterSshConnector()) {
            masterSshConnectorToUse = masterSshConnector();
        } else {
            K8sMasterInstaller masterInstaller = masterInstaller();
            Preconditions.checkArgument(
                    null != masterInstaller,
                    "masterSshConnector and masterInstaller cannot both be null"
            );
            Preconditions.checkArgument(masterInstaller.installed(),
                    "masterInstaller should be installed before use");
            masterSshConnectorToUse = masterInstaller.sshConnector();
        }
        return masterSshConnectorToUse;
    }

    private String generateJoinCommandFromMaster(SshConnector masterSshConnectorToUse) throws IOException {
        masterSshConnectorToUse.configure();
        String remoteTempFilePath = String.format(
                "/tmp/join_command_%s.txt",
                RandomStringUtils.randomAlphanumeric(8));
        masterSshConnectorToUse.validateSshCommander()
                .run(String.format("kubeadm token create --print-join-command > %s", remoteTempFilePath));
        File localTempFile = File.createTempFile("join_command.", ".txt");
        localTempFile.delete();
        masterSshConnectorToUse.validateSimpleScp()
                .download(Collections.singletonList(remoteTempFilePath), localTempFile.getAbsolutePath());
        masterSshConnectorToUse.validateSshCommander()
                .run(String.format("rm -f %s", remoteTempFilePath));
        String joinCommand = FileUtils.readFileToString(localTempFile);
        localTempFile.delete();
        return joinCommand;
    }
}
