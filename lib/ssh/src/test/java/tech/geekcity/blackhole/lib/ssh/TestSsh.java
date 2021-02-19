package tech.geekcity.blackhole.lib.ssh;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.geekcity.blackhole.lib.ssh.wrap.RsaKeyPairWrap;
import tech.geekcity.blackhole.lib.ssh.wrap.SshClientWrap;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestSsh {
    private static final String REMOTE_TEMP_DIRECTORY = "/tmp";
    private transient String localTempDirectory;
    private transient RsaKeyPairWrap rsaKeyPairWrap;

    @BeforeEach
    public void setUp() throws IOException, NoSuchAlgorithmException {
        localTempDirectory = System.getProperty("java.io.tmpdir");
        rsaKeyPairWrap = RsaKeyPairWrap.Builder.newInstance()
                .generate(2048);
    }

    @AfterEach
    public void tearDown() {
        localTempDirectory = null;
        rsaKeyPairWrap = null;
    }

    @Test
    public void testCommand() throws IOException {
        try (SshCommander sshCommander = SshCommander.Builder.newInstance()
                .sshClientWrap(SshClientWrap.Builder.newInstance()
                        .username("root")
                        .host("localhost")
                        .port(2222)
                        .rsaKeyPairWrap(rsaKeyPairWrap)
                        .build())
                .standardOutput(System.out)
                .errorOutput(System.err)
                .build()) {
            sshCommander.configure();
            sshCommander.run("hostname");
            sshCommander.run("hostname -i");
        }
    }

    @Test
    public void testCopy() throws IOException {
        List<File> fileList = IntStream.range(0, 3)
                .mapToObj(index -> {
                    try {
                        return createRandomTempFile();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).collect(Collectors.toList());
        try (SimpleScp simpleScp = SimpleScp.Builder.newInstance()
                .sshClientWrap(SshClientWrap.Builder.newInstance()
                        .username("root")
                        .host("localhost")
                        .port(2222)
                        .rsaKeyPairWrap(rsaKeyPairWrap)
                        .build())
                .build()) {
            simpleScp.configure();
            simpleScp.upload(
                    fileList.stream()
                            .map(File::getAbsolutePath)
                            .collect(Collectors.toList()),
                    REMOTE_TEMP_DIRECTORY);
            simpleScp.download(
                    fileList.stream()
                            .map(file -> String.format("%s/%s", REMOTE_TEMP_DIRECTORY, file.getName()))
                            .collect(Collectors.toList()),
                    localTempDirectory);
        }
        fileList.forEach(file ->
                new File(String.format("%s/%s", localTempDirectory, file.getName()))
                        .deleteOnExit());
        fileList.forEach(file -> {
            try {
                Assertions.assertEquals(
                        FileUtils.readFileToString(file, StandardCharsets.UTF_8),
                        FileUtils.readFileToString(
                                new File(String.format("%s/%s", localTempDirectory, file.getName())),
                                StandardCharsets.UTF_8
                        ));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private File createRandomTempFile() throws IOException {
        File randomTempFile = File.createTempFile("random_temp_", ".txt");
        randomTempFile.deleteOnExit();
        FileUtils.writeStringToFile(
                randomTempFile,
                RandomStringUtils.randomAlphanumeric(128),
                StandardCharsets.UTF_8);
        return randomTempFile;
    }
}
