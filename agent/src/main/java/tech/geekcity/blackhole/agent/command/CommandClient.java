package tech.geekcity.blackhole.agent.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.agent.proto.CommandAgentGrpc;
import tech.geekcity.blackhole.agent.proto.CommandBox;
import tech.geekcity.blackhole.agent.proto.CommandResult;
import tech.geekcity.blackhole.core.Configurable;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.io.File;
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

    public abstract String keyCertChainFilePath();

    public abstract String keyFilePath();

    @Nullable
    public abstract String trustCertCollectionFilePath();

    @Override
    public void open() throws SSLException {
        String host = host();
        int port = port();
        SslContext sslContext = sslContextForClient();
        channel = NettyChannelBuilder.forAddress(host, port)
                .sslContext(sslContext)
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

    public CommandResult run(CommandBox commandBox) {
        return commandAgentBlockingStub.run(commandBox);
    }

    private SslContext sslContextForClient() throws SSLException {
        String keyCertChainFilePath = keyCertChainFilePath();
        String keyFilePath = keyFilePath();
        String trustCertCollectionFilePath = trustCertCollectionFilePath();
        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient()
                .keyManager(new File(keyCertChainFilePath), new File(keyFilePath));
        if (null != trustCertCollectionFilePath) {
            sslContextBuilder.trustManager(new File(trustCertCollectionFilePath));
        }
        return sslContextBuilder.build();
    }
}
