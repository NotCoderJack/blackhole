package tech.geekcity.blackhole.lib.core;

import java.io.Closeable;
import java.io.IOException;

public interface Configurable extends Closeable {
    default void configure() throws IOException {
    }

    @Override
    default void close() throws IOException {

    }
}
