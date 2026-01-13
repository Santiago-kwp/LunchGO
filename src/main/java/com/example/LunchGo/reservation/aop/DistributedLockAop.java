package com.example.LunchGo.reservation.aop;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.annotation.DistributedLock;
import com.example.LunchGo.reservation.exception.DuplicateReservationException;
import com.example.LunchGo.reservation.exception.WaitingReservationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private final RedisUtil redisUtil;

    @Around("@annotation(com.example.LunchGo.reservation.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // SpEL 파싱하여 키 생성
        String lockKey = (String) CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.lockKey());
        
        String userLockKey = null;
        if (StringUtils.hasText(distributedLock.userLockKey())) {
            userLockKey = (String) CustomSpringELParser.getDynamicValue(
                    signature.getParameterNames(), joinPoint.getArgs(), distributedLock.userLockKey());
        }

        // 1. [개인 락] Lettuce (Fail-Fast)
        if (userLockKey != null) {
            if (!redisUtil.setIfAbsent(userLockKey, "processing", distributedLock.userLockTime())) {
                throw new DuplicateReservationException("이미 처리 중인 예약 요청입니다. 잠시 후 다시 시도해주세요.");
            }
        }

        // 2. [식당 락] Redisson (Waiting Queue)
        // Redisson 키는 보통 LOCK: prefix를 붙여서 구분하기도 함 (여기서는 그대로 사용)
        RLock rLock = redisUtil.getLock(lockKey);
        String waitingCountKey = "waiting_count:" + lockKey;

        try {
            // 대기열 진입: 카운트 증가
            redisUtil.increment(waitingCountKey);

            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            
            if (!available) {
                // 대기열 진입 실패 시 (타임아웃)
                // 개인 락 해제하여 재시도 허용
                if (userLockKey != null) {
                    redisUtil.deleteData(userLockKey);
                }
                
                // 현재 대기 인원 조회
                long currentWaitingCount = redisUtil.getCount(waitingCountKey);
                throw new WaitingReservationException("접속자가 많아 대기 중입니다. 잠시 후 자동으로 재시도합니다.", currentWaitingCount);
            }

            // --- 비즈니스 로직 실행 ---
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            // 인터럽트 발생 시 락 해제 및 상태 복구
            if (userLockKey != null) {
                redisUtil.deleteData(userLockKey);
            }
            Thread.currentThread().interrupt(); // 인터럽트 상태 복구
            throw new IllegalStateException("DistributedLock AOP: Lock acquisition interrupted", e); // 런타임 예외로 래핑
        } finally {
            // 식당 락 해제
            if (rLock != null && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
            // 작업 종료(성공/실패/포기) 후 대기열 이탈: 카운트 감소
            redisUtil.decrement(waitingCountKey);
            
            // 개인 락은 성공 시 해제하지 않음 (TTL 유지)
            // 단, 예외가 발생해서 여기까지 왔다면(catch 블록을 거치지 않은 런타임 예외 등) 해제해야 할 수도 있지만,
            // 현재 로직상 성공 시에는 유지하는 것이 정책이므로 그대로 둠.
            // (비즈니스 예외 발생 시에도 개인 락이 유지되는 부작용은 있으나, 이는 3초 후 해소됨)
        }
    }
}
