package tech.geekcity.blackhole.application.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "rsaKeyGenerator",
        mixinStandardHelpOptions = true,
        description = "rsa key generator")
public class Deployer implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Deployer.class);
    @CommandLine.Option(names = {"--idRsaFile"}, description = "path for id_rsa file", required = true)
    private File idRsaFile;
    @CommandLine.Option(names = {"--deployHost"}, description = "deploy host", defaultValue = "localhost")
    private String deployHost;
    @CommandLine.Option(names = {"--sshPort"}, description = "ssh port of deploy host", defaultValue = "22")
    private int sshPort;
    @CommandLine.Option(names = {"--sshUser"}, description = "ssh user", defaultValue = "root")
    private int sshUser;
    @CommandLine.Option(
            names = {"--gitRemote"},
            description = "git remote to pull codes",
            defaultValue = "git@github.com:ben-wangz/blackhole.git")
    private String gitRemote;
    @CommandLine.Option(
            names = {"--checkoutCommit"},
            description = "commit/tag/branch to checkout",
            defaultValue = "master")
    private String checkoutCommit;

    @Override
    public Integer call() throws IOException {
        // git pull from git hub with gitRemote and checkout with checkoutCommit
        // build docker image for secure_deploy
        // run secure_deploy
        return 0;
    }
}
