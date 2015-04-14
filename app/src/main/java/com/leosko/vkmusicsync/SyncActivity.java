package com.leosko.vkmusicsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.util.VKUtil;


public class SyncActivity extends ActionBarActivity
{
    static final String APP_ID = "4378706";
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        sp = getSharedPreferences("prefs", MODE_PRIVATE);
        VKSdk.initialize(new VKSdkListener()
        {

            @Override
            public void onCaptchaError(VKError captchaError)
            {

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
