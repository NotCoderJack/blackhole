package tech.geekcity.blackhole.lib.ssh.image;

import com.github.dockerjava.api.model.Image;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.geekcity.blackhole.lib.docker.DockerProxy;
import tech.geekcity.blackhole.lib.docker.util.DockerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.TimeUnit;

public class SshImageTest {
    private static final String TAG = "v_test_1.0";
    private transient DockerProxy dockerProxy;

    @BeforeEach
    void setUp() {
        dockerProxy = DockerProxy.Builder.newInstance()
                .build();
        dockerProxy.configure();
    }

    @AfterEach
    void tearDown() throws IOException {
        dockerProxy.close();
    }

    @Test
    void testBuild() throws IOException {
        try (SshImage sshImage = SshImage.Builder.newInstance()
                .tag(TAG)
                .build()) {
            sshImage.configure();
            String imageId = sshImage.buildImage();
            Image image = dockerProxy.findImageByName(SshImage.IMAGE_NAME, TAG);
            Assertions.assertNotNull(image);
            Assertions.assertTrue(
                    image.getId()
                            .startsWith(DockerUtil.wrapIdWithSha256(imageId)));
        }
    }

    @Test
    void testContainer() throws IOException {
        // /tmp is docker engine(docker desktop for mac os) default shared path
        File tempFileDirectory = new File("/tmp/blackhole/test");
        File authorizedKeysFile = null;
        try (SshImage sshImage = SshImage.Builder.newInstance()
                .tag(TAG)
                .build()) {
            sshImage.configure();
            // build it first
            sshImage.buildImage();
            tempFileDirectory.mkdirs();
            authorizedKeysFile = File.createTempFile(
                    "authorized_keys.", ".tmp", tempFileDirectory);
            Files.setPosixFilePermissions(
                    Paths.get(authorizedKeysFile.toURI()),
                    PosixFilePermissions.fromString("rw-------"));
            FileUtils.writeStringToFile(authorizedKeysFile, "this is a test", StandardCharsets.UTF_8);
            String containerName = "test_ssh_container";
            sshImage.startContainer(
                    containerName,
                    authorizedKeysFile,
                    9022);
            // TODO test ssh instead of sleep
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sshImage.stopContainer(containerName);
        } finally {
            if (null != authorizedKeysFile) {
                authorizedKeysFile.delete();
            }
        }
    }
}
