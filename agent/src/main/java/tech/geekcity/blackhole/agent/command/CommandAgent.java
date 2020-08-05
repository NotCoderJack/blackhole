package tech.geekcity.blackhole.agent.command;

import tech.geekcity.blackhole.core.Runner;

public interface CommandAgent extends Runner {
    void blockUntilShutdown() throws InterruptedException;

    void stopGracefully();

    String toJsonSilently();
}
