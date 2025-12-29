package com.example.LunchGo.sms.service;

import com.example.LunchGo.common.util.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisUtil redisUtil;

    @Value("${coolsms.apiKey}")
    private String apiKey;

    @Value("${coolsms.secretkey}")
    private String apiSecret;

    @Value("${coolsms.from}") // 발신번호
    private String fromNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    @Override
    public String createVerifyCode() {
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

    @Override
    public void certificateSMS(String phone) {
        //랜덤 번호 생성
        String randomNum = createVerifyCode();

        //메시지 생성
        Message coolsms = new Message();
        coolsms.setFrom(fromNumber);
        coolsms.setTo(phone);
        coolsms.setText("[LunchGo] 인증번호 [" + randomNum + "]를 입력해 주세요.");

        if(redisUtil.existData(phone)){
            redisUtil.deleteData(phone); //이미 존재하면 데이터 지우고 다시 보내기
        }

        long expireTime = 1000*60*3L; //유효기간 설정 후 redis에 저장
        redisUtil.setDataExpire(phone, randomNum, expireTime);

        try { //인증번호 전송
            SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(coolsms));
        }catch(Exception e) {
            redisUtil.deleteData(phone);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()); //메시지 전송 실패시 서버 문제
        }
    }

    //인증번호 검증
    @Override
    public Boolean verifySMSCode(String phone, String code) {
        String codeByPhone = redisUtil.getData(phone);

        if(codeByPhone == null) return false;
        return code.equals(codeByPhone);
    }
}
