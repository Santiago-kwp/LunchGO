package com.example.LunchGo.sms.service;

import java.util.HashMap;

public interface SmsService {
    String createVerifyCode();

    void certificateSMS(String phone);

    Boolean verifySMSCode(String phone, String code);
}
