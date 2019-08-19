package net.nextabc.edgex;

import lombok.extern.log4j.Log4j;

/**
 * @author 陈哈哈 (yoojiachen@gmail.com)
 */
@Log4j
public class Texts {

    public static String required(String value, String message) {
        if (value == null || value.isEmpty()) {
            log.fatal(message);
        }
        return value;
    }
}
