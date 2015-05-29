package com.leosko.vkmusicsync;

import android.content.Context;
import android.graphics.Color;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by LeoSko on 25.05.2015.
 */
public class AudioListViewAdapter extends ArrayAdapter
{
    static final String LIST_AUDIO_PATTERN = "%1$s - %2$s [%3$02d:%4$02d]";

    private Context mContext;
    private int id;
    private List<Audio> items;

    public AudioListViewAdapter(Context context, int resource, List objects)
    {
        super(context, resource, objects);
        mContext = context;
        id = resource;
        items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View mView = convertView;
        if(mView == null){
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = (TextView) mView.findViewById(R.id.textView);
        ProgressBar pb = (ProgressBar) mView.findViewById(R.id.progressBar);

        if (items.get(position) != null)
        {
            Audio a = items.get(position);
            String res = String.format(LIST_AUDIO_PATTERN, a.artist, a.title, a.duration / 60, a.duration % 60);
            text.setText(res);
            if (a.size != 0)
            {
                pb.setMax(a.size);
                pb.setProgress(a.progress);
            }
            else
            {
                pb.setProgress(0);
                pb.setMax(100);
            }
        }

        return mView;
    }
}
