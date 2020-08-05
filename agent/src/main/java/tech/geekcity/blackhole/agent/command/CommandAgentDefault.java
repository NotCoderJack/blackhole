package tech.geekcity.blackhole.agent.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@FreeBuilder
@JsonDeserialize(builder = CommandAgentDefault.Builder.class)
public abstract class CommandAgentDefault implements CommandAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgentDefault.class.getName());
    private transient Server server;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link CommandAgentDefault}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link CommandAgentDefault} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends CommandAgentDefault_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
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

        public CommandAgentDefault parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CommandAgentDefault.class);
        }
    }

    public abstract int port();

    @Override
    public void open() {
        server = ServerBuilder.forPort(port())
                .addService(new CommandService())
                .build();
    }

    @Override
    public void close() throws IOException {
        if (null == server) {
            return;
        }
        if (server.isTerminated()) {
            return;
        }
        if (!server.isShutdown()) {
            server.shutdown();
            try {
                server.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public void run() {
        LOGGER.info("Server started, listening on {}", port());
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
                    try {
                        close();
                        System.err.println("*** shutting down gRPC server since JVM is shutting down");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    @Override
    public void blockUntilShutdown() throws InterruptedException {
        if (null != server) {
            server.awaitTermination();
        }
    }

    @Override
    public void stopGracefully() {
        try {
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
