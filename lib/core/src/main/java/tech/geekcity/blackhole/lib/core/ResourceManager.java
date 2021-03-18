package tech.geekcity.blackhole.lib.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResourceManager {
    @Nonnull
    public static String contentFromResource(@Nonnull Class callCalss, @Nonnull String resourcePath)
            throws IOException {
        return contentFromFileOrResource(callCalss, null, resourcePath);
    }

    @Nonnull
    public static String contentFromFileOrResource(
            @Nonnull Class callCalss,
            @Nullable String filePath,
            @Nonnull String resourcePath
    ) throws IOException {
        if (null != filePath) {
            return inputStreamToString(new FileInputStream(filePath));
        }
        return inputStreamToString(
                Objects.requireNonNull(
                        callCalss.getClassLoader()
                                .getResourceAsStream(resourcePath)
                ));
    }

    @Nonnull
    private static String inputStreamToString(@Nonnull InputStream inputStream) {
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }
}
