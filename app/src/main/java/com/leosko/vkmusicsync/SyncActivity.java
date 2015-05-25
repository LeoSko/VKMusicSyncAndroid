package com.leosko.vkmusicsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.getAudioListListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;


public class SyncActivity extends ActionBarActivity
{
    static final String APP_ID = "4378706";
    List<Audio> audioList;

    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        audioList = new LinkedList<Audio>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        ListView lv = (ListView) findViewById(R.id.audioListView);
        final AudioListViewAdapter lvAdapter = new AudioListViewAdapter(getApplicationContext(), R.layout.audioitem, audioList);

        lv.setAdapter(lvAdapter);

        VKSdk.initialize(new VKSdkListener()
        {
            @Override
            public void onCaptchaError(VKError captchaError)
            {
                Toast.makeText(getApplicationContext(), captchaError.errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTokenExpired(VKAccessToken expiredToken)
            {

            }

            @Override
            public void onReceiveNewToken(VKAccessToken newToken)
            {
                super.onReceiveNewToken(newToken);
                newToken.saveTokenToSharedPreferences(getApplicationContext(), "access_token");
            }

            @Override
            public void onAccessDenied(VKError authorizationError)
            {
                Log.d("onAccessDenied", authorizationError.toString());
            }
        }, APP_ID, VKAccessToken.tokenFromSharedPreferences(getApplicationContext(), "access_token"));
        Button refreshBtn = (Button) findViewById(R.id.refreshButton);
        refreshBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                v.setEnabled(false);
                VKRequest req = new VKRequest("audio.get");
                req.addExtraParameter("count", sp.getString("pref_countOfTracks", "5"));
                audioList.clear();
                lvAdapter.notifyDataSetChanged();
                req.executeWithListener(new getAudioListListener(new VKRequest.VKRequestListener()
                {
                    @Override
                    public void onComplete(VKResponse response)
                    {
                        super.onComplete(response);
                        try
                        {
                            JSONObject resp = response.json.getJSONObject("response");
                            JSONArray arr = resp.getJSONArray("items");
                            int size = arr.length();
                            for (int i = 0; i < size; i++)
                            {
                                JSONObject obj = arr.getJSONObject(i);
                                Audio na = new Audio(obj);
                                audioList.add(na);
                            }
                        }
                        catch (JSONException jsone)
                        {
                            jsone.printStackTrace();
                        }
                        lvAdapter.notifyDataSetChanged();
                        v.setEnabled(true);
                    }

                    @Override
                    public void onError(VKError error)
                    {
                        super.onError(error);
                    }
                }));
            }
        });

        Button syncBtn = (Button) findViewById(R.id.syncButton);
        syncBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            startActivity(new Intent(this, PrefsActivity.class));
            return true;
        }
        else if (id == R.id.action_exit)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        VKUIHelper.onResume(this);
        String res;
        if (VKSdk.isLoggedIn())
        {
            res = getResources().getString(R.string.state_online);
        }
        else
        {
            res = getResources().getString(R.string.state_offline);
        }
        TextView tv = (TextView)findViewById(R.id.currentState);
        tv.setText(res);
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
}
