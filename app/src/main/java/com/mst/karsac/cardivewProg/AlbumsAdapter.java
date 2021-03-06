package com.mst.karsac.cardivewProg;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import com.mst.karsac.Neighbours.NeighboursActivity;
import com.mst.karsac.RatingsActivity.FinalRatings;
import com.mst.karsac.Roger.RogerActivity;
import com.mst.karsac.interest.InterestActivity;
import com.mst.karsac.R;
import com.mst.karsac.messages.InboxActivity;

/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Album> albumList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            view.setOnClickListener(new View.OnClickListener() {
                Intent intent = null;
                @Override
                public void onClick(View view) {
                    switch (title.getText().toString()){
                        case "Interest":
                            intent = new Intent(mContext, InterestActivity.class);
                            mContext.startActivity(intent);
                            break;
                        case "Messages":
                            intent = new Intent(mContext, InboxActivity.class);
                            mContext.startActivity(intent);
                            break;
                        case "Send to IP":
                            intent = new Intent(mContext, RogerActivity.class);
                            mContext.startActivity(intent);
                            break;
                        case"Device Ratings":
                            intent = new Intent(mContext, FinalRatings.class);
                            mContext.startActivity(intent);
                            break;
                    }
                }
            });
        }
    }


    public AlbumsAdapter(Context mContext, List<Album> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Album album = albumList.get(position);
        holder.title.setText(album.getName());

        // loading album cover using Glide library
        Glide.with(mContext).load(album.getThumbnail()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
