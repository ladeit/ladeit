package com.ladeit.util.git;

/**
 * @description: GitLabUtil
 * @author: falcomlife
 * @create: 2019/07/16
 * @version: 1.0.0
 */
public class GitLabUtil {
    /**
     * @param url
     * @FunctionName getPathFromUrl
     * @author falcomlife
     * @date 19-7-16
     * @version 1.0.0
     * @Return java.lang.String
     */
    public static String getPathFromUrl(String url) {
        String[] temp = url.split("/");
        String temp2 = temp[temp.length - 2] + "/" + temp[temp.length - 1];
        String[] finalString = temp2.split("\\.");
        return finalString[0];
    }
}
