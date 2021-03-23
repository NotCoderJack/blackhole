package tech.geekcity.blackhole.application.army.knife.install;

import com.google.common.base.Splitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.application.army.knife.image.FlintImage;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class K8sClusterInstallerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sClusterInstallerTest.class);
    private static final String FLINT_IMAGE_TAG = "v_test_1.0";
    private transient boolean skip;
    private transient String masterHost;
    private transient SshConnector masterSshConnector;
    private transient List<SshConnector> workerSshConnectorList;

    @BeforeEach
    void setUp() throws IOException {
        // prepare machines first
        // virtual machine is a nice option: reference to external/create_vm_with_virtualbox.md
        // note: the number of available CPUs should be 2 at least
        // NOTE: run the command below first because we need a valid jar of flint
        // ./gradlew --info :application:army_knife:bootJar
        // ./gradlew :application:army_knife:test \
        //     --tests "tech.geekcity.blackhole.application.army.knife.install.K8sClusterInstallerTest" \
        //     -Pblackhole.test.master_host=master-k8s \
        //     -Pblackhole.test.worker_host.list=worker1-k8s,worker2-k8s
        masterHost = System.getProperty("blackhole.test.master_host", "");
        int masterPort = Integer.parseInt(System.getProperty("blackhole.test.master_port", "22"));
        String workerHostList = System.getProperty("blackhole.test.worker_host.list", "");
        int workerPort = Integer.parseInt(System.getProperty("blackhole.test.worker_port", "22"));
        String username = System.getProperty("blackhole.test.username", "root");
        String password = System.getProperty("blackhole.test.password", "123456");
        skip = StringUtils.isBlank(masterHost);
        if (skip) {
            LOGGER.warn("masterHost is blank, skip all tests in {}", this.getClass().getName());
            return;
        }
        try (FlintImage flintImage = FlintImage.Builder.newInstance()
                .tag(FLINT_IMAGE_TAG)
                .build()) {
            flintImage.configure();
            LOGGER.info("building flint image...");
            flintImage.buildImage();
        }
        masterSshConnector = SshConnector.Builder.newInstance()
                .username(username)
                .host(masterHost)
                .port(masterPort)
                .standardOutput(new ByteArrayOutputStream())
                .errorOutput(new ByteArrayOutputStream())
                .password(password)
                .build();
        workerSshConnectorList = Splitter.on(",")
                .omitEmptyStrings()
                .splitToStream(workerHostList)
                .distinct()
                .map(workerHost -> SshConnector.Builder.newInstance()
                        .username(username)
                        .host(workerHost)
                        .port(workerPort)
                        .standardOutput(new ByteArrayOutputStream())
                        .errorOutput(new ByteArrayOutputStream())
                        .password(password)
                        .build())
                .collect(Collectors.toList());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (skip) {
            skip = false;
        }
    }

    @Test
    void testInstall() throws IOException {
        if (skip) {
            return;
        }
        LOGGER.info("installing base environment for master({}:{})",
                masterSshConnector.host(), masterSshConnector.port());
        installBaseEnvironment(masterSshConnector);
        try (K8sMasterInstaller masterInstaller = K8sMasterInstaller.Builder.newInstance()
                .sshConnector(masterSshConnector.toBuilder()
                        .standardOutput(new ByteArrayOutputStream())
                        .errorOutput(new ByteArrayOutputStream())
                        .build())
                .build()) {
            masterInstaller.configure();
            LOGGER.info("installing k8s for master({}:{})",
                    masterSshConnector.host(), masterSshConnector.port());
            masterInstaller.install();
            for (SshConnector workerSshConnector : workerSshConnectorList) {
                LOGGER.info("installing base environment for worker({}:{})",
                        workerSshConnector.host(), workerSshConnector.port());
                installBaseEnvironment(workerSshConnector);
                try (Installer workerInstaller = K8sWorkerInstaller.Builder.newInstance()
                        .masterInstaller(masterInstaller)
                        .sshConnector(workerSshConnector.toBuilder()
                                .standardOutput(new ByteArrayOutputStream())
                                .errorOutput(new ByteArrayOutputStream())
                                .build())
                        .build()) {
                    workerInstaller.configure();
                    LOGGER.info("installing k8s for worker({}:{})",
                            workerSshConnector.host(), workerSshConnector.port());
                    workerInstaller.install();
                }
            }
            try (Installer flintInstaller = FlintInstaller.Builder.newInstance()
                    .sshConnector(masterSshConnector.toBuilder()
                            .standardOutput(new ByteArrayOutputStream())
                            .errorOutput(new ByteArrayOutputStream())
                            .build())
                    .flintImageTag(FLINT_IMAGE_TAG)
                    .addSshConnectorToPushFlintImageList(masterSshConnector)
                    .addAllSshConnectorToPushFlintImageList(workerSshConnectorList)
                    .build()) {
                flintInstaller.configure();
                LOGGER.info("installing flint at master node({}:{})",
                        masterSshConnector.host(), masterSshConnector.port());
                flintInstaller.install();
            }
        }
        // TODO check one service
    }

    private void installBaseEnvironment(SshConnector sshConnector) throws IOException {
        try (Installer installer = AliyunRepoInstaller.Builder.newInstance()
                .sshConnector(sshConnector.toBuilder()
                        .standardOutput(new ByteArrayOutputStream())
                        .errorOutput(new ByteArrayOutputStream())
                        .build())
                .build()) {
            installer.configure();
            installer.install();
        }
        try (Installer installer = DockerEngineInstaller.Builder.newInstance()
                .sshConnector(sshConnector.toBuilder()
                        .standardOutput(new ByteArrayOutputStream())
                        .errorOutput(new ByteArrayOutputStream())
                        .build())
                .build()) {
            installer.configure();
            installer.install();
        }
    }
}
