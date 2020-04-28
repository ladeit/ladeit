package com.ladeit.biz.utils;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

/**
 * @author MddandPyy
 * @version V1.0
 * @Classname PropertiesUtil
 * @Date 2020/4/24 17:14
 */
@Component
public class PropertiesUtil {

    private static String ladeitproperties = System.getProperty("user.home")+"/.ladeit/config/.ladeit.conf";

    public String getProperty(String key){
        String property = null;
        Properties properties = new Properties();
        File file = new File(ladeitproperties);
        if(file.exists()){
            InputStream input = null;
            try {
                input = new FileInputStream(ladeitproperties);
                properties.load(input);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            property = (String) properties.get(key);
        }
        return property;
    }
}
