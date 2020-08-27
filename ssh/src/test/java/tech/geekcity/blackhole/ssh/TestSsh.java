package tech.geekcity.blackhole.ssh;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

public class TestSsh {
    @Test
    public void test() throws IOException, ClassNotFoundException {
        byte[] keyPairDataBytes = FileUtils.readFileToByteArray(new File("../dist/dim/sshd/id_rsa.key.pair"));
        KeyPair keyPair = RsaKeyPairWrap.deserialize(keyPairDataBytes).keyPair();

        try (SshCommander sshCommander = SshCommander.Builder.newInstance()
                .username("root")
                .host("localhost")
                .port(2222)
                .keyPair(keyPair)
                .standardOutput(System.out)
                .errorOutput(System.err)
                .build()) {
            sshCommander.open();
            sshCommander.run("hostname");
            sshCommander.run("hostname -i");
        }
    }
}
