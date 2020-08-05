package tech.geekcity.blackhole.agent.command;

import tech.geekcity.blackhole.agent.command.grpc.CommandBox;
import tech.geekcity.blackhole.agent.command.grpc.CommandResult;
import tech.geekcity.blackhole.core.Configurable;

public interface CommandClient extends Configurable {
    CommandResult run(CommandBox commandBox);
}
