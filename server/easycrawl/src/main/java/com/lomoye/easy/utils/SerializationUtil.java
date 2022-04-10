package com.lomoye.easy.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SerializationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationUtil.class);

    private static final ObjectMapper MAPPER = JacksonMapperUtil.create();  //thread safe的

    public static String obj2String(Object object) {
        try {
            if (object == null) {
                return "";
            }
            return MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            LOGGER.warn("obj to json string error|objectType=" + object.getClass().getCanonicalName(), e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T str2Object(String jsonString, TypeReference<T> type) {
        try {
            return MAPPER.readValue(jsonString, type);
        } catch (IOException e) {
            LOGGER.warn("json string to object error by type|string={}|type={}", jsonString, type.getClass().getCanonicalName(), e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T str2Object(String jsonString, Class<T> clazz) {
        try {
            return MAPPER.readValue(jsonString, clazz);
        } catch (IOException e) {
            LOGGER.warn("json string to object error by class|str= {}|type={}", jsonString, clazz.getCanonicalName(), e);
            throw new RuntimeException(e);
        }
    }
}
