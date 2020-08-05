package tech.geekcity.blackhole.core;

import java.io.Closeable;
import java.io.IOException;

public interface Configurable extends Closeable {
    default void open() throws IOException {
    }

    @Override
    default void close() throws IOException {

    }
}
