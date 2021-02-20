package tech.geekcity.blackhole.lib.ssh.wrap;

import org.apache.sshd.client.session.ClientSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.geekcity.blackhole.lib.ssh.AbstractSshClientTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SshClientWrapTest extends AbstractSshClientTest {
    @BeforeEach
    void setUp() throws IOException {
        super.configure();
    }

    @AfterEach
    void tearDown() throws IOException {
        super.close();
    }

    @Test
    void testSession() throws IOException {
        ClientSession clientSession = super.sshClientWrap().clientSession();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        clientSession.executeRemoteCommand(
                "cat /root/.ssh/authorized_keys",
                stdout,
                stderr,
                StandardCharsets.UTF_8);
        Assertions.assertEquals(super.publicKey(), stdout.toString());
        Assertions.assertEquals(
                "", stderr.toString());
    }
}
