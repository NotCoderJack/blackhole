package tech.geekcity.blackhole.agent;

import tech.geekcity.blackhole.agent.command.CommandAgent;

import java.io.IOException;

public class EntryPoint {
    public static void main(String[] args) throws IOException, InterruptedException {
        try (CommandAgent commandAgent = CommandAgent.Builder.newInstance()
                .port(8980)
                .build()) {
            commandAgent.open();
            commandAgent.run();
            commandAgent.blockUntilShutdown();
        }
    }
}
