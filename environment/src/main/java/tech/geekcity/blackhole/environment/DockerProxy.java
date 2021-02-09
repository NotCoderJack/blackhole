package tech.geekcity.blackhole.environment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.inferred.freebuilder.FreeBuilder;

import java.io.Closeable;
import java.io.IOException;

@FreeBuilder
@JsonDeserialize(builder = DockerProxy.Builder.class)
public abstract class DockerProxy implements Closeable {
    private transient DockerClientConfig dockerClientConfig;
    private transient DockerHttpClient httpClient;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link DockerProxy}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link DockerProxy} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends DockerProxy_Builder {
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

        public DockerProxy parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, DockerProxy.class);
        }
    }

    public String apiVersion() {
        VersionCmd versionCmd = DockerClientImpl.getInstance(dockerClientConfig, httpClient)
                .versionCmd();
        Version version = versionCmd.exec();
        return version.getApiVersion();
    }

    public void open() {
        dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
    }

    @Override
    public void close() throws IOException {

    }
}
