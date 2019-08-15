package com.schechter.thirsty;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    private List<String> mVicinities = new ArrayList<>();
    private List<String> mNearbyPlaceNames = new ArrayList<>();
    private List<Uri> mImages = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context context, List<String> vicinities, List<String> nearbyPlaceNames, List<Uri> images) {
        mNearbyPlaceNames = nearbyPlaceNames;
        mVicinities = vicinities;
        mImages = images;
        mContext = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        Glide.with(mContext).asBitmap().load(mImages.get(position)).into(holder.image);


        holder.mainText.setText(mNearbyPlaceNames.get(position));
        holder.subText.setText(mVicinities.get(position));



        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked ");

                Toast.makeText(mContext, mNearbyPlaceNames.get(position), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {


        private CircleImageView image;
        private TextView mainText;
        private TextView subText;
        private RelativeLayout parentLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.recycler_item_image);
            mainText = itemView.findViewById(R.id.recycler_item_main_text);
            subText = itemView.findViewById(R.id.recycler_item_sub_text);
            parentLayout = itemView.findViewById(R.id.recycler_item_container);
        }


    }


}
