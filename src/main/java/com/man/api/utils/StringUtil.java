package com.man.api.utils;

public class StringUtil {

    public static boolean isNotEmpty(String str){
        return null != str && !"".equals(str);
    }

    public static boolean isEmpty(String str){
        return null == str && "".equals(str);
    }

    /**
     * 替换字符
     * @param sourceStr
     * @param matchStr
     * @param replaceStr
     * @return
     */
    public static String replaceString(String sourceStr, String matchStr, String replaceStr){
        int index = sourceStr.indexOf(matchStr);
        int matLength = matchStr.length();
        int sourLength = sourceStr.length();

        String beginStr = sourceStr.substring(0, index);
        String endStr = sourceStr.substring(index+matLength, sourLength);

        sourceStr = beginStr + replaceStr + endStr;
        return sourceStr;
    }
}
