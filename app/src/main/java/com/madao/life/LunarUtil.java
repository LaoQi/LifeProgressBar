package com.madao.life;

import android.util.Log;

import java.util.Calendar;

public class LunarUtil {
    /**
     * from <a href="https://github.com/OPN48/cnlunar/blob/master/cnlunar/config.py#L133">...</a>
     * # 农历数据 每个元素的存储格式如下：
     * # 7~6    5~1
     * # 春节月  春节日
     */
    private final static byte[] LunarNewYear = new byte[]{
            0x38, 0x4c, 0x41, 0x36, 0x49, 0x3d, 0x52, 0x47, 0x3a, 0x4e,  // 2001 ~ 2010
            0x43, 0x37, 0x4a, 0x3f, 0x53, 0x48, 0x3c, 0x50, 0x45, 0x39,  // 2011 ~ 2020
            0x4c, 0x41, 0x36, 0x4a, 0x3d, 0x51, 0x46, 0x3a, 0x4d, 0x43,  // 2021 ~ 2030
            0x37, 0x4b, 0x3f, 0x53, 0x48, 0x3c, 0x4f, 0x44, 0x38, 0x4c,  // 2031 ~ 2040
            0x41, 0x36, 0x4a, 0x3e, 0x51, 0x46, 0x3a, 0x4e, 0x42, 0x37,  // 2041 ~ 2050
            0x4b, 0x41, 0x53, 0x48, 0x3c, 0x4f, 0x44, 0x38, 0x4c, 0x42,  // 2051 ~ 2060
            0x35, 0x49, 0x3d, 0x51, 0x45, 0x3a, 0x4e, 0x43, 0x37, 0x4b,  // 2061 ~ 2070
            0x3f, 0x53, 0x47, 0x3b, 0x4f, 0x45, 0x38, 0x4c, 0x42, 0x36,  // 2071 ~ 2080
            0x49, 0x3d, 0x51, 0x46, 0x3a, 0x4e, 0x43, 0x38, 0x4a, 0x3e,  // 2081 ~ 2090
            0x52, 0x47, 0x3b, 0x4f, 0x45, 0x39, 0x4c, 0x41, 0x35, 0x49,  // 2091 ~ 2100
    };

    private static Calendar GetNewYearDay(int year) {
        Calendar calendar = Calendar.getInstance();
        if (year > 2100 || year < 2001) {
            Log.e("LunarUtil", "Cannot get year " + year);
            return calendar;
        }
        byte data = LunarNewYear[year - 2001];
        int month = (data & 0x60) >> 5;
        int day = data & 0x1F;
        calendar.set(year, month - 1, day, 0, 0, 0);
        return calendar;
    }

    public static int DiffDayOfNewYear() {
        Calendar now = Calendar.getInstance();
        Calendar newYear = GetNewYearDay(now.get(Calendar.YEAR));
        if (newYear.before(now)) {
            newYear = GetNewYearDay(now.get(Calendar.YEAR) + 1);
        }

        return (int) ((newYear.getTimeInMillis() - now.getTimeInMillis()) / 86400 / 1000);
    }

    public static int TotalDaysOfYear() {
        Calendar now = Calendar.getInstance();
        Calendar lastYear = GetNewYearDay(now.get(Calendar.YEAR));
        Calendar newYear = GetNewYearDay(now.get(Calendar.YEAR));
        if (newYear.before(now)) {
            newYear = GetNewYearDay(now.get(Calendar.YEAR) + 1);
        } else {
            lastYear = GetNewYearDay(now.get(Calendar.YEAR) - 1);
        }

        return (int) ((newYear.getTimeInMillis() - lastYear.getTimeInMillis()) / 86400 / 1000);
    }
}
