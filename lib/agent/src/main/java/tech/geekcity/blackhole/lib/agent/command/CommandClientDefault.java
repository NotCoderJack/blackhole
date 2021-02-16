package tech.geekcity.blackhole.lib.agent.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandAgentGrpc;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandBox;
import tech.geekcity.blackhole.lib.agent.command.grpc.CommandResult;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@FreeBuilder
@JsonDeserialize(builder = CommandClientDefault.Builder.class)
public abstract class CommandClientDefault implements CommandClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandClientDefault.class.getName());
    private transient CommandAgentGrpc.CommandAgentBlockingStub commandAgentBlockingStub;
    private transient ManagedChannel channel;

    /**
     * Returns a new {@link CommandClientDefault.Builder} with the same property values as this {@link CommandClientDefault}
     */
    public abstract CommandClientDefault.Builder toBuilder();

    /**
     * Builder of {@link CommandClientDefault} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends CommandClientDefault_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static CommandClientDefault.Builder newInstance() {
            return new CommandClientDefault.Builder();
        }

        public String toJson() throws JsonProcessingException {
            return objectMapper.writeValueAsString(build());
        }

        public String toJsonSilently() {
            try {
                return objectMapper.writeValueAsString(build());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public CommandClientDefault parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CommandClientDefault.class);
        }
    }

    public abstract String host();

    public abstract int port();

    @Override
    public void configure() {
        String host = host();
        int port = port();
        channel = ManagedChannelBuilder.forTarget(String.format("%s:%s", host, port))
                .usePlaintext()
                .build();
        LOGGER.info("connecting to {}:{}", host, port);
        commandAgentBlockingStub = CommandAgentGrpc.newBlockingStub(channel);
    }

    @Override
    public void close() {
        if (null == channel) {
            return;
        }
        if (channel.isTerminated()) {
            return;
        }
        if (!channel.isShutdown()) {
            try {
                channel.shutdown();
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public CommandResult run(CommandBox commandBox) {
        return commandAgentBlockingStub.run(commandBox);
    }
}
