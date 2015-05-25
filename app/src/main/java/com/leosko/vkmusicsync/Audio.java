package com.leosko.vkmusicsync;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by LeoSko on 25.05.2015.
 */
public class Audio
{
    public int duration;
    public String title;
    public String artist;
    public URL url;
    public int genre;
    public int id;
    public int progress;
    public int size;

    Audio(JSONObject obj)
    {
        try
        {
            this.duration = obj.optInt("duration", 0);
            this.title = obj.optString("title", "Unknown title");
            this.artist = obj.optString("artist", "Unknown artist");
            this.url = new URL(obj.optString("url", ""));
            this.genre = obj.optInt("genre_id", 0);
            this.id = obj.optInt("id", 0);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        countSize();
    }

    Audio(int duration, String title, String artist, URL url, int genre, int id)
    {
        this.duration = duration;
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.genre = genre;
        this.id = id;
        countSize();
    }

    private void countSize()
    {
        this.progress = 0;
        AsyncTask<URL, Integer, Integer> task = new AsyncTask<URL, Integer, Integer>()
        {
            @Override
            protected Integer doInBackground(URL... params)
            {
                try
                {
                    URLConnection con = params[0].openConnection();
                    con.connect();
                    return con.getContentLength();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer)
            {
                size = integer;
                super.onPostExecute(integer);
            }
        };
        task.execute(this.url);
    }


}
