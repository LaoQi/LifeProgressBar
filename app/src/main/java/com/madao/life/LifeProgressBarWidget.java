package com.madao.life;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
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
    private static final String TAG = "LifeProgressBarWidget";

    public interface Condition {
        boolean compare(int value);
    }

    static class ProgressBar {
        private final int TextBar;
        private final int TextColor;
        private final int[] ColorBar;
        private int value;

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

        public ProgressBar then(RemoteViews views, boolean cond, int value, int maxVal) {
            if (cond) {
                this.value = value;
                views.setTextColor(TextBar, TextColor);
                setProgressBar(views, value * 100 / maxVal);
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
        String lifetimeString = sp.getString(Constant.PreferenceKeyLifetime, "76");
        int lifetime = Integer.parseInt(lifetimeString);
        Calendar birthday = Calendar.getInstance(Locale.CHINA);

        String birthdayString = sp.getString(Constant.PreferenceKeyBirthday, "2000/1/1");
        DateBean db = new DateBean();
        db.fromString(birthdayString);

        String firstDayOfWeekString = sp.getString(Constant.PreferenceKeyFirstDayOfWeek, "1");
        int firstDayOfWeek = Integer.parseInt(firstDayOfWeekString);

        boolean enableLunar = sp.getBoolean(Constant.PreferenceKeyEnableLunar, false);

        int textColor = sp.getInt(Constant.PreferenceKeyFontColor, Color.WHITE);
        int bgColor = sp.getInt(Constant.PreferenceKeyBackgroundColor, Color.GRAY);

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

        // lifeBar
        new ProgressBar(R.id.LastLife, LastLifeBarIds, textColor)
                .show(views, (int) (lastDays / 365), lifetime, "你的人生还剩下超过 %d年")
                .then(views, value -> value < 0, "已去世超过 %d年", -lastDays / 365);

        new ProgressBar(R.id.LastDay, LastDayBarIds, textColor)
                .show(views, 23 - now.get(Calendar.HOUR_OF_DAY), 24, "今天还余下大约 %d小时")
                .then(views, value -> value <= 0, "今天还剩不到 1小时");

        new ProgressBar(R.id.LastWeek, LastWeekBarIds, textColor)
                .show(views, (7 + firstDayOfWeek - now.get(Calendar.DAY_OF_WEEK)) % 7, 7, "本周还剩%d天")
                .then(views, value -> value <= 0, "本周还剩不到 1天");

       new ProgressBar(R.id.LastMonth, LastMonthBarIds, textColor)
                .show(views,
                        now.getActualMaximum(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_MONTH),
                        now.getActualMaximum(Calendar.DAY_OF_MONTH),
                        "本月还剩%d天")
               .then(views, value -> value <= 0, "本月还剩不到 1天");

        new ProgressBar(R.id.LastYear, LastYearBarIds, textColor)
                .show(views, 11 - now.get(Calendar.MONTH), 12, "今年还剩%d个月")
                .then(views, value -> value <= 0, "今年还剩不到 1月")
                .then(views, enableLunar, LunarUtil.DiffDayOfNewYear(), LunarUtil.TotalDaysOfYear())
                .then(views, value -> enableLunar, "距离新年还剩 " + LunarUtil.DiffDayOfNewYear() + "天");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        ComponentName componentName = new ComponentName(context, LifeProgressBarWidget.class);
//        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.life_progress_bar_widget);
//        appWidgetManager.updateAppWidget(componentName, views);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.i(TAG, "onReceive: update widget " + appWidgetId + " " + context.getPackageName());
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.life_progress_bar_widget);
            updateAppWidget(context, views);

            Intent intent = new Intent(Constant.BroadcastName);
            intent.setPackage(context.getPackageName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    /* context = */ context,
                    /* requestCode = */ 0,
                    /* intent = */ intent,
                    /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widgetBackground, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        Log.i(TAG, "onReceive: " + action);
        if (Constant.BroadcastName.equals(action)) {
            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, this.getClass()));
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            } else {
                Log.w(TAG, "onReceive: not found widget");
            }
        }
        if (Constant.MiuiBroadcast.equals(action)) {
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
            } else {
                Log.w(TAG, "onReceive: not found widget");
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