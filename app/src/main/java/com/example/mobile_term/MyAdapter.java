package com.example.mobile_term;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MyAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<MyData> data;

    public MyAdapter(Context ctx, ArrayList<MyData> data) {
        this.ctx = ctx;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            view = inflater.inflate(R.layout.money_log, viewGroup, false);
        }

        TextView moneyType = view.findViewById(R.id.money_type);
        moneyType.setText(data.get(i).type);

        TextView moneyCost = view.findViewById(R.id.money_cost);
        String formattedCost = NumberFormat.getNumberInstance(Locale.getDefault()).format(data.get(i).cost);
        moneyCost.setText(formattedCost);

        return view;
    }
}
