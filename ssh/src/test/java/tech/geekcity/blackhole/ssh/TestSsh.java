package tech.geekcity.blackhole.ssh;

import org.apache.commons.io.FileUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.EnumSet;

public class TestSsh {
    @Test
    public void test() throws IOException, ClassNotFoundException {
        byte[] keyPairDataBytes = FileUtils.readFileToByteArray(new File("../dist/dim/sshd/id_rsa.key.pair"));
        KeyPair keyPair = RsaKeyPairWrap.deserialize(keyPairDataBytes).keyPair();
        SshClient client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(new DefaultKnownHostsServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE));
        client.start();
        try (ClientSession session = client.connect("root", "localhost", 2222)
                .verify()
                .getSession()) {
            session.addPublicKeyIdentity(keyPair);
            session.auth().verify();
            try (ClientChannel channel = session.createExecChannel("hostname")) {
                channel.setOut(System.out);
                channel.setErr(System.err);
                channel.open().verify();
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);
            }
        }
        client.stop();
        client.close();
    }
}
