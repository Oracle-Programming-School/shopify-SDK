/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FND;

import java.util.*;
import javax.mail.*;
import Shopify.Log.Console;
import javax.mail.internet.*;

/**
 *
 * @author abdul.ahad1
 */
public class Email {

    public void sendAlert(String toName, String toMail, String subject, String emailText, boolean includeDefaultCC, String customCC) {

        /*
     --> toName is not a mandatory parameter.
     --> toName treated as salutation, if its null than it will treated as Concern e.g Hello Concern.
     --> In toMail we can pass comma separated list of emails e.g "abc@xyz.com,dummy@xyz.com"
     --> includeDefaultCC is mandatory parameter eiter true or false: it will handle include or exclude default CC group
     --> customCC is not a mandatory parameter.
        
     --> example call# 1: this.sendAlert(null,"abc@xyz.com","test","test email",true,null);
     --> example call# 2: this.sendAlert("Abdul Ahad","abc@xyz.com","test","test email",false,"abc@srl.com.pk");
         */
        String host = "host@domain.com";
        String defaultCC = "";
        String to = toMail;
        final String user = "host@srl.com.pk";
        final String password = "Password";
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "smtp.office365.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        Session session = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.addRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            if (includeDefaultCC == true) {

                if (customCC != null) {
                    defaultCC = defaultCC + "," + customCC;
                }

                message.addRecipients(Message.RecipientType.CC,
                        InternetAddress.parse(defaultCC));

            } else if (includeDefaultCC == false && customCC != null) {

                message.addRecipients(Message.RecipientType.CC,
                        InternetAddress.parse(customCC));

            }

