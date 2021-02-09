package tech.geekcity.blackhole.environment;

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
    public void testBuildImage() throws IOException {
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
        // TODO see implementation of buildImage: baseDirectory bug
        dockerProxy.buildImage(dockerFile.toFile(), null, "test_base_docker", "v0.1");
//        FileUtils.deleteDirectory(dockerDirectory.toFile());
    }
}
