package com.jftse.emulator.common.exception;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Log4j2
public class GlobalExceptionHandler {

    @AfterThrowing(pointcut = "execution(* com.ft.emulator..*(..))", throwing = "e")
    public void handleUncaughtException(Exception e) {
        log.error(e.getMessage(), e);
    }
}