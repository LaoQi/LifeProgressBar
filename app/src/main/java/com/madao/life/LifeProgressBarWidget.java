package com.madao.life;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class LifeProgressBarWidget extends AppWidgetProvider {

    private static final int[] LastDayBarIds = new int[]{R.id.LastDayBarGreen, R.id.LastDayBarBlue, R.id.LastDayBarYellow, R.id.LastDayBarRed};
    private static final int[] LastLifeBarIds = new int[]{R.id.LastLifeBarGreen, R.id.LastLifeBarBlue, R.id.LastLifeBarYellow, R.id.LastLifeBarRed};
    private static final int[] LastMonthBarIds = new int[]{R.id.LastMonthBarGreen, R.id.LastMonthBarBlue, R.id.LastMonthBarYellow, R.id.LastMonthBarRed};
    private static final int[] LastWeekBarIds = new int[]{R.id.LastWeekBarGreen, R.id.LastWeekBarBlue, R.id.LastWeekBarYellow, R.id.LastWeekBarRed};
    private static final int[] LastYearBarIds = new int[]{R.id.LastYearBarGreen, R.id.LastYearBarBlue, R.id.LastYearBarYellow, R.id.LastYearBarRed};

    public interface Condition {
        boolean compare(int value);
    }

    static class ProgressBar {
        private final int TextBar;
        private final int TextColor;
        private final int[] ColorBar;
        private int value;

        public int getValue() {
            return value;
        }

        ProgressBar(int textBar, int[] colorBar, int textColor) {
            TextBar = textBar;
            ColorBar = colorBar;
            TextColor = textColor;
        }

        @SuppressLint("DefaultLocale")
        public ProgressBar show(RemoteViews views, int value, int maxVal, String format) {
            this.value = value;
            views.setTextViewText(TextBar, String.format(format, value));
            views.setTextColor(TextBar, TextColor);
            setProgressBar(views, value * 100 / maxVal);
            return this;
        }

        public ProgressBar then(RemoteViews views, Condition cond, String format, Object... args) {
            if (cond.compare(value)) {
                views.setTextColor(TextBar, TextColor);
                views.setTextViewText(TextBar, String.format(format, args));
            }
            return this;
        }

        private void setProgressBar(RemoteViews views, int progress) {
            for (int id : ColorBar) {
                views.setViewVisibility(id, View.GONE);
            }
            int id;
            if (progress > 75) {
                id = ColorBar[0];
            } else if (progress > 50) {
                id = ColorBar[1];
            } else if (progress > 25) {
                id = ColorBar[2];
            } else {
                id = ColorBar[3];
            }
            views.setProgressBar(id, 100, progress, false);
            views.setViewVisibility(id, View.VISIBLE);
        }
    }

    @SuppressLint("DefaultLocale")
    static void updateAppWidget(Context context, RemoteViews views) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String lifetimeString = sp.getString(SettingsActivity.PreferenceKeyLifetime, "76");
        int lifetime = Integer.parseInt(lifetimeString);
        Calendar birthday = Calendar.getInstance(Locale.CHINA);

        String birthdayString = sp.getString(SettingsActivity.PreferenceKeyBirthday, "2000/1/1");
        DateBean db = new DateBean();
        db.fromString(birthdayString);

        String firstDayOfWeekString = sp.getString(SettingsActivity.PreferenceKeyFirstDayOfWeek, "1");
        int firstDayOfWeek = Integer.parseInt(firstDayOfWeekString);

        int textColor = sp.getInt(SettingsActivity.PreferenceKeyFontColor, Color.WHITE);
        int bgColor = sp.getInt(SettingsActivity.PreferenceKeyBackgroundColor, Color.GRAY);

        birthday.set(db.getYear(), db.getMonth() - 1, db.getDay());
        var now = Calendar.getInstance(Locale.CHINA);
        now.setFirstDayOfWeek(Calendar.MONDAY);
        var overtime = Calendar.getInstance(Locale.CHINA);
        overtime.set(birthday.get(Calendar.YEAR) + lifetime, birthday.get(Calendar.MONTH), birthday.get(Calendar.DATE));

        var lastDays = (overtime.getTimeInMillis() - now.getTimeInMillis()) / 86400 / 1000;

        views.setTextColor(R.id.title, textColor);
        views.setInt(R.id.widgetBackground, "setBackgroundColor", bgColor);

        if (lastDays < 0) {
            views.setTextViewText(R.id.title,
                    String.format("已去世%d年%d天", -lastDays / 365, -lastDays % 365));
        } else {
            views.setTextViewText(R.id.title,
                    String.format("离终点还有%d年%d天", lastDays / 365 , lastDays % 365));
        }

        var lifeBar = new ProgressBar(R.id.LastLife, LastLifeBarIds, textColor)
                .show(views, (int) (lastDays / 365), lifetime, "你的人生还剩下超过 %d年")
                .then(views, value -> value < 0, "已去世超过 %d年", -lastDays/365);

        var dayBar = new ProgressBar(R.id.LastDay, LastDayBarIds, textColor)
                .show(views, 23 - now.get(Calendar.HOUR_OF_DAY), 24, "今天还余下大约 %d小时")
                .then(views, value -> value <= 0, "今天还剩不到 1小时");

        var weekBar = new ProgressBar(R.id.LastWeek, LastWeekBarIds, textColor)
                .show(views, (7 + firstDayOfWeek - now.get(Calendar.DAY_OF_WEEK)) % 7, 7, "本周还剩%d天")
                .then(views, value -> value <= 0, "本周还剩不到 1天");

       var monthBar = new ProgressBar(R.id.LastMonth, LastMonthBarIds, textColor)
                .show(views,
                        now.getActualMaximum(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_MONTH),
                        now.getActualMaximum(Calendar.DAY_OF_MONTH),
                        "本月还剩%d天")
               .then(views, value -> value <= 0, "本月还剩不到 1天");

        new ProgressBar(R.id.LastYear, LastYearBarIds, textColor)
                .show(views, 11 - now.get(Calendar.MONTH), 12, "今年还剩%d个月")
                .then(views, value -> value <= 0, "今年还剩不到 1月");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.life_progress_bar_widget);
            updateAppWidget(context, views);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (SettingsActivity.BroadcastName.equals(action)) {
            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, this.getClass()));
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            }
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}