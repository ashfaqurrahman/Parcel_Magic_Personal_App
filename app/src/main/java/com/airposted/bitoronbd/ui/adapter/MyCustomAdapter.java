package com.airposted.bitoronbd.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airposted.bitoronbd.R;
import com.airposted.bitoronbd.model.ContactsInfo;

import java.util.ArrayList;
import java.util.List;

public class MyCustomAdapter extends ArrayAdapter {

    private ArrayList<String> contactsInfoList;
    private Context context;

    public MyCustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        this.contactsInfoList = objects;
        this.context = context;
    }

    private class ViewHolder {
        TextView displayName;
        TextView phoneNumber;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.contact_info, null);

            holder = new ViewHolder();
            //holder.displayName = (TextView) convertView.findViewById(R.id.displayName);
            holder.phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //ContactsInfo contactsInfo = (ContactsInfo) contactsInfoList.get(position);
        //holder.displayName.setText(contactsInfo.getDisplayName());
        holder.phoneNumber.setText(contactsInfoList.get(position));

        return convertView;
    }
}
