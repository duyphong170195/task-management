package com.eight_seneca.task_management.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AOPAround {

//    @Around("execution(* com.eight_seneca.task_management.service.*.*(..))")
//    public Object around(ProceedingJoinPoint pjp) throws Throwable {
//
//        System.out.println("Before");
//
//        Object result = pjp.proceed(); // gọi method gốc
//
//        System.out.println("After");
//
//        return result;
//    }

    @Before("execution(* com.eight_seneca.task_management.service.*.*(..))")
    public void beforeAdvice(ProceedingJoinPoint pjp) {
        System.out.println("Before method");
    }

    @Before("this(com.eight_seneca.task_management.TaskManagementApplication)")
    public void thisPointcut() {
    }
}
