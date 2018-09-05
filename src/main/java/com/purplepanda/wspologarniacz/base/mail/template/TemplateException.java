package com.purplepanda.wspologarniacz.base.mail.template;

public class TemplateException extends RuntimeException {

    public TemplateException() {
        super("Could not use template");
    }
}
