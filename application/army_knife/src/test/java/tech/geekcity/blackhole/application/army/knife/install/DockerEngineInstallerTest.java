package tech.geekcity.blackhole.application.army.knife.install;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.geekcity.blackhole.application.army.knife.AbstractSshClientTest;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.ssh.wrap.SshClientWrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

public class DockerEngineInstallerTest extends AbstractSshClientTest {
    private transient SshConnector sshConnector;
    private transient String repoContent;
    private transient String dockerDaemonJson;
    private transient File dockerCeRepoFile;

    @BeforeEach
    void setUp() throws IOException {
        super.configure();
        SshClientWrap rsaKeyPairWrap = super.sshClientWrap();
        sshConnector = SshConnector.Builder.newInstance()
                .username(rsaKeyPairWrap.username())
                .host(rsaKeyPairWrap.host())
                .port(rsaKeyPairWrap.port())
                .standardOutput(new ByteArrayOutputStream())
                .errorOutput(new ByteArrayOutputStream())
                .rsaKeyPairWrap(rsaKeyPairWrap.rsaKeyPairWrap())
                .build();
        repoContent = originalRepoString() + "\n"
                + "# marked by blackhole " + RandomStringUtils.randomAlphanumeric(16) + "\n";
        dockerDaemonJson = originalDockerDaemonJson();
        dockerCeRepoFile = File.createTempFile("docker.ce.aliyun.", ".repo");
        FileUtils.writeStringToFile(dockerCeRepoFile, repoContent, StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (null != sshConnector) {
            sshConnector.close();
            sshConnector = null;
        }
        if (null != dockerCeRepoFile) {
            dockerCeRepoFile.delete();
        }
        super.close();
    }

    @Test
    void test() throws IOException {
        try (Installer installer = DockerEngineInstaller.Builder.newInstance()
                .dockerCeRepoPath(dockerCeRepoFile.getAbsolutePath())
                .sshConnector(sshConnector)
                // we cannot start docker engine in docker(there's a way to start, but we do not need that)
                .start(false)
                .build()) {
            installer.configure();
            installer.install();
            File downloadedRepoFile = File.createTempFile("downloaded.docker.ce.aliyun.", ".repo");
            downloadedRepoFile.delete();
            installer.simpleScp()
                    .download(
                            Collections.singletonList("/etc/yum.repos.d/docker.ce.aliyun.repo"),
                            downloadedRepoFile.getAbsolutePath());
            Assertions.assertEquals(repoContent, FileUtils.readFileToString(downloadedRepoFile, StandardCharsets.UTF_8));
            File downloadedDockerDaemonJsonFile = File.createTempFile("downloaded.docker.daemon.json.", ".repo");
            downloadedDockerDaemonJsonFile.delete();
            installer.simpleScp()
                    .download(
                            Collections.singletonList("/etc/docker/daemon.json"),
                            downloadedDockerDaemonJsonFile.getAbsolutePath());
            Assertions.assertEquals(dockerDaemonJson, FileUtils.readFileToString(downloadedDockerDaemonJsonFile, StandardCharsets.UTF_8));
        }
    }

    private String originalRepoString() throws IOException {
        return IOUtils.toString(
                Objects.requireNonNull(
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("blackhole.army.knife/docker.ce.aliyun.repo")
                ));
    }

    private String originalDockerDaemonJson() throws IOException {
        return IOUtils.toString(
                Objects.requireNonNull(
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("blackhole.army.knife/docker.daemon.json")
                ));
    }
}
