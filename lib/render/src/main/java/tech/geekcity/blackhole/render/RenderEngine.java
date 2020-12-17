package tech.geekcity.blackhole.render;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.inferred.freebuilder.FreeBuilder;
import tech.geekcity.blackhole.lib.core.Configurable;

import java.io.*;
import java.util.Map;

@FreeBuilder
@JsonDeserialize(builder = RenderEngine.Builder.class)
public abstract class RenderEngine implements Configurable {
    private transient Configuration configuration;

    /**
     * Returns a new {@link Builder} with the same property values as this {@link RenderEngine}
     */
    public abstract Builder toBuilder();

    /**
     * Builder of {@link RenderEngine} instances
     * auto generated builder className which cannot be modified
     */
    public static class Builder extends RenderEngine_Builder {
        private ObjectMapper objectMapper = new ObjectMapper();

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder() {
            templatePath("./template");
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

        public RenderEngine parseFromJson(String json) throws IOException {
            return objectMapper.readValue(json, RenderEngine.class);
        }
    }

    public abstract String templatePath();

    @Override
    public void open() throws IOException {
        configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File(templatePath()));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    @Override
    public void close() {

    }

    public ByteArrayOutputStream render(Map<String, Object> data, String filename) {
        try {
            Template template = configuration.getTemplate(filename);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            template.process(
                    ImmutableMap.builder()
                            .put("__DOLLAR__", "$")
                            .putAll(data)
                            .build(),
                    new OutputStreamWriter(outputStream));
            return outputStream;
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