            message.setSubject(subject);
            message.setContent(this.formatedBody(toName, emailText), "text/html; charset=utf-8");
            Transport.send(message, message.getAllRecipients());
            System.out.println("message sent....");
            Console.show("Subject Email |" + subject + "| Send Successfully To: " + to);

        } catch (Exception ex) {
            Console.show("Error --> FND.Email --> Send --> " + ex.getMessage());
        }

    }

    private String formatedBody(String salutation, String text) {

        if (salutation == null) {
            salutation = "Concern";
        }
        return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional //EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n"
                + "<head>\n"
                + "<!--[if gte mso 9]>\n"
                + "<xml>\n"
                + "  <o:OfficeDocumentSettings>\n"
                + "    <o:AllowPNG/>\n"
                + "    <o:PixelsPerInch>96</o:PixelsPerInch>\n"
                + "  </o:OfficeDocumentSettings>\n"
                + "</xml>\n"
                + "<![endif]-->\n"
                + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <meta name=\"x-apple-disable-message-reformatting\">\n"
                + "  <!--[if !mso]><!--><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><!--<![endif]-->\n"
                + "  <title></title>\n"
                + "  \n"
                + "    <style type=\"text/css\">\n"
                + "      table, td { color: #000000; } @media (max-width: 480px) { #u_content_text_2 .v-text-align { text-align: left !important; } #u_content_image_3 .v-src-width { width: 99px !important; } #u_content_image_3 .v-src-max-width { max-width: 15% !important; } }\n"
                + "@media only screen and (min-width: 520px) {\n"
                + "  .u-row {\n"
                + "    width: 500px !important;\n"
                + "  }\n"
                + "  .u-row .u-col {\n"
                + "    vertical-align: top;\n"
                + "  }\n"
                + "\n"
                + "  .u-row .u-col-100 {\n"
                + "    width: 500px !important;\n"
                + "  }\n"
                + "\n"
                + "}\n"
                + "\n"
                + "@media (max-width: 520px) {\n"
                + "  .u-row-container {\n"
                + "    max-width: 100% !important;\n"
                + "    padding-left: 0px !important;\n"
                + "    padding-right: 0px !important;\n"
                + "  }\n"
                + "  .u-row .u-col {\n"
                + "    min-width: 320px !important;\n"
                + "    max-width: 100% !important;\n"
                + "    display: block !important;\n"
                + "  }\n"
                + "  .u-row {\n"
                + "    width: calc(100% - 40px) !important;\n"
                + "  }\n"
                + "  .u-col {\n"
                + "    width: 100% !important;\n"
                + "  }\n"
                + "  .u-col > div {\n"
                + "    margin: 0 auto;\n"
                + "  }\n"
                + "}\n"
                + "body {\n"
                + "  margin: 0;\n"
                + "  padding: 0;\n"
                + "}\n"
                + "\n"
                + "table,\n"
                + "tr,\n"
                + "td {\n"
                + "  vertical-align: top;\n"
                + "  border-collapse: collapse;\n"
                + "}\n"
                + "\n"
                + "p {\n"
                + "  margin: 0;\n"
                + "}\n"
                + "\n"
                + ".ie-container table,\n"
                + ".mso-container table {\n"
                + "  table-layout: fixed;\n"
                + "}\n"
                + "\n"
                + "* {\n"
                + "  line-height: inherit;\n"
                + "}\n"
                + "\n"
                + "a[x-apple-data-detectors='true'] {\n"
                + "  color: inherit !important;\n"
                + "  text-decoration: none !important;\n"
                + "}\n"
                + "\n"
                + "</style>\n"
                + "  \n"
                + "  \n"
                + "\n"
                + "<!--[if !mso]><!--><link href=\"https://fonts.googleapis.com/css?family=Rubik:400,700&display=swap\" rel=\"stylesheet\" type=\"text/css\"><!--<![endif]-->\n"
                + "\n"
                + "</head>\n"
                + "\n"
                + "<body class=\"clean-body\" style=\"margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #F4F4F7;\n"
                + "   \n"
                + "    background-repeat: no-repeat;\n"
                + "    background-position: right bottom;\n"
                + "    background-size: 200px 280px;\n"
                + "  mix-blend-mode: multiply;\")\">\n"
                + "  <!--[if IE]><div class=\"ie-container\"><![endif]-->\n"
                + "  <!--[if mso]><div class=\"mso-container\"><![endif]-->\n"
                + "  <table style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #e7e7e7;width:100%\" cellpadding=\"0\" cellspacing=\"0\">\n"
                + "  <tbody>\n"
                + "  <tr style=\"vertical-align: top\">\n"
                + "    <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
                + "    <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td align=\"center\" style=\"background-color: #e7e7e7;\"><![endif]-->\n"
                + "    \n"
                + "\n"
                + "<div class=\"u-row-container\" style=\"padding: 0px;background-color: #ffffff\">\n"
                + "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 500px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">\n"
                + "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">\n"
                + "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: #ffffff;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:500px;\"><tr style=\"background-color: transparent;\"><![endif]-->\n"
                + "      \n"
                + "<!--[if (mso)|(IE)]><td align=\"center\" width=\"500\" style=\"width: 500px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->\n"
                + "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 500px;display: table-cell;vertical-align: top;\">\n"
                + "  <div style=\"width: 100% !important;\">\n"
                + "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->\n"
                + "  \n"
                + "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
                + "  <tbody>\n"
                + "    <tr>\n"
                + "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
                + "        \n"
                + "  <div class=\"v-text-align\" style=\"line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
                + "    <p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-family: Rubik, sans-serif; font-size: 14px; line-height: 19.6px;\"> <strong>Hello " + salutation + ", </strong></span></p>\n"
                + "<p style=\"font-size: 14px; line-height: 140%;\">&nbsp;</p>\n"
                + "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-family: Rubik, sans-serif; font-size: 14px; line-height: 19.6px;\">" + text + " </span></p>\n"
                + "  </div>\n"
                + "\n"
                + "      </td>\n"
                + "    </tr>\n"
                + "  </tbody>\n"
                + "</table>\n"
                + "\n"
                + "<table id=\"u_content_text_2\" style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
                + "  <tbody>\n"
                + "    <tr>\n"
                + "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
                + "        \n"
                + "  <div class=\"v-text-align\" style=\"line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
                + "    \n"
                + "  </div>\n"
                + "\n"
                + "      </td>\n"
                + "    </tr>\n"
                + "  </tbody>\n"
                + "</table>\n"
                + "\n"
                + "<table id=\"u_content_image_3\" style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
                + "  <tbody>\n"
                + "    <tr>\n"
                + "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
                + "        \n"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
                + "  <tr>\n"
                + "    <td class=\"v-text-align\" style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\">\n"
                + "      \n"
                + "      \n"
                + "    </td>\n"
                + "  </tr>\n"
                + "</table>\n"
                + "\n"
                + "      </td>\n"
                + "    </tr>\n"
                + "  </tbody>\n"
                + "</table>\n"
                + "\n"
                + "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
                + "  <tbody>\n"
                + "    <tr>\n"
                + "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
                + "        \n"
                + "  <div class=\"v-text-align\" style=\"line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
                + "  </div>\n"
                + "\n"
                + "      </td>\n"
                + "    </tr>\n"
                + "  </tbody>\n"
                + "</table>\n"
                + "\n"
                + "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
                + "  <tbody>\n"
                + "    <tr>\n"
                + "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">\n"
                + "        \n"
                + "\n"
                + "      </td>\n"
                + "    </tr>\n"
                + "  </tbody>\n"
                + "</table>\n"
                + "\n"
                + "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n"
                + "  </div>\n"
                + "</div>\n"
                + "<!--[if (mso)|(IE)]></td><![endif]-->\n"
                + "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</div>\n"
                + "\n"
                + "\n"
                + "    <!--[if (mso)|(IE)]></td></tr></table><![endif]-->\n"
                + "    </td>\n"
                + "  </tr>\n"
                + "  </tbody>\n"
                + "  </table>\n"
                + "  <!--[if mso]></div><![endif]-->\n"
                + "  <!--[if IE]></div><![endif]-->\n"
                + "</body>\n"
                + "\n"
                + "</html>";

    }

}
