package com.hevaisoi.android.datasource;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hevaisoi.android.Constants;
import com.hevaisoi.android.MainActivity;
import com.hevaisoi.android.R;
import com.hevaisoi.android.model.HairModel;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ERP on 6/23/2017.
 */

public class HairAdapter extends RecyclerView.Adapter<HairAdapter.ViewHolder> implements View.OnClickListener {
    private Context _context;
    private List<HairModel> _lstHair;

    public HairAdapter(Context context, List<HairModel> lstHair) {
        this._lstHair = lstHair;
        this._context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.hair_item_layout, parent, false);
        ViewHolder holder = new ViewHolder(itemView);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(Constants.LOG_TAG, "Begin bindViewHolder hair");
        HairModel model = _lstHair.get(position);
        if (model != null) {
            Picasso.with(_context)
                   .load("file:///android_asset/"+model.getFileName()+".png")
                   .placeholder(R.drawable.ddicon)
                   .into(holder.imgHair);
            holder.imgHair.setTag(model.getId());
            holder.imgHair.setOnClickListener(this);
            holder.txtDescription.setText(model.getDescription());
        }

        Log.d(Constants.LOG_TAG, "End bindViewHolder hair");
    }

    @Override
    public int getItemCount() {

        if (_lstHair == null) {
            Log.d(Constants.LOG_TAG, "List hair return zero");
            return 0;
        }

        return _lstHair.size();
    }

    @Override
    public void onClick(View v) {
        MainActivity activity = (MainActivity) _context;
        if (activity != null) {
            int hairId = (int) v.getTag();
            activity.setHair(hairId);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHair;
        TextView txtDescription;

        public ViewHolder(View convertView) {
            super(convertView);
            imgHair = (ImageView) convertView.findViewById(R.id.imgHair);
            txtDescription = (TextView) convertView.findViewById(R.id.txtHairDescription);
        }
    }
}
