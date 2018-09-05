package com.purplepanda.wspologarniacz.base.mail.template;

import java.util.Map;

public interface TemplateService {
    String merge(String templateName, Map<String, Object> model);
}
