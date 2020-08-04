package tech.geekcity.blackhole.agent.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.agent.proto.CommandAgentGrpc;
import tech.geekcity.blackhole.agent.proto.CommandBox;
import tech.geekcity.blackhole.agent.proto.CommandResult;
import tech.geekcity.blackhole.core.Configurable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@FreeBuilder
@JsonDeserialize(builder = CommandClient.Builder.class)
public abstract class CommandClient implements Configurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandClient.class.getName());
    private transient CommandAgentGrpc.CommandAgentBlockingStub commandAgentBlockingStub;
    private transient ManagedChannel channel;

    /**
     * Returns a new {@link CommandClient.Builder} with the same property values as this {@link CommandClient}
     */
    public abstract CommandClient.Builder toBuilder();

    /**
     * Builder of {@link CommandClient} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends CommandClient_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static CommandClient.Builder newInstance() {
            return new CommandClient.Builder();
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

        public CommandClient parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CommandClient.class);
        }
    }

    public abstract String host();

    public abstract int port();

    @Override
    public void open() {
        channel = ManagedChannelBuilder
                .forTarget(String.format("%s:%s", host(), port()))
                .usePlaintext()
                .build();
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
        if (channel.isShutdown()) {
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public CommandResult run(CommandBox commandBox) {
        return commandAgentBlockingStub.run(commandBox);
    }
}
