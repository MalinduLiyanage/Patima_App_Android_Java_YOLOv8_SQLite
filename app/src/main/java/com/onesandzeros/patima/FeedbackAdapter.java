package com.onesandzeros.patima;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder>{

    private List<Feedback> feedbackList;
    private Context context;
    boolean isSummaey;
    String userName = null, outputImage = null, inputImage = null;
    SQLiteHelper dbHelper;
    int userid;

    public FeedbackAdapter(List<Feedback> feedbackList, Context context, String name, SQLiteHelper dbHelper, boolean isSummaey, int userid) {
        this.feedbackList = feedbackList;
        this.context = context;
        this.userName = name;
        this.dbHelper = dbHelper;
        this.isSummaey = isSummaey;
        this.userid = userid;
    }

    @NonNull
    @Override
    public FeedbackAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_viewfeedback, parent, false);
        return new FeedbackAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdapter.ViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);

        outputImage = dbHelper.getOutputImagepath(feedback.getImg_id());
        inputImage = dbHelper.getInputImagepath(feedback.getImg_id());
        holder.feedbackTxt.setText(feedback.getDesc());
        holder.ratingTxt.setText(String.valueOf(feedback.getRating()) + " out of 5");
        holder.usernameTxt.setText("By " + userName);

        if(isSummaey){
            holder.feedbackImg.setVisibility(View.GONE);
            holder.feedbackuserImg.setVisibility(View.VISIBLE);

            String profilepicturePath = dbHelper.getProfilepicture(userid);

            if(profilepicturePath.contains("http")){
                Picasso.get()
                        .load(profilepicturePath)
                        .placeholder(R.drawable.placeholder_profile)
                        .into(holder.feedbackuserImg);
            }else{
                File imageFile = new File(profilepicturePath);
                Picasso.get()
                        .load(imageFile)
                        .placeholder(R.drawable.placeholder_profile)
                        .into(holder.feedbackuserImg);
            }

        }else{
            if(outputImage.contains("http")){
                Picasso.get()
                        .load(outputImage)
                        .placeholder(R.drawable.placeholder_profile)
                        .into(holder.feedbackImg);
            }else{
                File imageFile = new File(outputImage);
                Picasso.get()
                        .load(imageFile)
                        .placeholder(R.drawable.ic_welcome)
                        .into(holder.feedbackImg);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ViewComparisonActivity.class);
                    intent.putExtra("imgId", feedback.getImg_id());
                    intent.putExtra("base_path", inputImage);
                    intent.putExtra("detection_path", outputImage);
                    intent.putExtra("timestamp", "Not specified");
                    context.startActivity(intent);

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView feedbackImg;
        TextView feedbackTxt, ratingTxt, usernameTxt;
        CircleImageView feedbackuserImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            feedbackImg = itemView.findViewById(R.id.feedimg_img);
            feedbackuserImg = itemView.findViewById(R.id.profile_img);
            feedbackTxt = itemView.findViewById(R.id.feedtxt_feedback);
            ratingTxt = itemView.findViewById(R.id.feedtxt_rate);
            usernameTxt = itemView.findViewById(R.id.feedtxt_username);
        }
    }
}
