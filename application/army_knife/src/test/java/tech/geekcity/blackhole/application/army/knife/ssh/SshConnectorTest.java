package tech.geekcity.blackhole.application.army.knife.ssh;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.application.army.knife.AbstractSshClientTest;
import tech.geekcity.blackhole.lib.ssh.SshCommander;
import tech.geekcity.blackhole.lib.ssh.wrap.SshClientWrap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SshConnectorTest extends AbstractSshClientTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SshConnectorTest.class);
    private static final String PASSWORD = RandomStringUtils.randomAlphanumeric(16);
    private transient SshCommander sshCommander;
    private transient ByteArrayOutputStream stdout;
    private transient ByteArrayOutputStream stderr;

    @BeforeEach
    void setUp() throws IOException {
        super.configure();
        stdout = new ByteArrayOutputStream();
        stderr = new ByteArrayOutputStream();
        sshCommander = SshCommander.Builder.newInstance()
                .sshClientWrap(super.sshClientWrap())
                .standardOutput(stdout)
                .errorOutput(stderr)
                .build();
        sshCommander.configure();
        LOGGER.debug("password: %s", PASSWORD);
        sshCommander.runAndCheckReturn(String.format("echo '%s\n%s' | passwd", PASSWORD, PASSWORD));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (null != sshCommander) {
            sshCommander.close();
        }
        super.close();
    }

    @Test
    void testRun() throws IOException {
        SshClientWrap testSshClientWrap = super.sshClientWrap();

        try (SshConnector sshConnector = SshConnector.Builder.newInstance()
                .username(testSshClientWrap.username())
                .host(testSshClientWrap.host())
                .port(testSshClientWrap.port())
                .standardOutput(stdout)
                .errorOutput(stderr)
                .password(PASSWORD)
                .build()) {
            sshConnector.configure();
            SshCommander sshCommander = sshConnector.validateSshCommander();
            Assertions.assertNotNull(sshCommander);
        }
    }
}
