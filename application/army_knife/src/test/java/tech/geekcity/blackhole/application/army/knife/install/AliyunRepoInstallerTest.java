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

public class AliyunRepoInstallerTest extends AbstractSshClientTest {
    private transient ByteArrayOutputStream stdout;
    private transient ByteArrayOutputStream stderr;
    private transient SshConnector sshConnector;
    private transient String repoContent;
    private transient File centos7RepoFile;

    @BeforeEach
    void setUp() throws IOException {
        super.configure();
        stdout = new ByteArrayOutputStream();
        stderr = new ByteArrayOutputStream();
        SshClientWrap rsaKeyPairWrap = super.sshClientWrap();
        sshConnector = SshConnector.Builder.newInstance()
                .username(rsaKeyPairWrap.username())
                .host(rsaKeyPairWrap.host())
                .port(rsaKeyPairWrap.port())
                .standardOutput(stdout)
                .errorOutput(stderr)
                .rsaKeyPairWrap(rsaKeyPairWrap.rsaKeyPairWrap())
                .build();
        repoContent = originalRepoString() + "\n"
                + "# marked by blackhole " + RandomStringUtils.randomAlphanumeric(16) + "\n";
        centos7RepoFile = File.createTempFile("centos.7.aliyun.", ".repo");
        FileUtils.writeStringToFile(centos7RepoFile, repoContent, StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (null != sshConnector) {
            sshConnector.close();
            sshConnector = null;
        }
        if (null != centos7RepoFile) {
            centos7RepoFile.delete();
        }
        super.close();
    }

    @Test
    void test() throws IOException {
        try (Installer installer = AliyunRepoInstaller.Builder.newInstance()
                .centos7RepoPath(centos7RepoFile.getAbsolutePath())
                .sshConnector(sshConnector)
                .build()) {
            installer.configure();
            installer.install();
            File downloadedFile = File.createTempFile("downloaded.centos.7.aliyun.", ".repo");
            downloadedFile.delete();
            installer.simpleScp()
                    .download(
                            Collections.singletonList("/etc/yum.repos.d/centos.7.aliyun.repo"),
                            downloadedFile.getAbsolutePath());
            Assertions.assertEquals(repoContent, FileUtils.readFileToString(downloadedFile, StandardCharsets.UTF_8));
        }
    }

    private String originalRepoString() throws IOException {
        return IOUtils.toString(
                Objects.requireNonNull(
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream("blackhole.army.knife/centos.7.aliyun.repo")
                ));
    }
}
