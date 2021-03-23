package tech.geekcity.blackhole.application.tool;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.geekcity.blackhole.lib.agent.command.api.CommandAgent;
import tech.geekcity.blackhole.lib.agent.command.CommandAgentDefault;
import tech.geekcity.blackhole.lib.agent.command.ssl.CommandAgentSsl;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "commandAgentRunner",
        mixinStandardHelpOptions = true,
        description = "command agent runner")
public class CommandAgentRunner implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgentRunner.class.getName());
    @CommandLine.Option(names = {"--keyCertChainFile"}, description = "keyCertChainFile for server")
    private File keyCertChainFile;
    @CommandLine.Option(names = {"--keyFile"}, description = "keyFile for server")
    private File keyFile;
    @CommandLine.Option(names = {"--trustCertCollectionFile"}, description = "trustCertCollectionFile for server")
    private File trustCertCollectionFile;
    @CommandLine.Option(names = {"--port"}, required = true, description = "server port")
    private int port;
    @CommandLine.Option(names = {"--ssl"}, defaultValue = "false", description = "communicate with ssl or not")
    private boolean ssl;

    @Override
    public Integer call() {
        try (CommandAgent commandAgent = commandAgent()) {
            commandAgent.configure();
            LOGGER.info("running commandAgent: {}", commandAgent.toJsonSilently());
            commandAgent.run();
            commandAgent.blockUntilShutdown();
            return 0;
        } catch (Exception e) {
            LOGGER.error(String.format("failed: %s", e.getMessage()), e);
            return -1;
        }
    }

    private CommandAgent commandAgent() {
        if (ssl) {
            Preconditions.checkNotNull(keyCertChainFile);
            Preconditions.checkNotNull(keyFile);
            Preconditions.checkNotNull(trustCertCollectionFile);
            return CommandAgentSsl.Builder.newInstance()
                    .keyCertChainFilePath(keyCertChainFile.getAbsolutePath())
                    .keyFilePath(keyFile.getAbsolutePath())
                    .trustCertCollectionFilePath(trustCertCollectionFile.getAbsolutePath())
                    .port(port)
                    .build();
        }
        return CommandAgentDefault.Builder.newInstance()
                .port(port)
                .build();

    }
}
