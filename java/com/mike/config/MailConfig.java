package com.mike.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${host}")
    private String host;

    @Value("${secretaryMail}")
    private String secretaryMail;

    @Value("${password}")
    private String password;

    @Value("${port}")
    private int port;

    @Value("${protocol}")
    private String protocol;

    @Value("${encoding}")
    private String encoding;

    @Value("true")
    private String debug;

    @Bean
    public JavaMailSender getMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(secretaryMail);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding(encoding);

        Properties properties = mailSender.getJavaMailProperties();

        properties.setProperty("mail.transport.protocol", protocol);
        properties.setProperty("mail.debug", debug);

        return mailSender;
    }
}
