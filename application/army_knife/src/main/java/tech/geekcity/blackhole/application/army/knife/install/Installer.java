package tech.geekcity.blackhole.application.army.knife.install;

import org.apache.commons.io.FileUtils;
import tech.geekcity.blackhole.application.army.knife.ssh.SshConnector;
import tech.geekcity.blackhole.lib.core.Configurable;
import tech.geekcity.blackhole.lib.core.ResourceManager;
import tech.geekcity.blackhole.lib.ssh.SimpleScp;
import tech.geekcity.blackhole.lib.ssh.SshCommander;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public abstract class Installer implements Configurable {
    private transient boolean configured = false;
    private transient SshConnector sshConnector;
    private transient SshCommander sshCommander;
    private transient SimpleScp simpleScp;
    private transient boolean installed;

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
        installed = false;
    }

    @Override
    public void close() throws IOException {
        // sshCommander and simpleScp will be closed by sshConnector
        if (null != sshConnector) {
            sshConnector.close();
            sshConnector = null;
        }
        if (installed) {
            installed = false;
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

    public void install() throws IOException {
        if (installed) {
            return;
        }
        doInstall();
        installed = true;
    }

    public boolean installed() {
        return installed;
    }

    protected abstract void doInstall() throws IOException;

    protected void runSingleCommand(String command) {
        try {
            sshCommander.runAndCheckReturn(command, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String contentFromFileOrResource(
            @Nullable String filePath,
            @Nonnull String resourcePath
    ) throws IOException {
        return ResourceManager.contentFromFileOrResource(this.getClass(), filePath, resourcePath);
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
}
