package tech.geekcity.blackhole.core;

import java.io.Closeable;
import java.io.IOException;

public interface Configurable extends Closeable {
    void open() throws IOException;
}
