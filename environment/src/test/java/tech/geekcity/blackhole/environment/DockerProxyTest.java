package tech.geekcity.blackhole.environment;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class DockerProxyTest {
    private DockerProxy dockerProxy;
    public static final String IMAGE_NAME = "test_base_docker";
    public static final String IMAGE_TAG = "v1.0";

    @BeforeEach
    void setUp() {
        dockerProxy = DockerProxy.Builder.newInstance()
                .build();
        dockerProxy.open();
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
    public void testBuildAndStart() throws IOException {
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
//        FileUtils.deleteDirectory(dockerDirectory.toFile());
        String expectImageName = String.format("%s:%s", IMAGE_NAME, IMAGE_TAG);
        Assertions.assertTrue(
                dockerProxy.listImageId()
                        .stream()
                        // format: "sha256:${imageId}..."
                        .anyMatch(id -> id.startsWith(imageId, 7)));
        Assertions.assertTrue(
                dockerProxy.listImage()
                        .stream()
                        .anyMatch(expectImageName::equals));
        dockerProxy.startContainer(
                expectImageName,
                "test_container",
                ImmutableList.of("/bin/bash", "-c", "sleep 1d"));
//        dockerProxy.stopContainer(expectImageName);
    }
}
