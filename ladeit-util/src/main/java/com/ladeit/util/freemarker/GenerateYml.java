package com.ladeit.util.freemarker;

import com.ladeit.common.pool.Pool;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.Map;

public class GenerateYml {
    public static String getYml(String ftl, Map root, Pool pool) {
        Configuration config = (Configuration) pool.getItem();
        String result = null;
        Template temp = null;
        try {
            temp = config.getTemplate(ftl);
            // 写到磁盘上
            File file = new File("/data/product/.gitlab-ci.yaml");
            Writer out = new FileWriter(file);
            temp.process(root, out);
            // 读取到内存中
            Writer strout = new StringWriter(2048);
            temp.process(root, strout);
            result = strout.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            pool.close(config);
        }
        return result;
    }
}
