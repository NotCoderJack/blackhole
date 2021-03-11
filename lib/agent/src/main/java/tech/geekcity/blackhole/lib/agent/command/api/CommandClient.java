package tech.geekcity.blackhole.lib.agent.command.api;

import tech.geekcity.blackhole.lib.agent.command.grpc.CommandBox;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandResult;
import tech.geekcity.blackhole.lib.core.Configurable;

public interface CommandClient extends Configurable {
    CommandResult run(CommandBox commandBox);
}
