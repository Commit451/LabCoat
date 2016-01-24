package com.commit451.gitlab.util;

import android.content.Context;

import com.afollestad.appthemeengine.ATE;
import com.commit451.gitlab.R;

public class AppThemeUtil {

    public static void setupDefaultConfigs(Context context) {
        if (!ATE.config(context, "light_theme").isConfigured(0)) {
            ATE.config(context, "light_theme")
                    .activityTheme(R.style.AppThemeLight)
                    .primaryColorRes(R.color.primary_default)
                    .accentColorRes(R.color.accent_default)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    //.navigationViewSelectedIconRes(R.color.colorAccentLightDefault)
                    //.navigationViewSelectedTextRes(R.color.colorAccentLightDefault)
                    .commit();
        }
        if (!ATE.config(context, "dark_theme").isConfigured(0)) {
            ATE.config(context, "dark_theme")
                    .activityTheme(R.style.AppTheme)
                    .primaryColorRes(R.color.primary_default)
                    .accentColorRes(R.color.accent_default)
                    .coloredNavigationBar(true)
                    .usingMaterialDialogs(true)
                    //.navigationViewSelectedIconRes(R.color.colorAccentDarkDefault)
                    //.navigationViewSelectedTextRes(R.color.colorAccentDarkDefault)
                    .commit();
        }
    }
}
