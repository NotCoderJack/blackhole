package tech.geekcity.blackhole.lib.ssh;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleScpTest extends AbstractSshClientTest {
    private transient SimpleScp simpleScp;
    private transient List<File> fileList;
    private transient File tempFileDirectory;

    @BeforeEach
    void setUp() throws IOException {
        super.configure();
        simpleScp = SimpleScp.Builder.newInstance()
                .sshClientWrap(super.sshClientWrap())
                .build();
        simpleScp.configure();
        fileList = IntStream.range(0, 3)
                .mapToObj(index -> {
                    try {
                        return createRandomTempFile();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }).collect(Collectors.toList());
        tempFileDirectory = new File("/tmp/blackhole/test/scp");
        tempFileDirectory.mkdirs();
    }

    @AfterEach
    void tearDown() throws IOException {
        super.close();
        if (null != simpleScp) {
            simpleScp.close();
        }
        if (null != tempFileDirectory) {
            FileUtils.deleteQuietly(tempFileDirectory);
        }
    }

    @Test
    void testUploadAndDownload() throws IOException {
        String remoteTempDirectory = "/tmp";
        simpleScp.upload(
                fileList.stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toList()),
                remoteTempDirectory);
        simpleScp.download(
                fileList.stream()
                        .map(file -> String.format("%s/%s", remoteTempDirectory, file.getName()))
                        .collect(Collectors.toList()),
                tempFileDirectory.getAbsolutePath());
        fileList.forEach(file -> {
            try {
                Assertions.assertEquals(
                        FileUtils.readFileToString(file, StandardCharsets.UTF_8),
                        FileUtils.readFileToString(
                                new File(String.format("%s/%s", tempFileDirectory.getAbsolutePath(), file.getName())),
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
