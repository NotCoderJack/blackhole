package tech.geekcity.blackhole.k8s_setup.configuration;

import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import tech.geekcity.blackhole.core.Configurable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class RenderEngine implements Configurable, Runnable {
    private String templatePath;
    private Configuration configuration;

    @Override
    public void open() throws IOException {
        configuration = new Configuration();
        configuration.setDirectoryForTemplateLoading(new File(templatePath));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    @Override
    public void close() {

    }

    public RenderEngine(String templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public void run() {
        try {
            Template template = configuration.getTemplate("setup_docker_environment.sh");
            Writer out = new OutputStreamWriter(System.out);
            template.process(
                    ImmutableMap.builder()
                            .put("__DOLLAR__", "$")
//                            .put("dockerCeRepo", "http://")
                            .build(),
                    out);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
