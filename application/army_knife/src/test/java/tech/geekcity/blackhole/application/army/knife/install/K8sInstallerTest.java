package tech.geekcity.blackhole.application.army.knife.install;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class K8sInstallerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sInstallerTest.class);
    private transient boolean skip;
    private transient String masterHost;
    private transient SshConnector masterSshConnector;
    private transient SshConnector workerSshConnector;

    @BeforeEach
    void setUp() throws IOException {
        // prepare machines first
        // virtual machine is a nice option
        // note: the number of available CPUs should be 2 at least
        // ./gradlew :application:army_knife:test \
        //     --tests "tech.geekcity.blackhole.application.army.knife.install.K8sInstallerTest" \
        //     -Pblackhole.test.master_host=192.168.123.180
        //     -Pblackhole.test.worker_host=192.168.123.181
        masterHost = System.getProperty("blackhole.test.master_host", "");
        int masterPort = Integer.parseInt(System.getProperty("blackhole.test.master_port", "22"));
        String workerHost = System.getProperty("blackhole.test.worker_host", "");
        int workerPort = Integer.parseInt(System.getProperty("blackhole.test.worker_port", "22"));
        String username = System.getProperty("blackhole.test.username", "root");
        String password = System.getProperty("blackhole.test.password", "123456");
        skip = StringUtils.isBlank(masterHost);
        if (skip) {
            LOGGER.warn("masterHost is blank, skip all tests in {}", this.getClass().getName());
            return;
        }
        masterSshConnector = SshConnector.Builder.newInstance()
                .username(username)
                .host(masterHost)
                .port(masterPort)
                .standardOutput(new ByteArrayOutputStream())
                .errorOutput(new ByteArrayOutputStream())
                .password(password)
                .build();
        workerSshConnector = SshConnector.Builder.newInstance()
                .username(username)
                .host(workerHost)
                .port(workerPort)
                .standardOutput(new ByteArrayOutputStream())
                .errorOutput(new ByteArrayOutputStream())
                .password(password)
                .build();
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
        try (Installer installer = AliyunRepoInstaller.Builder.newInstance()
                .sshConnector(masterSshConnector)
                .build()) {
            installer.configure();
            installer.install();
        }
        try (Installer installer = DockerEngineInstaller.Builder.newInstance()
                .sshConnector(masterSshConnector)
                .build()) {
            installer.configure();
            installer.install();
        }
        try (Installer installer = K8sMasterInstaller.Builder.newInstance()
                .sshConnector(masterSshConnector)
                .build()) {
            installer.configure();
            installer.install();
        }
        try (Installer installer = K8sWorkerInstaller.Builder.newInstance()
                .sshConnector(workerSshConnector)
                .build()) {
            installer.configure();
            installer.install();
        }
        // TODO check one service
    }
}
