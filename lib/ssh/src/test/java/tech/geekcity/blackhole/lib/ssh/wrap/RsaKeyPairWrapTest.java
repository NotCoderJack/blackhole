package tech.geekcity.blackhole.lib.ssh.wrap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RsaKeyPairWrapTest {
    private transient RsaKeyPairWrap rsaKeyPairWrap;

    @BeforeEach
    void setUp() throws IOException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        rsaKeyPairWrap = RsaKeyPairWrap.Builder.newInstance()
                .parseFromKeyPair(keyPair);
    }

    @Test
    void test() {
        System.out.println(rsaKeyPairWrap.toBuilder().toJsonSilently());
    }
}
