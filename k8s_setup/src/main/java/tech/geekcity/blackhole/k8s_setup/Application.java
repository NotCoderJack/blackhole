package tech.geekcity.blackhole.k8s_setup;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tech.geekcity.blackhole.render.RenderEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Override
    public void run(String... args) throws IOException {
        LOGGER.info("running with arguments: {}", String.join(" ", args));
        try (RenderEngine renderEngine = RenderEngine.Builder.newInstance()
                .templatePath(args.length == 0 ? "template" : args[0])
                .build()) {
            renderEngine.open();
            ByteArrayOutputStream byteArrayOutputStream = renderEngine.render(
                    ImmutableMap.<String, Object>builder()
                            .put("password", "123456")
                            .build(),
                    "setup_docker_environment.sh");
            LOGGER.info(byteArrayOutputStream.toString());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
