package com.example.badger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.TypedValue;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private ArrayList<Post> mDataset;
    private HomePageActivity mActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView mPostDescriptionTextView;
        public ImageView mImageView;
        public Button mLikeButton;
        public ChipGroup mBadgesChipGroup;

        public ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.textView2);
            mImageView = v.findViewById(R.id.imageView);
            mLikeButton = v.findViewById(R.id.likeButton);
            mPostDescriptionTextView = v.findViewById(R.id.postDescriptionTextView);
            mBadgesChipGroup = v.findViewById(R.id.badgesChipGroup);
        }
    }

    public ImageAdapter(ArrayList<Post> myDataset, HomePageActivity activity) {
        mDataset = myDataset;
        mActivity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_view, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Post post = (Post) mDataset.get(position);
        if (post.user != null) {
            holder.mTextView.setText(post.user.displayName);
        }
        holder.mPostDescriptionTextView.setText(post.description);
        holder.mBadgesChipGroup.removeAllViews();
        for (String badge : post.badges) {
            Chip c = new Chip(holder.mBadgesChipGroup.getContext());
            c.setText(badge);
            c.setClickable(false);
            c.setCheckable(false);
            holder.mBadgesChipGroup.addView(c);
        }

        Picasso.get().load(post.imageDownloadUrl).into(holder.mImageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addImage(Post post) {
        mDataset.add(0, post);
        notifyDataSetChanged();
    }
}