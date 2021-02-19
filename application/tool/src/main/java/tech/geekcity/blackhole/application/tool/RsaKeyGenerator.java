package tech.geekcity.blackhole.application.tool;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.geekcity.blackhole.lib.ssh.wrap.RsaKeyPairWrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "rsaKeyGenerator",
        mixinStandardHelpOptions = true,
        description = "rsa key generator")
public class RsaKeyGenerator implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RsaKeyGenerator.class);
    @CommandLine.Option(names = {"--keySize"}, description = "rsa key size", defaultValue = "2048")
    private int keySize;
    @CommandLine.Option(names = {"--idRsaFile"}, description = "path for id_rsa file", required = true)
    private File idRsaFile;
    @CommandLine.Option(names = {"--idRsaPubFile"}, description = "path for id_rsa.pub file", required = true)
    private File idRsaPubFile;
    @CommandLine.Option(names = {"--keyPairFile"}, description = "key pair serialized file", required = true)
    private File keyPairFile;
    @CommandLine.Option(names = {"--overwrite"}, description = "overwrite existing file", defaultValue = "false")
    private boolean overwriteExistingFile;
    @CommandLine.Option(names = {"--existsDoNothing"}, description = "overwrite existing file", defaultValue = "true")
    private boolean existsDoNothing;
    @CommandLine.Option(names = {"--user"}, description = "user description", defaultValue = "auto-gen-user@blackhole")
    private String user;

    @Override
    public Integer call() throws IOException, NoSuchAlgorithmException {
        // TODO check exists
        if (idRsaFile.exists() && idRsaPubFile.exists() && existsDoNothing) {
            LOGGER.info(
                    "DO NOTHING: idRsaFile({}) and idRsaPubFile({}) exists, existsDoNothing({})",
                    idRsaFile.getAbsolutePath(),
                    idRsaPubFile.getAbsolutePath(),
                    existsDoNothing
            );
            return 0;
        }
        if (!overwriteExistingFile && idRsaFile.exists()) {
            LOGGER.error(
                    "idRsaFile({}) exists! use --overwrite true to overwrite", idRsaFile.getAbsolutePath());
            return -1;
        }
        if (!overwriteExistingFile && idRsaPubFile.exists()) {
            LOGGER.error(
                    "idRsaPubFile({}) exists! use --overwrite true to overwrite", idRsaPubFile.getAbsolutePath());
            return -2;
        }
        RsaKeyPairWrap rsaKeyPairWrap = RsaKeyPairWrap.Builder.newInstance()
                .generate(keySize);
        FileUtils.writeStringToFile(idRsaFile, rsaKeyPairWrap.privateKeyAsString());
        FileUtils.writeStringToFile(idRsaPubFile, rsaKeyPairWrap.publicKeyAsString(user));
        FileUtils.writeByteArrayToFile(keyPairFile, rsaKeyPairWrap.asBytes());
        Files.setPosixFilePermissions(
                idRsaFile.toPath(),
                ImmutableSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        return 0;
    }
}
