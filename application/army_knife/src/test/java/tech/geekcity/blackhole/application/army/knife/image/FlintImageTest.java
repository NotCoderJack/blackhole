package tech.geekcity.blackhole.application.army.knife.image;

import com.github.dockerjava.api.model.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.geekcity.blackhole.lib.docker.DockerProxy;
import tech.geekcity.blackhole.lib.docker.util.DockerUtil;

import java.io.IOException;

public class FlintImageTest {
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
        final String tag = "v_test_1.0";
        try (FlintImage flintImage = FlintImage.Builder.newInstance()
                .tag(tag)
                .build()) {
            flintImage.configure();
            String imageId = flintImage.buildImage();
            Image image = dockerProxy.findImageByName(FlintImage.IMAGE_NAME, tag);
            Assertions.assertNotNull(image);
            Assertions.assertTrue(
                    image.getId()
                            .startsWith(DockerUtil.wrapIdWithSha256(imageId)));
        }
    }
}
