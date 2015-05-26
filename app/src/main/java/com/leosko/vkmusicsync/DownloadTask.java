package com.leosko.vkmusicsync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LeoSko on 26.05.2015.
 */
public class DownloadTask extends AsyncTask<Audio, Integer, String>
{

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private AudioListViewAdapter listViewAdapter;
    private Audio audio;
    public static String FILE_PATH_PATTERN = "%1$s/%2$s - %3$s.mp3";
    public static String FILE_NAME_PATTERN = "%1$s - %2$s.mp3";

    public DownloadTask(Context context, AudioListViewAdapter lva)
    {
        this.context = context;
        this.listViewAdapter = lva;
    }

    @Override
    protected String doInBackground(Audio... audios)
    {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        audio = audios[0];
        try
        {
            URL url = audio.url;
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file using filepathpattern
            input = connection.getInputStream();
            String filePath = String.format(
                    FILE_PATH_PATTERN,
                    PreferenceManager.getDefaultSharedPreferences(context)
                        .getString("pref_directory", Environment.getExternalStorageDirectory().toString()),
                    audio.artist,
                    audio.title);
            output = new FileOutputStream(filePath);

            byte data[] = new byte[4096];
            int total = 0;
            int count;
            while ((count = input.read(data)) != -1)
            {
                // allow canceling with back button
                if (isCancelled())
                {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                {
                    audio.progress = total;
                    publishProgress(total);
                }
                output.write(data, 0, count);
            }
        }
        catch (Exception e)
        {
            return e.toString();
        }
        finally
        {
            try
            {
                if (output != null) output.close();
                if (input != null) input.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (connection != null)
            {
                connection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        super.onProgressUpdate(progress);
        // let UI know that we download something
        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostExecute(String result)
    {
        mWakeLock.release();
        if (result != null)
        {
            Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(context, String.format(FILE_NAME_PATTERN, audio.artist, audio.title)+ " downloaded",
                    Toast.LENGTH_SHORT).show();
        }
        listViewAdapter.notifyDataSetChanged();
    }
}