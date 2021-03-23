package tech.geekcity.blackhole.lib.docker.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public class DockerUtil {
    public static String wrapIdWithSha256(@Nonnull String id) {
        Preconditions.checkNotNull(id, "id cannot be null");
        // id format: "sha256:${imageId}..."
        return "sha256:" + id;
    }

    public static String camelToSnake(String camel) {
        return camel.replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .toLowerCase();
    }
}
