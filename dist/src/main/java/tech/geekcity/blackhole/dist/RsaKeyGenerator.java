package tech.geekcity.blackhole.dist;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import tech.geekcity.blackhole.ssh.RsaKeyPairGenerator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "rsaKeyGenerator",
        mixinStandardHelpOptions = true,
        description = "rsa key generator")
public class RsaKeyGenerator implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RsaKeyGenerator.class);
    @CommandLine.Option(names = {"--keySize"}, description = "rsa key size")
    private int keySize;
    @CommandLine.Option(names = {"--user"}, description = "user description")
    private String user;
    @CommandLine.Option(names = {"--idRsaFile"}, description = "path for id_rsa file")
    private File idRsaFile;
    @CommandLine.Option(names = {"--idRsaPubFile"}, description = "path for id_rsa.pub file")
    private File idRsaPubFile;
    @CommandLine.Option(names = {"--overwrite"}, description = "overwrite existing file", defaultValue = "false")
    private boolean overwriteExistingFile;

    @Override
    public Integer call() throws IOException {
        try (RsaKeyPairGenerator rsaKeyPairGenerator = RsaKeyPairGenerator.Builder.newInstance()
                .user("ben.wangz@some.net.com")
                .build()) {
            rsaKeyPairGenerator.open();
            // TODO check exists
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
            FileUtils.writeStringToFile(idRsaFile, rsaKeyPairGenerator.privateKeyAsString());
            FileUtils.writeStringToFile(idRsaPubFile, rsaKeyPairGenerator.publicKeyAsString());
            return 0;
        }
    }
}
