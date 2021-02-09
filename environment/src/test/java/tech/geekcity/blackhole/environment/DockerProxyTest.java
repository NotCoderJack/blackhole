package tech.geekcity.blackhole.environment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class DockerProxyTest {
    @Test
    public void testApiVersion() {
        try (DockerProxy dockerProxy = DockerProxy.Builder.newInstance()
                .build()) {
            dockerProxy.open();
            Assertions.assertTrue(dockerProxy.apiVersion().compareTo("1.39") >= 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
