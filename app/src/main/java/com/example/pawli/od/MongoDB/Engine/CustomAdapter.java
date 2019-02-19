package com.example.pawli.od.MongoDB.Engine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.pawli.od.MongoDB.Classes.Ingredient;
import com.example.pawli.od.R;
import com.example.pawli.od.MongoDB.Classes.User;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter{
    private Context mContext;
    private ArrayList<User> lstUsers;

    public CustomAdapter(Context mContext, ArrayList<User> lstUsers) {
        this.mContext = mContext;
        this.lstUsers = lstUsers;
    }

    @Override
    public int getCount() {
        return lstUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return lstUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     /*   View view = inflater.inflate(R.layout.row, null);

        TextView txtIngrednient = (TextView)view.findViewById(R.id.txtIngredient);
        txtIngrednient.setText(lstUsers.get(position).getLogin());

        return view;*/
        return null;
    }
}
