package tech.geekcity.blackhole.lib.agent.command;

import tech.geekcity.blackhole.lib.core.Runner;

public interface CommandAgent extends Runner {
    void blockUntilShutdown() throws InterruptedException;

    void stopGracefully();

    String toJsonSilently();
}
