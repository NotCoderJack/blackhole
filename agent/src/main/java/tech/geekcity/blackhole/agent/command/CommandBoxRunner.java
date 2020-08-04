package tech.geekcity.blackhole.agent.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.agent.proto.CommandBox;
import tech.geekcity.blackhole.agent.proto.CommandResult;
import tech.geekcity.blackhole.core.Runner;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@FreeBuilder
@JsonDeserialize(builder = CommandBoxRunner.Builder.class)
public abstract class CommandBoxRunner implements Runner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandBoxRunner.class);
    private transient OutputStream standardOutputStream;
    private transient OutputStream errorOutputStream;
    private transient CommandResult commandResult;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link CommandBoxRunner}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link CommandBoxRunner} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends CommandBoxRunner_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
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

        public CommandBoxRunner parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CommandBoxRunner.class);
        }
    }

    public abstract CommandBox commandBox();

    @Nullable
    public abstract OutputStream standardOutputStream();

    @Nullable
    public abstract OutputStream errorOutputStream();

    @Nullable
    public abstract Runner callback();

    @Override
    public void open() throws IOException {
        standardOutputStream = standardOutputStream();
        if (null == standardOutputStream) {
            standardOutputStream = new ByteArrayOutputStream();
        }
        errorOutputStream = errorOutputStream();
        if (null == errorOutputStream) {
            errorOutputStream = new ByteArrayOutputStream();
        }
    }

    @Override
    public void close() throws IOException {
        Runner callback = callback();
        if (null == callback) {
            return;
        }
        try (Runner runner = callback) {
            runner.open();
            runner.run();
        }
    }

    @Override
    public void run() {
        CommandBox commandBox = commandBox();
        CommandLine commandLine = new CommandLine(commandBox.getExecutor());
        commandBox.getArgumentsList()
                .forEach(commandLine::addArgument);
        LOGGER.info("running command: {}", StringUtils.join(commandLine.toStrings(), " "));
        // TODO support files
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(
                new PumpStreamHandler(standardOutputStream, errorOutputStream));
        try {
            int exitValue = executor.execute(commandLine);
            commandResult = CommandResult.newBuilder()
                    .setExitValue(exitValue)
                    .setStandardOutput(standardOutputStream.toString())
                    .setErrorOutput(errorOutputStream.toString())
                    .setExceptionStackTrace("")
                    .build();
        } catch (Exception e) {
            commandResult = CommandResult.newBuilder()
                    .setExitValue(-1)
                    .setStandardOutput(standardOutputStream.toString())
                    .setErrorOutput(errorOutputStream.toString())
                    .setExceptionStackTrace(ExceptionUtils.getStackTrace(e))
                    .build();
        }
    }

    public CommandResult commandResult() {
        return commandResult;
    }
}
