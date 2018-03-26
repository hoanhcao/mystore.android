package com.hevaisoi.android.datasource;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hevaisoi.android.MainActivity;
import com.hevaisoi.android.R;

import java.util.List;

/**
 * Created by ERP on 7/11/2017.
 */
public class TrouserColorAdapter extends RecyclerView.Adapter<TrouserColorAdapter.ViewHolder> {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View interView = inflater.inflate(R.layout.trouser_color_item_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(interView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = _colors.get(position);
        if (item != null) {
            holder.txtColor.setBackgroundColor(Color.parseColor(item));
            holder.txtColor.setTag(position);
            if (selectedItems.get(position, false)) {
                holder.imgChecked.setVisibility(View.VISIBLE);
            } else {
                holder.imgChecked.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (_colors == null) {
            return 0;
        }
        return _colors.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtColor;
        ImageView imgChecked;
        RelativeLayout layout;

        public ViewHolder(View convertView) {
            super(convertView);
            layout = (RelativeLayout) convertView.findViewById(R.id.color_group_item);
            layout.setOnClickListener(this);
            txtColor = (TextView) convertView.findViewById(R.id.txtTrouserColor);
            imgChecked = (ImageView) convertView.findViewById(R.id.img_checked_color);
        }

        @Override
        public void onClick(View v) {
            handleSelection(getAdapterPosition());
            MainActivity activity = (MainActivity) _context;
            if (activity != null) {
               activity.setTrouserColor(_colors.get(getAdapterPosition()));
            }
            notifyDataSetChanged();
        }
    }

    public void handleSelection(int position) {
        selectedItems.clear();
        for (int i = 0; i <= _colors.size(); i++) {
            if (i == position) {
                selectedItems.put(i, true);
            } else {
                selectedItems.put(i, false);
            }
        }
    }

    private List<String> _colors;
    private Context _context;
    private SparseBooleanArray selectedItems;
    public TrouserColorAdapter(Context context, List<String> colors, String selectedColor) {
        _colors = colors;
        _context = context;
        selectedItems = new SparseBooleanArray();
        int selectedIndex = colors.lastIndexOf(selectedColor);
        handleSelection(selectedIndex);
    }
}
