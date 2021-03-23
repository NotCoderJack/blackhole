package tech.geekcity.blackhole.lib.ssh.wrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RsaKeyPairWrapTest {
    private transient RsaKeyPairWrap rsaKeyPairWrap;
    private transient KeyPair keyPair;

    @BeforeEach
    void setUp() throws IOException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        keyPair = keyPairGenerator.generateKeyPair();
        rsaKeyPairWrap = RsaKeyPairWrap.Builder.newInstance()
                .parseFromKeyPair(keyPair);
    }

    @Test
    void testKeyPair() {
        Assertions.assertEquals(keyPair.getPrivate(), rsaKeyPairWrap.keyPair().getPrivate());
        Assertions.assertEquals(keyPair.getPublic(), rsaKeyPairWrap.keyPair().getPublic());
    }

    @Test
    void testGenerate() throws IOException, NoSuchAlgorithmException {
        RsaKeyPairWrap generated = RsaKeyPairWrap.Builder.newInstance()
                .generate(2048);
        // TODO validation?
        Assertions.assertNotNull(generated);
    }

    @Test
    void testPrivateKey() {
        Assertions.assertEquals(keyPair.getPrivate(), rsaKeyPairWrap.privateKey());
    }

    @Test
    void testPublicKey() {
        Assertions.assertEquals(keyPair.getPublic(), rsaKeyPairWrap.publicKey());
    }

    @Test
    void testJson() throws IOException {
        Assertions.assertEquals(
                rsaKeyPairWrap,
                RsaKeyPairWrap.Builder.newInstance()
                        .parseFromJson(rsaKeyPairWrap.toBuilder().toJsonSilently()));
    }
}
