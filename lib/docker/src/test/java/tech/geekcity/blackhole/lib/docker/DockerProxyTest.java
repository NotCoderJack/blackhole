package tech.geekcity.blackhole.lib.docker;

import com.github.dockerjava.api.model.Image;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.docker.util.DockerUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class DockerProxyTest {
    private transient DockerProxy dockerProxy;
    public static final String IMAGE_NAME = "test_base_docker";
    public static final String IMAGE_TAG = "v1.0";

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
    public void testApiVersion() {
        Assertions.assertTrue(dockerProxy.apiVersion().compareTo("1.39") >= 0);
    }

    @Test
    public void testBuildAndStart() throws IOException, InterruptedException {
        String content = IOUtils.toString(
                Objects.requireNonNull(
                        getClass().getClassLoader()
                                .getResourceAsStream("build_image.dockerfile")),
                StandardCharsets.UTF_8);
        LoggerFactory.getLogger(getClass()).debug(content);
        Path dockerFile = Files.createTempFile("build_image.", ".dockerfile");
        dockerFile.toFile().deleteOnExit();
        FileUtils.writeStringToFile(dockerFile.toFile(), content, StandardCharsets.UTF_8);
//        Path dockerDirectory = Files.createTempDirectory("docker");
        FileUtils.copyFile(dockerFile.toFile(),
                new File(String.format(
                        "%s/build_image.dockerfile",
                        dockerFile.toFile().getParentFile().getAbsolutePath())));
        String imageId = dockerProxy.buildImage(
                dockerFile.toFile(),
                // TODO see implementation of buildImage: baseDirectory bug
                null,
                IMAGE_NAME,
                IMAGE_TAG);
        Image image = dockerProxy.findImageByName(IMAGE_NAME, IMAGE_TAG);
        Assertions.assertNotNull(image);
        Assertions.assertTrue(
                image.getId()
                        .startsWith(DockerUtil.wrapIdWithSha256(imageId)));
//        FileUtils.deleteDirectory(dockerDirectory.toFile());
        String containerName = "test_container";
        dockerProxy.startContainer(
                String.format("%s:%s", IMAGE_NAME, IMAGE_TAG),
                containerName,
                ImmutableList.of("/bin/bash", "-c", "sleep 1d"));
        Assertions.assertTrue(dockerProxy.listContainer()
                .containsKey(String.format("/%s", containerName)));
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        dockerProxy.exec(
                containerName,
                stdout, stderr,
                "cat", "/tmp/build_image.dockerfile");
        Assertions.assertEquals(content, stdout.toString());
        Assertions.assertEquals("", stderr.toString());
        dockerProxy.stopContainer(containerName);
    }
}
