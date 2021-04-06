package com.example.hikokainotes.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.hikokainotes.R;

public class SpinnerAdapter extends BaseAdapter {
    Context context;
    int[] searchMode;
    LayoutInflater inflater;

    public SpinnerAdapter(Context applicationContext, int[] searchMode) {
        this.context = applicationContext;
        this.searchMode = searchMode;
        inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return searchMode.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_search_mode, null);
        ImageView icon = (ImageView) convertView.findViewById(R.id.imageView);
        icon.setImageResource(searchMode[position]);
        return convertView;
    }
}
