package tech.geekcity.blackhole.lib.agent.command;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandBox;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandResult;

import java.util.concurrent.TimeUnit;

public class CommandAgentDefaultTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgentDefaultTest.class.getName());
    private transient CommandAgent commandAgent;
    private transient CommandClient commandClient;

    @Before
    public void setUp() throws Exception {
        int port = new Double(Math.random() * 10000).intValue() + 10000;
        commandAgent = CommandAgentDefault.Builder.newInstance()
                .port(port)
                .build();
        commandAgent.open();
        commandAgent.run();
        commandClient = CommandClientDefault.Builder.newInstance()
                .host("localhost")
                .port(port)
                .build();
        commandClient.open();
    }

    @After
    public void tearDown() throws Exception {
        if (null != commandAgent) {
            commandAgent.close();
        }
        if (null != commandClient) {
            commandClient.close();
        }
    }

    @Test
    public void test() throws InterruptedException {
        verify(
                commandClient.run(CommandBox.newBuilder()
                        .setExecutor("sleep")
                        .addArguments("3")
                        .build()),
                0);
        verify(
                commandClient.run(CommandBox.newBuilder()
                        .setExecutor("cat")
                        .addArguments("/etc/hosts")
                        .build()),
                0);
        verify(
                commandClient.run(CommandBox.newBuilder()
                        .setExecutor("cat")
                        .addArguments("/etc/not_exists")
                        .build()),
                -1);
        new Thread(() -> {
            try {
                LOGGER.info("thread waiting 5 seconds");
                TimeUnit.SECONDS.sleep(5);
                LOGGER.info("thread stopping command tool");
                commandAgent.stopGracefully();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        commandAgent.blockUntilShutdown();
    }

    private void verify(CommandResult commandResult, int expectedExitValue) {
        LOGGER.info("exitValue: {}", commandResult.getExitValue());
        LOGGER.info("standardOutput: {}", commandResult.getStandardOutput());
        LOGGER.info("errorOutput: {}", commandResult.getErrorOutput());
        LOGGER.info("exceptionStackTrace: {}", commandResult.getExceptionStackTrace());
        Assert.assertEquals(expectedExitValue, commandResult.getExitValue());
    }
}
