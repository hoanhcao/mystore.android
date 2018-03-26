package com.hevaisoi.android.datasource;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.hevaisoi.android.Constants;
import com.hevaisoi.android.MainActivity;
import com.hevaisoi.android.R;
import com.hevaisoi.android.model.ClothModel;
import com.hevaisoi.android.webservice.MyJsonParser;
import com.hevaisoi.android.webservice.WebServiceHelper;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ERP on 10/29/2016.
 */

public class ClothAdapter extends RecyclerView.Adapter<ClothAdapter.ViewHolder> {
    private List<ClothModel> lstProducts;
//    private int itemCount = 0;
    private int selectedCatalogId = 1;
    private Context myContext;
    private SparseBooleanArray selectedItems;

    public void setSelectedCatalogId(int selectedCatalogId) {
        this.selectedCatalogId = selectedCatalogId;
    }

    class ParseProductFeedTask extends AsyncTask<Integer, Void, List<ClothModel>> {

        @Override
        protected void onPreExecute() {
            MainActivity activity = (MainActivity) myContext;
            activity.setWaiting(true);
        }

        @Override
        protected List<ClothModel> doInBackground(Integer... args) {
            WebServiceHelper wsHelper = WebServiceHelper.getInstance();
            String strJson = wsHelper.GetJsonFromUrl(String.format(Constants.GET_CLOTH_URL, selectedCatalogId));
            MyJsonParser<ClothModel> parser = new MyJsonParser<>();
            Type collectionType = new TypeToken<List<ClothModel>>() {
            }.getType();

            return parser.parseJSON(strJson, collectionType);
        }

        @Override
        protected void onPostExecute(List<ClothModel> parsedObj) {
            MainActivity mainActivity = (MainActivity) myContext;
            mainActivity.setWaiting(false);

            if (parsedObj != null && !parsedObj.isEmpty()) {
                lstProducts.clear();
                lstProducts.addAll(parsedObj);
                handleSelection(mainActivity.getSelectedClothId());
                notifyDataSetChanged();
//                notifyItemInserted(selectedCatalogId * 9);
                Log.d(Constants.LOG_TAG, "Cloth count: " + parsedObj.size());
            } else {
                Toast.makeText(myContext, myContext.getString(R.string.product_list_empty), Toast.LENGTH_LONG).show();
            }
        }
    }
    // TODO: Need implement lazy loading in the future
/*
    class ParseProductCount extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected void onPreExecute() {
            MainActivity activity = (MainActivity) myContext;
            activity.setWaiting(true);
        }

        @Override
        protected Integer doInBackground(Integer... args) {
            WebServiceHelper wsHelper = WebServiceHelper.getInstance();
            String strJson = wsHelper.GetJsonFromUrl(Constants.GET_ITEM_COUNT);
            String result = strJson.substring(strJson.lastIndexOf(":") + 1, strJson.lastIndexOf("}"));
            Log.d(Constants.LOG_TAG, "itemCount: " + result);
            return Integer.parseInt(result);
        }

        @Override
        protected void onPostExecute(Integer parsedObj) {
            MainActivity mainActivity = (MainActivity) myContext;
            mainActivity.setWaiting(false);
            itemCount = parsedObj;
        }
    }
*/

    public ClothAdapter(Context context, int selectedCatalogId) {
        this.lstProducts = new ArrayList<>();
        this.myContext = context;
        this.selectedCatalogId = selectedCatalogId;
        selectedItems = new SparseBooleanArray();
//        new_flag ParseProductCount().execute();
        new ParseProductFeedTask().execute();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.product_item_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position >= lstProducts.size()) return;

        ClothModel item = lstProducts.get(position);
        if (item != null) {
//            DecimalFormat decimalFormat = new DecimalFormat("###,###.#");
//            String price = String.format(myContext.getString(R.string.product_unit_price), decimalFormat.format(item.getPrice()));
//            holder.txtPrice.setText(price);

            Picasso.with(myContext)
                   .load(item.getImgUrl())
                   .placeholder(R.drawable.ddicon)
                   .into(holder.imgPrd);
            holder.txtCode.setText(item.getCode());
            if (item.isNew()){
                holder.imgNew.setVisibility(View.VISIBLE);
            } else {
                holder.imgNew.setVisibility(View.INVISIBLE);
            }
            if (selectedItems.get(item.getId(), false)) {
                holder.imgChecked.setVisibility(View.VISIBLE);
            } else {
                holder.imgChecked.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return lstProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgPrd;
        ImageView imgChecked;
        ImageView imgNew;
//        TextView txtPrice;
        TextView txtCode;
        public ViewHolder(View convertView) {
            super(convertView);
            imgPrd = (ImageView) convertView.findViewById(R.id.imgProduct);
            imgPrd.setOnClickListener(this);
//            txtPrice = (TextView) convertView.findViewById(R.id.txtPrice);
            imgChecked = (ImageView) convertView.findViewById(R.id.img_checked_cloth);
            txtCode = (TextView) convertView.findViewById(R.id.txtClothCode);
            imgNew = (ImageView) convertView.findViewById(R.id.img_new_cloth);
        }

        @Override
        public void onClick(View v) {
            int clothId = lstProducts.get(getAdapterPosition()).getId();
            MainActivity activity = (MainActivity) myContext;
            activity.setCloth(lstProducts.get(getAdapterPosition()));
            notifyDataSetChanged();
            handleSelection(clothId);
        }
    }

    public void handleSelection(int clothId) {
        selectedItems.clear();
        for (int i = 0; i < lstProducts.size(); i++) {
            if (lstProducts.get(i) != null && lstProducts.get(i).getId() == clothId) {
                selectedItems.put(lstProducts.get(i).getId(), true);
            } else {
                selectedItems.put(lstProducts.get(i).getId(), false);
            }
        }
    }
}
