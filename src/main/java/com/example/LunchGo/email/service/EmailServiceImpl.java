package com.example.LunchGo.email.service;

import com.example.LunchGo.common.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Random;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public String createCode() {
        int leftLimit = 48; //'0'
        int rightLimit = 122; // 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit+1)
                .filter(i-> (i <= 57 || i >= 65) && (i <= 90 || i>= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    //이메일 내용 초기화
    @Override
    public String setContext(String code) {
        Context context = new Context();

        context.setVariable("code", code);
        //스프링부트가 주입받은 엔진으로 resources/templates/mail.html 파일 찾음
        return templateEngine.process("mail", context);
    }

    @Override
    public MimeMessage createEmailForm(String email) throws MessagingException {
        String authCode = createCode();

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("[LunchGo] 인증번호 입니다. 인증번호 입력란에 입력해주세요.");
        message.setFrom(senderEmail);
        message.setText(setContext(authCode), "utf-8", "html");

        redisUtil.setDataExpire(email, authCode, 1000* 60*3L);
        return message;
    }

    //이메일 폼 생성 및 이메일 보내기
    @Override
    public void sendEmail(String toEmail) throws MessagingException {
        if(redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }

        MimeMessage emailForm = createEmailForm(toEmail);
        javaMailSender.send(emailForm);
    }

    //코드 검증
    @Override
    public Boolean verifyEmailCode(String email, String code) {
        String codeFoundByEmail = redisUtil.getData(email);
        log.info("code found by email: "+ codeFoundByEmail);
        if(codeFoundByEmail == null) return false; //코드가 null인 경우
        return codeFoundByEmail.equals(code);
    }
}
