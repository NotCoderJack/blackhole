package tech.geekcity.blackhole.lib.agent.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.core.Runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

@FreeBuilder
@JsonDeserialize(builder = SslFileGenerator.Builder.class)
public abstract class SslFileGenerator implements Runner {
    private static final Logger LOGGER = LoggerFactory.getLogger(SslFileGenerator.class);
    private transient CommandLine commandLine;
    private transient DefaultExecutor executor;
    private transient OutputStream standardOutputStream;
    private transient OutputStream errorOutputStream;

    /**
     * Returns a new {@link SslFileGenerator.Builder} with the same property values as this {@link SslFileGenerator}
     */
    public abstract SslFileGenerator.Builder toBuilder();

    /**
     * Builder of {@link SslFileGenerator} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SslFileGenerator_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static SslFileGenerator.Builder newInstance() {
            return new SslFileGenerator.Builder();
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public String toJsonSilently() {
            try {
                return objectMapper.writeValueAsString(build());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public SslFileGenerator parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SslFileGenerator.class);
        }
    }

    public abstract String scriptPath();

    public abstract String targetDirectoryPath();

    @Override
    public void configure() {
        String targetDirectoryPath = targetDirectoryPath();
        File targetDirectory = new File(targetDirectoryPath);
        if (!targetDirectory.exists()) {
            Preconditions.checkArgument(
                    targetDirectory.mkdirs(),
                    "create targetDirectory(%s) failed",
                    targetDirectory.getAbsolutePath());
        }
        commandLine = new CommandLine("bash");
        commandLine.addArgument(scriptPath());
        commandLine.addArgument(targetDirectoryPath);
        standardOutputStream = new ByteArrayOutputStream();
        errorOutputStream = new ByteArrayOutputStream();
        // TODO support files
        executor = new DefaultExecutor();
        executor.setStreamHandler(
                new PumpStreamHandler(standardOutputStream, errorOutputStream));
    }

    @Override
    public void run() {
        LOGGER.info("running command: {}", StringUtils.join(commandLine.toStrings(), " "));
        try {
            int exitValue = executor.execute(commandLine);
            LOGGER.info("exitValue: {}", exitValue);
            LOGGER.info("standardOutput: {}", standardOutputStream);
            LOGGER.info("errorOutput: {}", errorOutputStream);
        } catch (IOException e) {
            LOGGER.info("standardOutput: {}", standardOutputStream);
            LOGGER.info("errorOutput: {}", errorOutputStream);
            throw new RuntimeException(e);
        }
    }
}
