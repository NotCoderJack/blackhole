package tech.geekcity.blackhole.dist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.geekcity.blackhole.agent.command.ssl.CommandAgentSsl;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "dist",
        mixinStandardHelpOptions = true,
        description = "dist")
public class EntryPoint implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class.getName());
    @CommandLine.Option(names = {"--keyCertChainFile"}, description = "keyCertChainFile for server")
    private File keyCertChainFile;
    @CommandLine.Option(names = {"--keyFile"}, description = "keyFile for server")
    private File keyFile;
    @CommandLine.Option(names = {"--trustCertCollectionFile"}, description = "trustCertCollectionFile for server")
    private File trustCertCollectionFile;
    @CommandLine.Option(names = {"--port"}, description = "server port")
    private int port;
    // TODO support non ssl
    @CommandLine.Option(names = {"--ssl"}, defaultValue = "false", description = "communicate with ssl or not")
    private boolean ssl;

    @Override
    public Integer call() {
        try (CommandAgentSsl commandAgentSsl = CommandAgentSsl.Builder.newInstance()
                .keyCertChainFilePath(keyCertChainFile.getAbsolutePath())
                .keyFilePath(keyFile.getAbsolutePath())
                .trustCertCollectionFilePath(trustCertCollectionFile.getAbsolutePath())
                .port(port)
                .build()) {
            commandAgentSsl.open();
            LOGGER.info("running commandAgentSsl: {}", commandAgentSsl.toBuilder().toJsonSilently());
            commandAgentSsl.run();
            commandAgentSsl.blockUntilShutdown();
            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new EntryPoint()).execute(args);
        System.exit(exitCode);
    }
}
