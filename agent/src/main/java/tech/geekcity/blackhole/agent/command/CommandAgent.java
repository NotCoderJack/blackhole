/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.geekcity.blackhole.agent.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.inferred.freebuilder.FreeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.blackhole.agent.proto.CommandAgentGrpc;
import tech.geekcity.blackhole.agent.proto.CommandBox;
import tech.geekcity.blackhole.agent.proto.CommandResult;
import tech.geekcity.blackhole.core.Runner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@FreeBuilder
@JsonDeserialize(builder = CommandAgent.Builder.class)
public abstract class CommandAgent implements Runner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandAgent.class.getName());
    private transient Server server;

    public static class CommandService extends CommandAgentGrpc.CommandAgentImplBase {
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

    /**
     * Returns a new {@link Builder} with the same property values as this {@link CommandAgent}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link CommandAgent} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends CommandAgent_Builder {
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

        public CommandAgent parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, CommandAgent.class);
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
        if (server.isShutdown()) {
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

    public void blockUntilShutdown() throws InterruptedException {
        if (null != server) {
            server.awaitTermination();
        }
    }

    public void stopGracefully() {
        try {
            close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
