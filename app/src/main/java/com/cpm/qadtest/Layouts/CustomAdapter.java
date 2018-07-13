package com.cpm.qadtest.Layouts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cpm.qadtest.GetterSetter.UserDatum;
import com.cpm.qadtest.R;

import java.util.ArrayList;
import java.util.List;

class CustomAdapter extends BaseAdapter{

    List<UserDatum> userData = new ArrayList<>();
    Context context;
    LayoutInflater inflter;

    public CustomAdapter(Context applicationContext, List<UserDatum> userList) {
        context = applicationContext;
        userData  = userList;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return userData.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_spinner_items, null);
        TextView names = (TextView) view.findViewById(R.id.userNameTxt);
        names.setText(userData.get(i).getEmployee());
        return view;
    }
}
