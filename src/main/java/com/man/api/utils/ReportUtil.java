package com.man.api.utils;

import org.testng.Reporter;
import java.util.Calendar;

public class ReportUtil {

    private static String reportName = "自动化接口测试";

    private static String splitTieAndMsg = "===";

    public static void log(String msg){
        long timeMillis = Calendar.getInstance().getTimeInMillis();
        Reporter.log(timeMillis + splitTieAndMsg + msg, true);
    }

    public static void setReportName(String reportName){
        if(StringUtil.isNotEmpty(reportName)){
            ReportUtil.reportName = reportName;
        }
    }

    public static String getReportName() {
        return reportName;
    }
}
