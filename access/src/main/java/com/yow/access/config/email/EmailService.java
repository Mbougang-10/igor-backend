package com.yow.access.config.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Value("${spring.mail.username:noreply@localhost}")
    private String fromEmail;

    public void sendActivationEmail(String toEmail, String username, String activationToken) {
        String activationLink = baseUrl + "/activate?token=" + activationToken;

        if (mailSender == null) {
            // Mode developpement : afficher dans la console
            System.out.println("===========================================");
            System.out.println("EMAIL D'ACTIVATION (mode dev)");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: Activez votre compte");
            System.out.println("Lien d'activation: " + activationLink);
            System.out.println("===========================================");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Activez votre compte");
        message.setText(buildActivationEmailBody(username, activationLink));

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        String resetLink = baseUrl + "/reset-password?token=" + resetToken;

        if (mailSender == null) {
            // Mode developpement : afficher dans la console
            System.out.println("===========================================");
            System.out.println("EMAIL DE REINITIALISATION (mode dev)");
            System.out.println("To: " + toEmail);
            System.out.println("Subject: Reinitialisation de votre mot de passe");
            System.out.println("Lien de reinitialisation: " + resetLink);
            System.out.println("===========================================");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reinitialisation de votre mot de passe");
        message.setText(buildPasswordResetEmailBody(username, resetLink));

        mailSender.send(message);
    }

    private String buildActivationEmailBody(String username, String activationLink) {
        return "Bonjour " + username + ",\n\n" +
                "Votre compte a ete cree avec succes.\n\n" +
                "Pour activer votre compte, cliquez sur le lien ci-dessous:\n" +
                activationLink + "\n\n" +
                "Ce lien expire dans 24 heures.\n\n" +
                "Cordialement,\nL'equipe Access";
    }

    private String buildPasswordResetEmailBody(String username, String resetLink) {
        return "Bonjour " + username + ",\n\n" +
                "Vous avez demande la reinitialisation de votre mot de passe.\n\n" +
                "Cliquez sur le lien ci-dessous:\n" +
                resetLink + "\n\n" +
                "Ce lien expire dans 1 heure.\n\n" +
                "Cordialement,\nL'equipe Access";
    }
}