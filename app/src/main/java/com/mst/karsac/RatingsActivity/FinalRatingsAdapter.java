package com.mst.karsac.RatingsActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mst.karsac.R;
import com.mst.karsac.messages.InboxActivity;
import com.mst.karsac.ratings.DeviceRating;
import com.mst.karsac.ratings.MessageRatings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinalRatingsAdapter extends RecyclerView.Adapter<FinalRatingsAdapter.RatingsViewHolder> {

    private List<DeviceRating> deviceRatingList;
    Context context;

    public FinalRatingsAdapter(List<DeviceRating> finalRatingsList, Context context) {
        this.deviceRatingList = finalRatingsList;
        this.context = context;
    }

    @Override
    public RatingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.final_rating_row, parent, false);
        return new RatingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RatingsViewHolder holder, int position) {
        final DeviceRating deviceRating = deviceRatingList.get(position);
        holder.unique_txt.setText("Unique device id: " + deviceRating.getDevice_uuid());
        holder.avg_rating_txt.setText("Average rating: " + String.valueOf(deviceRating.getDevice_average()));
        holder.linear_finalRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InboxActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(FinalRatings.FROM_FINAL_RATING, deviceRating);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {

        return deviceRatingList.size();
    }

    public class RatingsViewHolder extends RecyclerView.ViewHolder{
        public TextView unique_txt, avg_rating_txt;
        private LinearLayout linear_finalRatings;

        public RatingsViewHolder(View itemView) {
            super(itemView);
            unique_txt = itemView.findViewById(R.id.unique_id);
            avg_rating_txt = itemView.findViewById(R.id.avg_ratings);
            linear_finalRatings = itemView.findViewById(R.id.linear_finalratings);
        }
    }
}
