package com.purplepanda.wspologarniacz.base.mail;

public interface MailService {
    void send(String to, String subject, String content, boolean html);
    default void send(String to, String subject, String content) {
        send(to, subject, content, true);
    }
}
