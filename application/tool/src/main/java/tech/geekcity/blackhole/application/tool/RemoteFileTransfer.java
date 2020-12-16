package tech.geekcity.blackhole.application.tool;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.geekcity.blackhole.lib.core.exception.NotSupportedException;
import tech.geekcity.blackhole.lib.ssh.RsaKeyPairWrap;
import tech.geekcity.blackhole.lib.ssh.SimpleScp;
import tech.geekcity.blackhole.lib.ssh.SshClientWrap;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "remoteFileTransfer",
        mixinStandardHelpOptions = true,
        description = "remote file transfer")
public class RemoteFileTransfer implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteFileTransfer.class);

    private enum TransferType {
        UPLOAD,
        DOWNLOAD,
    }

    @CommandLine.Option(names = {"--username"}, description = "username to login", required = true)
    private String username;
    @CommandLine.Option(names = {"--remoteHost"}, description = "remote host", required = true)
    private String remoteHost;
    @CommandLine.Option(names = {"--remotePort"}, description = "remote port", required = true)
    private int remotePort;
    @CommandLine.Option(names = {"--keyPairFile"}, description = "key pair serialized file", required = true)
    private File keyPairFile;
    @CommandLine.Option(names = {"--transferType"}, description = "upload|download", defaultValue = "upload")
    private String transferType;
    @CommandLine.Option(names = {"--localFile"}, description = "local file", required = true)
    private File[] localFiles;
    @CommandLine.Option(names = {"--remoteFile"}, description = "remote file", required = true)
    private File[] remoteFiles;

    @Override
    public Integer call() throws IOException, ClassNotFoundException, NotSupportedException {
        byte[] keyPairDataBytes = FileUtils.readFileToByteArray(keyPairFile);
        KeyPair keyPair = RsaKeyPairWrap.deserialize(keyPairDataBytes).keyPair();
        try (SimpleScp simpleScp = SimpleScp.Builder.newInstance()
                .sshClientWrap(SshClientWrap.Builder.newInstance()
                        .username(username)
                        .host(remoteHost)
                        .port(remotePort)
                        .keyPair(keyPair)
                        .build())
                .build()) {
            simpleScp.open();
            switch (TransferType.valueOf(transferType.toUpperCase())) {
                case UPLOAD:
                    Preconditions.checkArgument(
                            1 == remoteFiles.length,
                            "only one remote file is allowed in %s mode",
                            TransferType.UPLOAD);
                    simpleScp.upload(
                            Arrays.stream(localFiles)
                                    .map(File::getAbsolutePath)
                                    .collect(Collectors.toList()),
                            remoteFiles[0].getAbsolutePath());
                    break;
                case DOWNLOAD:
                    Preconditions.checkArgument(
                            1 == localFiles.length,
                            "only one local file is allowed in %s mode",
                            TransferType.DOWNLOAD);
                    simpleScp.download(
                            Arrays.stream(remoteFiles)
                                    .map(File::getAbsolutePath)
                                    .collect(Collectors.toList()),
                            localFiles[0].getAbsolutePath());
                default:
                    throw new NotSupportedException(String.format("not support transferType(%s)", transferType));
            }
        }
        return 0;
    }
}
