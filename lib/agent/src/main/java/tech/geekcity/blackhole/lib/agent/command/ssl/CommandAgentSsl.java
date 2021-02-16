package tech.geekcity.blackhole.lib.agent.command.ssl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.lib.agent.command.CommandAgent;
import tech.geekcity.blackhole.lib.agent.command.CommandService;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@FreeBuilder
@JsonDeserialize(builder = CommandAgentSsl.Builder.class)
public abstract class CommandAgentSsl implements CommandAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgentSsl.class.getName());
    private transient Server server;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link CommandAgentSsl}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link CommandAgentSsl} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends CommandAgentSsl_Builder {
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

        public CommandAgentSsl parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CommandAgentSsl.class);
        }
    }

    public abstract int port();

    public abstract String keyCertChainFilePath();

    public abstract String keyFilePath();

    @Nullable
    public abstract String trustCertCollectionFilePath();

    @Override
    public void configure() throws SSLException {
        server = NettyServerBuilder.forAddress(new InetSocketAddress(port()))
                .addService(new CommandService())
                .sslContext(sslContextForServer())
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

    @Override
    public String toJsonSilently() {
        return this.toBuilder().toJsonSilently();
    }

    private SslContext sslContextForServer() throws SSLException {
        String keyCertChainFilePath = keyCertChainFilePath();
        String keyFilePath = keyFilePath();
        String trustCertCollectionFilePath = trustCertCollectionFilePath();
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(
                new File(keyCertChainFilePath),
                new File(keyFilePath));
        if (null != trustCertCollectionFilePath) {
            sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder, SslProvider.OPENSSL)
                .build();
    }
}
