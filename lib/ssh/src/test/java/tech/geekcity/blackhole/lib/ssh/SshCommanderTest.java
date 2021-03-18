package tech.geekcity.blackhole.lib.ssh;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SshCommanderTest extends AbstractSshClientTest {
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
    }

    @AfterEach
    void tearDown() throws IOException {
        super.close();
    }

    @Test
    void testRun() throws IOException {
        sshCommander.runAndCheckReturn("cat /root/.ssh/authorized_keys");
        Assertions.assertEquals(super.publicKey(), stdout.toString());
        Assertions.assertEquals("", stderr.toString());
    }
}
