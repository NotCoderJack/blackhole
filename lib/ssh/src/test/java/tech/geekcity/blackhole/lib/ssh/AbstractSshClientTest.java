package tech.geekcity.blackhole.lib.ssh;

import org.apache.commons.io.FileUtils;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.ssh.image.SshImage;
import tech.geekcity.blackhole.lib.ssh.wrap.RsaKeyPairWrap;
import tech.geekcity.blackhole.lib.ssh.wrap.SshClientWrap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSshClientTest implements Configurable {
    private static final String USERNAME = "root";
    private static final String TAG = "v_test_1.0";
    private static final String CONTAINER_NAME = "test_ssh_container";
    private transient RsaKeyPairWrap rsaKeyPairWrap;
    private transient SshClientWrap sshClientWrap;
    private transient SshImage sshImage;
    private transient File authorizedKeysFile;
    private transient boolean configured = false;

    @Override
    public void configure() throws IOException {
        if (configured) {
            return;
        }
        String remoteHost = "localhost";
        int remotePort = 9022;
        try {
            rsaKeyPairWrap = RsaKeyPairWrap.Builder.newInstance()
                    .generate(2048);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(
                    String.format("generate rsa key pair failed: %s", e.getMessage()),
                    e);
        }
        authorizedKeysFile = createAuthorizedKeysTempFile(publicKey());
        startSshContainer(remotePort, authorizedKeysFile);
        sshClientWrap = SshClientWrap.Builder.newInstance()
                .username(USERNAME)
                .host(remoteHost)
                .port(remotePort)
                .rsaKeyPairWrap(rsaKeyPairWrap)
                .build();
        sshClientWrap.configure();
        configured = true;
    }

    public void close() throws IOException {
        if (null != sshClientWrap) {
            sshClientWrap.close();
            sshClientWrap = null;
        }
        if (null != authorizedKeysFile) {
            authorizedKeysFile.delete();
            authorizedKeysFile = null;
        }
        if (null != sshImage) {
            sshImage.stopContainer(CONTAINER_NAME);
            sshImage = null;
        }
        if (configured) {
            configured = false;
        }
    }

    protected String publicKey() {
        return rsaKeyPairWrap.publicKeyAsString(USERNAME);
    }

    protected SshClientWrap sshClientWrap() {
        return sshClientWrap;
    }

    private File createAuthorizedKeysTempFile(String publicKey) throws IOException {
        // /tmp is docker engine(docker desktop for mac os) default shared path
        File tempFileDirectory = new File("/tmp/blackhole/test");
        tempFileDirectory.mkdirs();
        File authorizedKeysFile = File.createTempFile(
                "authorized_keys.", ".tmp", tempFileDirectory);
        Files.setPosixFilePermissions(
                Paths.get(authorizedKeysFile.toURI()),
                PosixFilePermissions.fromString("rw-------"));
        FileUtils.writeStringToFile(authorizedKeysFile, publicKey, StandardCharsets.UTF_8);
        return authorizedKeysFile;
    }

    private void startSshContainer(int remotePort, File authorizedKeysFile) throws IOException {
        sshImage = SshImage.Builder.newInstance()
                .tag(TAG)
                .build();
        sshImage.configure();
        sshImage.buildImage();
        sshImage.startContainer(
                CONTAINER_NAME,
                authorizedKeysFile,
                remotePort);
        try {
            // TODO check container has already started
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
