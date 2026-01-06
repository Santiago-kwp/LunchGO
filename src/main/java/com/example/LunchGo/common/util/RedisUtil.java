package com.example.LunchGo.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class RedisUtil {
    /**
     * 스프링이 제공하는 Redis 클라이언트,  Redis 명령어를 자바 코드로 보낼 수 있음
     * */
    private final StringRedisTemplate template;

    public String getData(String key){
        ValueOperations<String, String> ops = template.opsForValue();
        return ops.get(key);
    }
    //Redis에 저장된 key에 해당하는 값(value) 가져오기
    //이메일로 발송된 인증코드 꺼낼 때 사용

    public boolean existData(String key){
        return Boolean.TRUE.equals(template.hasKey(key));
    }
    //key가 Redis에 살아있는 지 확인(유효한지, 시간 초과인지)

    public void setDataExpire(String key, String value, long duration){
        ValueOperations<String, String> ops = template.opsForValue();
        Duration expireDuration = Duration.ofMillis(duration);
        ops.set(key, value, expireDuration);
    }
    //데이터 저장+유효기간 저장
    //데이터를 저장하면서 언제 자동으로 삭제될지 TTL 설정
    //시간 지나면 Redis가 알아서 데이터 지움

    public void deleteData(String key){
        template.delete(key);
    }
    //데이터 즉시 삭제(인증 성공 시 사용하게 됨)

    public boolean updateData(String key, String newValue) {
        if (!existData(key)) { //데이터가 없는 경우
            return false;
        }

        long ttlMillis = template.getExpire(key, TimeUnit.MILLISECONDS);
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set(key, newValue);

        if (ttlMillis > 0) {
            template.expire(key, Duration.ofMillis(ttlMillis)); //시간 남았으면 그대로 설정
        }

        return true;
    }
}
