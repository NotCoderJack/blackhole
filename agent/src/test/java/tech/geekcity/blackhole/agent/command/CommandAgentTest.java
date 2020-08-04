package tech.geekcity.blackhole.agent.command;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.agent.proto.CommandBox;
import tech.geekcity.blackhole.agent.proto.CommandResult;

import java.io.IOException;

public class CommandAgentTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgentTest.class.getName());

    @Test
    public void test() throws IOException, InterruptedException {
        try (CommandAgent commandAgent = CommandAgent.Builder.newInstance()
                .port(8980)
                .build()) {
            commandAgent.open();
            commandAgent.run();
            runClient();
//            commandAgent.blockUntilShutdown();
        }
    }

    public void runClient() {
        try (CommandClient commandClient = CommandClient.Builder.newInstance()
                .host("localhost")
                .port(8980)
                .build()) {
            commandClient.open();
            CommandResult commandResult = commandClient.run(CommandBox.newBuilder()
                    .setExecutor("sleep")
                    .addArguments("30")
                    .build());
            LOGGER.info("exitValue: {}", commandResult.getExitValue());
            LOGGER.info("standardOutput: {}", commandResult.getStandardOutput());
            LOGGER.info("errorOutput: {}", commandResult.getErrorOutput());
            LOGGER.info("exceptionStackTrace: {}", commandResult.getExceptionStackTrace());
        }
    }
}
