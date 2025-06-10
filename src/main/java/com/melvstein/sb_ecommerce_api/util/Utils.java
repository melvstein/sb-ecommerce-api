package com.melvstein.sb_ecommerce_api.util;

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
}
