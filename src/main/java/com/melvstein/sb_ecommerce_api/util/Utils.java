package com.melvstein.sb_ecommerce_api.util;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Utils {
    public static String getClassName() {
        String className = "unknownClassName";
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        if (stackTraceElements.length > 2) {
            StackTraceElement element = stackTraceElements[2];
            className = element.getClassName();
            String[] parts = className.split("\\.");
            className = parts[parts.length - 1];
        }

        return className;
    }

    public static String getMethodName() {
        String methodName = "unknownMethodName";
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        if (stackTraceElements.length > 2) {
            StackTraceElement element = stackTraceElements[2];
            methodName = element.getMethodName();
        }

        return methodName;
    }

    public static String getClassAndMethod() {
        String classAndMethod = "unknownClassAndMethod";
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        if (stackTraceElements.length > 2) {
            StackTraceElement element = stackTraceElements[2];
            String className = element.getClassName();
            String methodName = element.getMethodName();
            String[] parts = className.split("\\.");
            className = parts[parts.length - 1];
            classAndMethod = className + "." + methodName;
        }

        return classAndMethod;
    }

    public static String formatInstantToDateString(Instant instant, String dateTimeFormat, String timezone) {
        if (instant == null) return null;

        return DateTimeFormatter.ofPattern(dateTimeFormat)
                .withZone(ZoneId.of(timezone))
                .format(instant);
    }

    public static String formatInstantToDateString(Instant instant) {
        if (instant == null) return null;

        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Manila"))
                .format(instant);
    }

    public static Date fromInstantToDate(Instant instant) {
        if (instant == null) return null;

        return Date.from(instant);
    }

    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
