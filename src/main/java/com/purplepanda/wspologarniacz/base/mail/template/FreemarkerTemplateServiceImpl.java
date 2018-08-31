package com.purplepanda.wspologarniacz.base.mail.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
class FreemarkerTemplateServiceImpl implements TemplateService {

    private final Configuration configuration;

    @Autowired
    FreemarkerTemplateServiceImpl(@Value("${templates.root.dir:/templates}") String templatePackage) {
        configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setClassLoaderForTemplateLoading(
                FreemarkerTemplateServiceImpl.class.getClassLoader(), templatePackage);
    }

    @Override
    public String merge(String templateName, Map<String, Object> model) {
        final Template template = getTemplate(templateName)
                .orElseThrow(com.purplepanda.wspologarniacz.base.mail.template.TemplateException::new);
        try (final StringWriter writer = new StringWriter()) {
            log.debug("Merging '{}' template with {}", templateName, model);
            template.process(model, writer);
            return writer.toString();
        } catch (final TemplateException | IOException e) {
            log.error("Failed to process requested template - {}", templateName, e);
            throw new com.purplepanda.wspologarniacz.base.mail.template.TemplateException();
        }
    }

    private Optional<Template> getTemplate(final String templateName) {
        try {
            return Optional.of(configuration.getTemplate(templateName));
        } catch (final IOException e) {
            log.error("Failed to load requested template - {}", templateName, e);
            return Optional.empty();
        }
    }
}
