package tech.geekcity.blackhole.lib.agent.command;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.agent.command.api.CommandAgent;
import tech.geekcity.blackhole.lib.agent.command.api.CommandClient;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandBox;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandResult;
import tech.geekcity.blackhole.lib.agent.command.ssl.CommandAgentSsl;
import tech.geekcity.blackhole.lib.agent.command.ssl.CommandClientSsl;
import tech.geekcity.blackhole.lib.agent.util.SslFileGenerator;

import java.util.concurrent.TimeUnit;

public class CommandAgentSslTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgentSslTest.class.getName());
    private transient CommandAgent commandAgent;
    private transient CommandClient commandClient;

    @Before
    public void setUp() throws Exception {
        String targetDirectory = ".runtime";
        try (SslFileGenerator sslFileGenerator = SslFileGenerator.Builder.newInstance()
                .scriptPath("./bin/generate_ssl_files.sh")
                .targetDirectoryPath(targetDirectory)
                .build()) {
            sslFileGenerator.configure();
            sslFileGenerator.run();
        }
        int port = new Double(Math.random() * 10000).intValue() + 10000;
        commandAgent = CommandAgentSsl.Builder.newInstance()
                .keyCertChainFilePath(String.format("%s/server.crt", targetDirectory))
                .keyFilePath(String.format("%s/server.pem", targetDirectory))
                .trustCertCollectionFilePath(String.format("%s/ca.crt", targetDirectory))
                .port(port)
                .build();
        commandAgent.configure();
        commandAgent.run();
        commandClient = CommandClientSsl.Builder.newInstance()
                .keyCertChainFilePath(String.format("%s/client.crt", targetDirectory))
                .keyFilePath(String.format("%s/client.pem", targetDirectory))
                .trustCertCollectionFilePath(String.format("%s/ca.crt", targetDirectory))
                .host("localhost")
                .port(port)
                .build();
        commandClient.configure();
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
