package tech.geekcity.blackhole.lib.agent.command;

import io.grpc.stub.StreamObserver;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandAgentGrpc;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandBox;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandResult;

import java.io.IOException;

public class CommandService extends CommandAgentGrpc.CommandAgentImplBase {
    @Override
    public void run(CommandBox commandBox, StreamObserver<CommandResult> responseObserver) {
        CommandBoxRunner commandBoxRunner = CommandBoxRunner.Builder.newInstance()
                .commandBox(commandBox)
                .build();
        try {
            commandBoxRunner.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        commandBoxRunner.run();
        responseObserver.onNext(commandBoxRunner.commandResult());
        responseObserver.onCompleted();
    }
}