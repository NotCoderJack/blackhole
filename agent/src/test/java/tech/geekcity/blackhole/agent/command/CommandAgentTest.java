package tech.geekcity.blackhole.agent.command;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.agent.command.grpc.CommandBox;
import tech.geekcity.blackhole.agent.command.grpc.CommandResult;
import tech.geekcity.blackhole.agent.util.SslFileGenerator;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CommandAgentTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgentTest.class.getName());

    @Test
    public void test() throws IOException, InterruptedException {
        String targetDirectory = ".runtime";
        int port = new Double(Math.random() * 10000).intValue() + 10000;
        try (SslFileGenerator sslFileGenerator = SslFileGenerator.Builder.newInstance()
                .scriptPath("./bin/generate_ssl_files.sh")
                .targetDirectoryPath(targetDirectory)
                .build()) {
            sslFileGenerator.open();
            sslFileGenerator.run();
        }
        try (CommandAgent commandAgent = CommandAgent.Builder.newInstance()
                .keyCertChainFilePath(String.format("%s/server.crt", targetDirectory))
                .keyFilePath(String.format("%s/server.pem", targetDirectory))
                .trustCertCollectionFilePath(String.format("%s/ca.crt", targetDirectory))
                .port(port)
                .build()) {
            commandAgent.open();
            commandAgent.run();
            runClient(targetDirectory, port);
            new Thread(() -> {
                try {
                    LOGGER.info("thread waiting 30 seconds");
                    TimeUnit.SECONDS.sleep(5);
                    LOGGER.info("thread stopping command dist");
                    commandAgent.stopGracefully();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            commandAgent.blockUntilShutdown();
        }
    }

    public void runClient(String targetDirectory, int port) throws SSLException {
        try (CommandClient commandClient = CommandClient.Builder.newInstance()
                .host("localhost")
                .port(port)
                .keyCertChainFilePath(String.format("%s/server.crt", targetDirectory))
                .keyFilePath(String.format("%s/server.pem", targetDirectory))
                .trustCertCollectionFilePath(String.format("%s/ca.crt", targetDirectory))
                .build()) {
            commandClient.open();
            verify(
                    commandClient.run(CommandBox.newBuilder()
                            .setExecutor("sleep")
                            .addArguments("10")
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
        }
    }

    private void verify(CommandResult commandResult, int expectedExitValue) {
        LOGGER.info("exitValue: {}", commandResult.getExitValue());
        LOGGER.info("standardOutput: {}", commandResult.getStandardOutput());
        LOGGER.info("errorOutput: {}", commandResult.getErrorOutput());
        LOGGER.info("exceptionStackTrace: {}", commandResult.getExceptionStackTrace());
        Assert.assertEquals(expectedExitValue, commandResult.getExitValue());
    }
}
