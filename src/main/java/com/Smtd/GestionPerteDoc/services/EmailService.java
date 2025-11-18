package com.Smtd.GestionPerteDoc.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    // Envoi d'email avec PDF en pièce jointe
    public void envoyerEmailAvecPdf(String destinataire, byte[] pdfBytes, String numeroReference) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(destinataire);
        helper.setSubject("Confirmation de votre déclaration - Réf : " + numeroReference);
        helper.setText("Bonjour,\n\nVotre déclaration a bien été enregistrée.\nVous trouverez ci-joint votre récépissé au format PDF.\n\nCordialement,\nPoste Central", false);

        helper.addAttachment("Declaration-" + numeroReference + ".pdf", new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }

    // Envoi d'email avec mot de passe temporaire
    public void envoyerEmailMotDePasseTemporaire(String destinataire, String motDePasseTemp, String resetToken) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(destinataire);
        helper.setSubject("Activation de votre compte");

        String lienReset = "http://localhost:3000/reset-password/" + resetToken;

        helper.setText("Bonjour,\n\nVotre compte a été créé.\n" +
                       "Votre mot de passe temporaire est : " + motDePasseTemp + "\n" +
                       "Cliquez sur ce lien pour activer votre compte et changer votre mot de passe : " + lienReset + "\n\n" +
                       "Cordialement,\nPoste de Police", false);

        mailSender.send(message);
    }
    public void envoyerEmailMotDePasseOublie(String destinataire, String resetToken) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(destinataire);
        helper.setSubject("Réinitialisation de votre mot de passe oublié");

        String lienReset = "http://localhost:3000/reset-password/" + resetToken;

        helper.setText("Bonjour,\n\n" +
                       "Veuillez cliquer sur ce lien pour réinitialiser votre mot de passe : " + lienReset + "\n\n" +
                       "Cordialement,\nPoste de Police", false);

        mailSender.send(message);
    }

    public void envoyerEmailDeclarationValidee(String destinataire, String numeroReference) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(destinataire);
        helper.setSubject("Votre déclaration a été validée");

        // Lien correct pour React local
        String lienReact = "http://localhost:3000/declarations/" + numeroReference;

        helper.setText("Bonjour,\n\n" +
                       "Votre déclaration avec le numéro de référence " + numeroReference + " a été validée.\n" +
                       "Vous pouvez venir récupérer votre document  " +
                       "\nCordialement, \nPoste de Police", false);

        mailSender.send(message);
    }

}
