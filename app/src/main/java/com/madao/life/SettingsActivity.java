package com.madao.life;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.skydoves.colorpickerpreference.ColorPickerPreference;
import com.skydoves.colorpickerview.AlphaTileView;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.flag.FlagView;

import java.util.Calendar;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    public final static String BroadcastName = "com.madao.life.APPWIDGET_UPDATE";
    public final static String PreferenceKeyBirthday = "birthday";
    public final static String PreferenceKeyLifetime = "lifetime";
    public final static String PreferenceKeyFirstDayOfWeek = "first_day_of_week";
    public final static String PreferenceKeyFontColor = "font_color";
    public final static String PreferenceKeyBackgroundColor = "background_color";

    public final static String PreferenceKeyEnableLunar = "enable_lunar";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        Button btn = findViewById(R.id.confirmBtn);
        btn.setVisibility(View.GONE);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.d("SettingsActivity", "first open");
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(v -> {
                    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        Log.d("SettingsActivity", "Config");
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                        RemoteViews views = new RemoteViews(getPackageName(), R.layout.life_progress_bar_widget);
                        LifeProgressBarWidget.updateAppWidget(this, views);
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                        Intent resultValue = new Intent();
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        setResult(RESULT_OK, resultValue);
                        finish();
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private Context mContext;
        SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener =
                (sharedPreferences, key) -> {
                    Intent intent = new Intent(BroadcastName);
                    intent.setPackage(mContext.getPackageName());
                    mContext.sendBroadcast(intent);
                };

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            mContext = getContext();
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            if (sp != null) {
                sp.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
            }
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference birthdayPreference = findPreference(PreferenceKeyBirthday);

            if (birthdayPreference != null) {
                birthdayPreference.setOnBindEditTextListener(this::bindDatePicker);
            }

            EditTextPreference lifetimePreference = findPreference(PreferenceKeyLifetime);
            Objects.requireNonNull(lifetimePreference).setOnBindEditTextListener(this::bindLifeTimePicker);

            ColorPickerPreference backgroundColorPreference = findPreference(PreferenceKeyBackgroundColor);
            if (backgroundColorPreference != null) {
                backgroundColorPreference.setDefaultColor(Color.DKGRAY);
                backgroundColorPreference.getColorPickerView().setFlagView(new CustomFlag(getContext()));
            }

            ColorPickerPreference fontColorPreference = findPreference(PreferenceKeyFontColor);
            if (fontColorPreference != null) {
                fontColorPreference.setDefaultColor(Color.LTGRAY);
                fontColorPreference.getColorPickerView().setFlagView(new CustomFlag(getContext()));
            }
        }

        @SuppressLint("ViewConstructor")
        static class CustomFlag extends FlagView {
            private final TextView textView;
            private final AlphaTileView alphaTileView;

            public CustomFlag(Context context) {
                super(context, R.layout.color_picker_flag_view);
                textView = findViewById(R.id.flag_color_code);
                alphaTileView = findViewById(R.id.flag_color_layout);
            }

            @Override
            public void onRefresh(ColorEnvelope colorEnvelope) {
                textView.setText(String.format("#%s", colorEnvelope.getHexCode()));
                alphaTileView.setPaintColor(colorEnvelope.getColor());
            }
        }

        @SuppressLint("DefaultLocale")
        private void bindLifeTimePicker(EditText editText) {
            View root = editText.getRootView();
            int age = 76;
            String value = String.valueOf(editText.getText());
            if (!value.isBlank()) {
                age = Integer.parseInt(value);
            } else {
                editText.setText(String.format("%d", age));
            }
            NumberPicker picker = root.findViewById(R.id.lifetime_picker);
            picker.setMaxValue(120);
            picker.setMinValue(3);
            picker.setValue(age);
            picker.setOnValueChangedListener(
                    (picker1, oldVal, newVal) -> {
                        editText.setText(String.format("%d", newVal));
                    });
        }

        @SuppressLint("DefaultLocale")
        private void bindDatePicker(EditText editText) {
            View root = editText.getRootView();

            final Calendar calendar = Calendar.getInstance();
            final DateBean db = new DateBean();

            String date = String.valueOf(editText.getText());
            if (!date.isBlank()) {
                db.fromString(date);
            } else {
                editText.setText(db.toString());
            }

            NumberPicker yearPicker = root.findViewById(R.id.date_picker_year);
            NumberPicker monthPicker = root.findViewById(R.id.date_picker_month);
            NumberPicker dayPicker = root.findViewById(R.id.date_picker_day);

            yearPicker.setMaxValue(calendar.get(Calendar.YEAR));
            yearPicker.setMinValue(1900);
            yearPicker.setValue(db.getYear());
            yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                db.setYear(newVal);
                editText.setText(db.toString());
            });

            monthPicker.setMaxValue(12);
            monthPicker.setMinValue(1);
            monthPicker.setValue(db.getMonth());

            final Calendar dayCalc = Calendar.getInstance();
            dayCalc.set(db.getYear(), db.getMonth() - 1, 1);
            monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                dayCalc.set(db.getYear(), newVal - 1, 1);
                dayPicker.setValue(1);
                dayPicker.setMaxValue(dayCalc.getActualMaximum(Calendar.DAY_OF_MONTH));
                db.setMonth(newVal);
                editText.setText(db.toString());
            });

            dayPicker.setMinValue(1);
            dayPicker.setMaxValue(dayCalc.getActualMaximum(Calendar.DAY_OF_MONTH));
            dayPicker.setValue(db.getDay());
            dayPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                db.setDay(newVal);
                editText.setText(db.toString());
            });
        }
    }
}