package com.raks.aegis.util;

import com.raks.aegis.AegisMain.ApiResult;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public static class SMTPConfig {
        public String host = "smtp.gmail.com";
        public int port = 587;
        public String username = "rakkuma8@gmail.com";
        public String password = "fjuqjxckvosjummv";
        public String from = "apiguard-noreply@raks.com";
        public String fromName = "Aegis";
        public String footerText = "&copy; 2026 RAKS - Enterprise API Solution Suite";
        public String headerTitle = "Aegis Validation Report";
        public String logoPath = null;
    }

    public static void sendReportEmail(List<ApiResult> results, String recipients, Path reportsZip, SMTPConfig config) {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }

        logger.info("Sending report email to: {}", recipients);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.host);
        props.put("mail.smtp.port", String.valueOf(config.port));
        props.put("mail.smtp.ssl.trust", config.host);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.username, config.password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            String fromDisplay = config.fromName + " <" + config.from + ">";
            message.setFrom(new InternetAddress(config.from, config.fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            
            int totalApis = results.size();
            int totalPassed = results.stream().mapToInt(r -> r.passed).sum();
            int totalFailed = results.stream().mapToInt(r -> r.failed).sum();
            String overallStatus = (totalFailed == 0) ? "PASS" : "FAIL";

            message.setSubject("Aegis Validation Report - " + overallStatus + " (" + totalApis + " APIs)");

             
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String htmlBody = generateHtmlBody(results, totalPassed, totalFailed, overallStatus, config.headerTitle, config.footerText);
            messageBodyPart.setContent(htmlBody, "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart();
            
             
            if (config.logoPath != null && new File(config.logoPath).exists()) {
                MimeBodyPart logoPart = new MimeBodyPart();
                logoPart.attachFile(new File(config.logoPath));
                logoPart.setContentID("<logo>");
                logoPart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(logoPart);
            }

            multipart.addBodyPart(messageBodyPart);

             
            if (reportsZip != null && reportsZip.toFile().exists()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(reportsZip.toFile());
                attachPart.setFileName(reportsZip.getFileName().toString());
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            logger.info("Report email sent successfully.");

        } catch (Exception e) {
            logger.error("Failed to send report email: {}", e.getMessage(), e);
        }
    }

    private static String generateHtmlBody(List<ApiResult> results, int totalPassed, int totalFailed, String status, String title, String footer) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");
        sb.append("<div style='background-color: #663399; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>");
        sb.append("<img src='cid:logo' alt='Aegis' style='height: 50px; display: block; margin: 0 auto 10px auto;'>");
        sb.append("<h1 style='margin: 0;'>🛡️ " + title + "</h1>");
        sb.append("</div>");
        
        sb.append("<div style='padding: 20px; border: 1px solid #ddd; border-top: none; border-radius: 0 0 8px 8px;'>");
        sb.append("<h2>Scan Summary</h2>");
        sb.append("<p><strong>Overall Status:</strong> <span style='color: " + (status.equals("PASS") ? "green" : "red") + "; font-weight: bold;'>" + status + "</span></p>");
        sb.append("<p><strong>Total APIs Scanned:</strong> " + results.size() + "</p>");
        sb.append("<p><strong>Total Rules Passed:</strong> <span style='color: green;'>" + totalPassed + "</span></p>");
        sb.append("<p><strong>Total Rules Failed:</strong> <span style='color: red;'>" + totalFailed + "</span></p>");
 
        sb.append("<h3>API Details</h3>");
        sb.append("<table border='1' cellpadding='10' cellspacing='0' style='width: 100%; border-collapse: collapse; border: 1px solid #ddd;'>");
        sb.append("<tr style='background-color: #f2f2f2;'><th>Repository</th><th>API Name</th><th>Passed</th><th>Failed</th><th>Status</th></tr>");
        
        for (ApiResult r : results) {
            String apiStatus = (r.failed == 0) ? "PASS" : "FAIL";
            String color = (r.failed == 0) ? "#e8f5e9" : "#ffebee";
            sb.append("<tr style='background-color: " + color + ";'>");
            sb.append("<td>" + r.repository + "</td>");
            sb.append("<td>" + r.name + "</td>");
            sb.append("<td>" + r.passed + "</td>");
            sb.append("<td>" + r.failed + "</td>");
            sb.append("<td style='color: " + (apiStatus.equals("PASS") ? "green" : "red") + "; font-weight: bold;'>" + apiStatus + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        
        sb.append("<p style='margin-top: 20px; font-size: 0.9em; color: #666;'>This is an automated report generated by Aegis. The full report is attached as a ZIP file.</p>");
        sb.append("</div>");
        sb.append("<div style='text-align: center; margin-top: 20px; font-size: 0.8em; color: #999;'>" + footer + "</div>");
        sb.append("</body></html>");
        
        return sb.toString();
    }
}
