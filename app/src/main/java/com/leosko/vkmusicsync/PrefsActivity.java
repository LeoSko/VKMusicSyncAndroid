package com.leosko.vkmusicsync;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.Toast;

import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;

import java.util.List;
import java.util.logging.Logger;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class PrefsActivity extends PreferenceActivity
{
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;


    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen()
    {
        if (!isSimplePreferences(this))
        {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
        /*// set the summary depending on if we are logged in
        if (VKSdk.isLoggedIn())
        {
            findPreference("pref_login").setSummary(getResources().getString(R.string.pref_login_state_online));
        }
        else
        {
            findPreference("pref_login").setSummary(getResources().getString(R.string.pref_login_state_offline));
        }*/

        findPreference("pref_login").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                boolean online = VKSdk.isLoggedIn();
                if (!online)
                {
                    VKSdk.authorize(VKScope.AUDIO);
                }
                else
                {
                    // remove everything we know about logged in user
                    VKSdk.logout();
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove("access_token").commit();
                    findPreference("pref_login").setSummary(getResources().getString(R.string.pref_login_state_offline));
                }
                return true;
            }
        });
        findPreference("pref_directory").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                DirectoryChooserDialog dirChoser = new DirectoryChooserDialog(PrefsActivity.this,
                        new DirectoryChooserDialog.ChosenDirectoryListener()
                {
                    @Override
                    public void onChosenDir(String chosenDir)
                    {
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                .putString("pref_directory", chosenDir).commit();
                    }
                });
                // we need "new folder" button, whoaoaa
                //dirChoser.setNewFolderEnabled(false);
                String curDir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString("pref_directory", Environment.getExternalStorageDirectory().toString());
                dirChoser.chooseDirectory(curDir);
                return true;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane()
    {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context)
    {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context)
    {
        return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        VKUIHelper.onResume(this);
        if (VKSdk.isLoggedIn())
        {
            findPreference("pref_login").setSummary(getResources().getString(R.string.pref_login_state_online));
        }
        else
        {
            findPreference("pref_login").setSummary(getResources().getString(R.string.pref_login_state_offline));
        }
    }
}
