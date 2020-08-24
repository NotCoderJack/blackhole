package tech.geekcity.blackhole.ssh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.inferred.freebuilder.FreeBuilder;

import java.io.IOException;

@FreeBuilder
@JsonDeserialize(builder = SshCommander.Builder.class)
public abstract class SshCommander {
    /**
     * Returns a new {@link Builder} with the same property values as this {@link SshCommander}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link SshCommander} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends SshCommander_Builder {
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

        public SshCommander parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, SshCommander.class);
        }
    }
}
