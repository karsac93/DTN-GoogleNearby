package info.androidhive.cardview.interest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import info.androidhive.cardview.R;

/**
 * Created by ks2ht on 3/24/2018.
 */

public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.InterestViewHolder> {
    Context mContext;
    List<Interest> interestList;

    public InterestAdapter(Context mContext, List<Interest> interestList) {
        this.mContext = mContext;
        this.interestList = interestList;
    }

    @Override
    public InterestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.interest_single_row, null);
        return new InterestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InterestViewHolder holder, int position) {
        Interest curInterest = interestList.get(position);
        holder.interest.setText(curInterest.getInterest());
        holder.timestamp.setText("Timestamp :" + curInterest.getTimestamp());
        holder.value.setText("Value :" + String.valueOf(curInterest.getValue()));
    }

    @Override
    public int getItemCount() {
        return interestList.size();
    }

    public class InterestViewHolder extends RecyclerView.ViewHolder{
        public TextView interest, timestamp, value;
        public RelativeLayout background;
        public LinearLayout foreground;

        public InterestViewHolder(View view){
            super(view);
            interest = view.findViewById(R.id.interest_tv);
            timestamp = view.findViewById(R.id.timestamp_tv);
            value = view.findViewById(R.id.interestval_tv);
            background = view.findViewById(R.id.view_background);
            foreground = view.findViewById(R.id.view_foreground);
        }

    }

    public void removeItem(int position) {
        interestList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Interest item, int position) {
        interestList.add(position, item);
        // notify item added by position
        notifyItemChanged(position, item);
    }
}
