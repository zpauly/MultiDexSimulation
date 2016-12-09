package com.zpauly.multidexsimulation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by zpauly on 2016/12/9.
 */

public class ClassUtils {
    public static Field getField(Object instance, String fieldName) {
        Class<?> clazz = instance.getClass();

        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Object instance, String methodName, Class<?>...parameterTypes) {
        Class<?> clazz = instance.getClass();

        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
