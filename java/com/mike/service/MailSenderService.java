package com.mike.service;

import com.mike.domain.Paper;
import com.mike.domain.Publication;
import com.mike.domain.User;
import com.mike.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class MailSenderService {

    @Autowired
    private MailSender mailSender;

    @Autowired
    private UserRepo userRepo;

    private static final String SecretarySignature = "С наилучшими пожеланиями,\n" +
            "секретарь конференции\n" +
            "Чебодаева Валентина Вадимовна\n" +
            "Телефон: +7 983 598-47-84\n" +
            "E-mail: meso@ispms.ru\n" +
            "meso.ispms.ru";

    private static final String MailSubject = "Физическая мезомеханика.";

    public void passwordChangeMessage(User userFromDb) {
        if (StringUtils.hasLength(userFromDb.getEmail())) {
            userFromDb.setActivationCode(UUID.randomUUID().toString());
            userFromDb.setActive(false);
            userRepo.save(userFromDb);
            String message = String.format(
                    "Уважаемый, %s %s !\n" +
                            "Для изменения пароля вашего профиля, пожалуйста, перейдите по ссылке: http://meso.ispms.ru/passwordChange/%s\n\n" +
                            SecretarySignature,
                    userFromDb.getFirstName(), userFromDb.getMiddleName(), userFromDb.getActivationCode()
            );
            mailSender.send(userFromDb.getEmail(), MailSubject + " Изменение пароля", message);
        }
    }

    public void sendMessage(User user) {
        if (StringUtils.hasLength(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Вы успешно прошли регистрацию на Международную конференцию \"Физическая мезомеханика. Материалы " +
                            "с многоуровневой иерархически организованной структурой и интеллектуальные производственные технологии.\" \n" +
                            "Для активации вашего профиля, пожалуйста, перейдите по ссылке: http://meso.ispms.ru/activate/%s\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName(), user.getActivationCode()
            );
            mailSender.send(user.getEmail(), MailSubject + " Активация профиля", message);
        }
    }

    public void sendMessageEn(String firstName, String lastName, String email, String degree, String telephone,
                              String organization, String city, String country, boolean young, String publ, String sect) {
        String message = String.format("firstName: %s\nlastName: %s\nemail: %s\ndegree: %s\ntelephone: %s\norganization: %s\n" +
                        "city: %s\ncountry: %s\nyoung: %s\npubl: %s\nsect: %s\n",
                firstName, lastName, email, degree, telephone, organization, city, country, young, publ, sect);
        mailSender.send("meso@ispms.ru", MailSubject + " Регистрация иностранного участника", message);

    }

    public void sendMessageThesis(User user, Publication publication) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Ваши тезисы \"%s\" " +
                            "успешно прошли регистрацию на Международную конференцию \"Физическая мезомеханика. Материалы " +
                            "с многоуровневой иерархически организованной структурой и интеллектуальные производственные технологии.\"\n\n " +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName(), publication.getPublicationName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Тезисы.", message);
        }
    }

    public void sendMessageThesisPermission(User user, Publication publication) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Ваши тезисы \"%s\" " +
                            "успешно прошли регистрацию на Международную конференцию \"Физическая мезомеханика. Материалы " +
                            "с многоуровневой иерархически организованной структурой и интеллектуальные производственные технологии.\"\n" +
                            "Однако, просим Вас, по мере готовности, самостоятельно заменить в личном кабинете файл \"Разрешения на публикацию\"\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName(), publication.getPublicationName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Тезисы.", message);
        }
    }

    public void sendMessageThesisRed(User user, Publication publication, String textField) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Ваши тезисы \"%s\" " +
                            "требуют некоторых изменений:\n\n" +
                            textField + "\n" +
                            "После внесения соответствующих исправлений загрузите файл тезисов " +
                            "через форму редактирования в личном кабинете\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName(), publication.getPublicationName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Тезисы.", message);
        }
    }

    public void sendMessagePaperChecked(User user, Paper paper) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Ваша статья \"%s\" " +
                            "требует некоторых исправлений. С рецензией на статью можно ознакомиться " +
                            "в личном кабинете на сайте конференции http://meso.ispms.ru\n" +
                            "Прошу загрузить исправленную версию статьи в личный кабинет не позднее 7 дней с момента получения этого письма\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName(), paper.getPaperName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Статья.", message);
        }
    }

    public void sendMessagePaperAccepted(User user, Paper paper) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Ваша статья \"%s\" " +
                            "принята к публикации. С рецензией на статью можно ознакомиться " +
                            "в личном кабинете на сайте конференции http://meso.ispms.ru\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName(), paper.getPaperName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Статья.", message);
        }
    }

    public void sendMessageContract(User user, String textField) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Ваш договор требует некоторых изменений:\n\n" +
                            textField + "\n" +
                            "После внесения соответствующих исправлений загрузите скан договора " +
                            "в раздел Оргвзнос личного кабинета http://meso.ispms.ru\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Оргвзнос.", message);
        }
    }

    public void sendMessageContractExtra(User user, String textField) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "Ваше дополнительное соглашение требует некоторых изменений:\n\n" +
                            textField + "\n" +
                            "После внесения соответствующих исправлений загрузите документ еще раз\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Оргвзнос.", message);
        }
    }

    public void sendMessageContractInvoice(User user) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "В ваш личный кабинет направлен счет для оплаты оргвзноса.\n" +
                            "После оплаты загрузите, пожалуйста, копию чека " +
                            "в раздел Оргвзнос личного кабинета http://meso.ispms.ru\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Оргвзнос.", message);
        }
    }

    public void sendMessageContractExtraInvoice(User user) {
        if (!StringUtils.isEmpty(user.getEmail())) {
            String message = String.format(
                    "Уважаемый(ая), %s %s !\n" +
                            "В ваш личный кабинет направлен скан дополнительного соглашения.\n\n" +
                            SecretarySignature,
                    user.getFirstName(), user.getMiddleName()
            );
            mailSender.send(user.getEmail(), MailSubject + " Оргвзнос.", message);
        }
    }
}
