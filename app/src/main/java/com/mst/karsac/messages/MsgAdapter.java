package com.mst.karsac.messages;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import com.mst.karsac.R;

/**
 * Created by ks2ht on 3/25/2018.
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {
    Context mContext;
    List<Messages> messagesList = new ArrayList<>();

    public MsgAdapter(Context mContext, List<Messages> messagesList) {
        this.mContext = mContext;
        this.messagesList = messagesList;
    }

    @Override
    public MsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.msg_single_row, null);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MsgViewHolder holder, int position) {
        Messages msg = messagesList.get(position);
        holder.fileName.setText("Filename : " + msg.fileName);
        holder.timestamp.setText("Timestamp : " + msg.timestamp);
        holder.rating.setText("Rating : " + String.valueOf(msg.rating));

        Glide.with(mContext).load(msg.imgPath).into(holder.thumbnail);

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MSGADAPTER", "onclick");
                PopupMenu popupMenu = new PopupMenu(mContext, holder.overflow);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new MyMenuItemClickListener());
                popupMenu.show();
            }
        });


    }

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.edit:
                    Toast.makeText(mContext, "Modify Edit", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigate:
                    Toast.makeText(mContext, "Modify Navigate", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MsgViewHolder extends RecyclerView.ViewHolder{
        public TextView fileName, timestamp, rating;
        public ImageView thumbnail, overflow;

        public MsgViewHolder(View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.thumbnail);
            overflow = itemView.findViewById(R.id.overflow_icon);
            fileName = itemView.findViewById(R.id.filename);
            timestamp = itemView.findViewById(R.id.timestamp);
            rating = itemView.findViewById(R.id.ratings);
        }
    }
}
