package com.leosko.vkmusicsync;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.util.LogWriter;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class PrefsActivity extends PreferenceActivity
{
    private LinkedList<String> toRemove = new LinkedList<String>();
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen()
    {
        addPreferencesFromResource(R.xml.pref_general);

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
                        .getString("pref_directory", Environment.DIRECTORY_MUSIC);
                dirChoser.chooseDirectory(curDir);
                return true;
            }
        });
        findPreference("pref_about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(PrefsActivity.this).create();
                String aboutTitle = getResources().getString(R.string.about_title);
                String aboutText = getResources().getString(R.string.about_text);
                alertDialog.setTitle(aboutTitle);
                alertDialog.setMessage(aboutText);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
                TextView tv = (TextView) alertDialog.findViewById(android.R.id.message);
                tv.setTextSize(14);
                return true;
            }
        });
        findPreference("pref_sendbug").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"leosko94@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                i.putExtra(Intent.EXTRA_TEXT, "");
                try
                {
                    startActivity(Intent.createChooser(i, getResources().getString(R.string.send_mail)));
                } catch (android.content.ActivityNotFoundException ex)
                {
                    Toast.makeText(PrefsActivity.this, getResources().getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        final Preference pRemoveNow = findPreference("pref_removeNow");
        pRemoveNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                AlertDialog removeNowAlertDialog = new AlertDialog.Builder(PrefsActivity.this).create();
                removeNowAlertDialog.setTitle(getResources().getString(R.string.remove_now_dialog_title));
                String mess = getResources().getString(R.string.remove_now_dialog_message);
                String yesBtn = getResources().getString(R.string.remove_now_dialog_yes);
                String noBtn = getResources().getString(R.string.remove_now_dialog_no);
                String res = "";
                for (String s : toRemove)
                {
                    res += s + '\n';
                }
                removeNowAlertDialog.setMessage(String.format(mess, res));
                removeNowAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, noBtn, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                removeNowAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, yesBtn, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String dir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("pref_directory", Environment.DIRECTORY_MUSIC);
                        int removed = 0;
                        LinkedList<String> notRemoved = new LinkedList<String>();
                        for (String s : toRemove)
                        {
                            File f = new File(dir + '/' + s);
                            if (f.delete())
                            {
                                removed++;
                            }
                            else
                            {
                                notRemoved.add(s);
                            }
                        }
                        if (removed < toRemove.size())
                        {
                            toRemove = notRemoved;
                            pRemoveNow.setSummary(String.format(getResources().getString(R.string.pref_remove_now_summary), toRemove.size()));
                        }
                        else
                        {
                            pRemoveNow.setEnabled(false);
                            pRemoveNow.setSummary(String.format(getResources().getString(R.string.pref_remove_now_summary), 0));
                        }
                    }
                });
                removeNowAlertDialog.show();
                TextView tv = (TextView) removeNowAlertDialog.findViewById(android.R.id.message);
                tv.setTextSize(14);
                return true;
            }
        });
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

        // scan for old audio files
        String removeNowSummary = getResources().getString(R.string.pref_remove_now_summary);
        String syncDir = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
            .getString("pref_directory", Environment.DIRECTORY_MUSIC);
        File files[] = new File(syncDir).listFiles();
        for (File f : files)
        {
            if (f.getName().endsWith(".mp3"))
            {
                toRemove.add(f.getName());
            }
        }
        for (Audio a : SyncActivity.audioList)
        {
            String fn = String.format(DownloadTask.FILE_NAME_PATTERN, a.artist, a.title) + ".mp3";
            toRemove.remove(fn);
        }
        findPreference("pref_removeNow").setSummary(String.format(removeNowSummary, toRemove.size()));
        if (toRemove.size() == 0)
        {
            findPreference("pref_removeNow").setEnabled(false);
        }
    }
}
