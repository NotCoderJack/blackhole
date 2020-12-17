package tech.geekcity.blackhole.application.tool;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.geekcity.blackhole.render.RenderEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "renderText",
        mixinStandardHelpOptions = true,
        description = "render text")
public class RenderText implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderText.class);
    @CommandLine.Option(names = {"--templateFile"}, description = "template file", required = true)
    private File templateFile;
    @CommandLine.Option(names = {"--propertyFile"}, description = "property file", required = true)
    private File propertyFile;
    @CommandLine.Option(names = {"--outputFile"}, description = "output file", required = true)
    private File outputFile;

    @Override
    public Integer call() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(propertyFile));
        try (RenderEngine renderEngine = RenderEngine.Builder.newInstance()
                .templatePath(templateFile.getParentFile().getAbsolutePath())
                .build()) {
            renderEngine.open();
            ByteArrayOutputStream byteArrayOutputStream = renderEngine.render(
                    properties.entrySet().stream().collect(Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            entry -> entry.getValue().toString()
                    )),
                    templateFile.getName());
            FileUtils.writeStringToFile(outputFile, byteArrayOutputStream.toString());
        }
        return 0;
    }
}
