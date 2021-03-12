package tech.geekcity.blackhole.application.army.knife.install;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.ssh.SimpleScp;
import tech.geekcity.blackhole.lib.ssh.SshCommander;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

public abstract class Installer implements Configurable {
    private transient boolean configured = false;
    private transient SshConnector sshConnector;
    private transient SshCommander sshCommander;
    private transient SimpleScp simpleScp;

    public abstract SshConnector sshConnector();

    @Override
    public void configure() throws IOException {
        if (configured) {
            return;
        }
        sshConnector = sshConnector();
        sshConnector.configure();
        sshCommander = sshConnector.validateSshCommander();
        simpleScp = sshConnector.validateSimpleScp();
        configured = true;
    }

    @Override
    public void close() throws IOException {
        // sshCommander and simpleScp will be closed by sshConnector
        if (null != sshConnector) {
            sshConnector.close();
            sshConnector = null;
        }
        if (configured) {
            configured = false;
        }
    }

    public SshCommander sshCommander() {
        return sshCommander;
    }

    public SimpleScp simpleScp() {
        return simpleScp;
    }

    public abstract void install() throws IOException;

    protected void runSingleCommand(String command) {
        try {
            int returnCode = sshCommander.run(command);
            if (0 != returnCode) {
                throw runCommandErrorException(
                        command,
                        new IOException(String.format("returnCode(%s) != 0", returnCode)),
                        sshCommander.standardOutput().toString(),
                        sshCommander.errorOutput().toString()
                );
            }
        } catch (IOException e) {
            throw runCommandErrorException(
                    command,
                    e,
                    sshCommander.standardOutput().toString(),
                    sshCommander.errorOutput().toString());
        }
    }

    protected String contentFromFileOrResource(
            @Nullable String filePath,
            @Nonnull String resourcePath
    ) throws IOException {
        if (null != filePath) {
            return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
        }
        return IOUtils.toString(
                Objects.requireNonNull(
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream(resourcePath)
                ));
    }

    protected void createTempFileAndUpload(
            @Nonnull String tempFilePrefix,
            @Nonnull String tempFileSuffix,
            @Nonnull String uploadContent,
            @Nonnull String uploadTarget
    ) throws IOException {
        File dockerCeRepoFile = File.createTempFile(tempFilePrefix, tempFileSuffix);
        FileUtils.writeStringToFile(dockerCeRepoFile, uploadContent, StandardCharsets.UTF_8);
        simpleScp.upload(
                Collections.singletonList(
                        dockerCeRepoFile.getAbsolutePath()),
                uploadTarget);
        dockerCeRepoFile.delete();
    }

    private RuntimeException runCommandErrorException(
            String command,
            Exception cause,
            String standardOutput,
            String errorOutput) {
        return new RuntimeException(
                String.format("run command(%s) failed: %s %s %s",
                        command,
                        cause.getMessage(),
                        standardOutput,
                        errorOutput,
                        cause));
    }
}
