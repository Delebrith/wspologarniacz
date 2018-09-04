package com.purplepanda.wspologarniacz.base.mail;

import com.purplepanda.wspologarniacz.base.mail.template.TemplateService;
import com.purplepanda.wspologarniacz.user.event.PasswordResetRequestEvent;
import com.purplepanda.wspologarniacz.user.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateService templateService;
    private final Boolean enabled;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender,
                           TemplateService templateService,
                           @Value("${mail.enabled}") Boolean enabled) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.enabled = enabled;
    }

    @Override
    public void send(String to, String subject, String content, boolean html) {
        if (!enabled) {
            log.info("Mail service disabled");
            return;
        }
        log.debug("Sending '{}' message to {}", to, subject);
        try {
            mailSender.send(mimeMessage -> {
                final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, html);
            });
        } catch (final Exception e) {
            log.error("Failed to send message - check configuration", e);
        }
    }

    @EventListener
    private void handleEvent(UserCreatedEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", event.getUser());
        data.put("link", event.getConfirmationUrl());
        send(event.getUser().getEmail(), "Współogarniacz - potwierdzenie rejestracji i aktywacja konta",
                templateService.merge("mail/accountCreated.ftl", data), true);
    }

    @EventListener
    private void handleEvent(PasswordResetRequestEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("user", event.getUser());
        data.put("resetUrl", event.getResetUrl());
        send(event.getUser().getEmail(), "Współogarniacz - reset hasła",
                templateService.merge("mail/passwordResetRequested.ftl", data), true);
    }

}
