package com.onesandzeros.patima;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder>{

    private List<Location> locationList;
    private Context context;
    SQLiteHelper dbHelper;
    String picturePath = null;

    public LocationAdapter(List<Location> locationList, Context context, SQLiteHelper dbHelper) {
        this.locationList = locationList;
        this.context = context;
        this.dbHelper = dbHelper;
    }


    @NonNull
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_image, parent, false);
        return new LocationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationAdapter.ViewHolder holder, int position) {

        Location location = locationList.get(position);
        picturePath = dbHelper.getOutputImagepath(location.getImg_id());

        if(picturePath.contains("http")){
            Picasso.get()
                    .load(picturePath)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(holder.objImg);
        }else{
            File imageFile = new File(picturePath);
            Picasso.get()
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(holder.objImg);
        }

    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView objImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            objImg = itemView.findViewById(R.id.obj_img);
        }
    }
}
