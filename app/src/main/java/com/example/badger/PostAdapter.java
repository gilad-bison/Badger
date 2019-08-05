package com.example.badger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ArrayList<Post> mDataset;
    private FeedActivity mActivity;
    private int mFinishedLoadingImages;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mAuthorTextView;
        public TextView mPostDescriptionTextView;
        public ImageView mImageView;
        public Button mLikeButton;
        public ChipGroup mBadgesChipGroup;

        public ViewHolder(View v) {
            super(v);
            mAuthorTextView = v.findViewById(R.id.postAuthorTextView);
            mImageView = v.findViewById(R.id.imageView);
            mLikeButton = v.findViewById(R.id.likeButton);
            mPostDescriptionTextView = v.findViewById(R.id.postDescriptionTextView);
            mBadgesChipGroup = v.findViewById(R.id.badgesChipGroup);
        }
    }

    public PostAdapter(ArrayList<Post> myDataset, FeedActivity activity) {
        mDataset = myDataset;
        mActivity = activity;
        mFinishedLoadingImages = 0;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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
            holder.mAuthorTextView.setText("By " + post.user.displayName);
        }
        holder.mPostDescriptionTextView.setText(post.description);
        holder.mBadgesChipGroup.removeAllViews();
        if (post.badges != null) {
            for (String badge : post.badges) {
                Chip c = new Chip(holder.mBadgesChipGroup.getContext());
                c.setText(badge);
                c.setClickable(false);
                c.setCheckable(false);
                holder.mBadgesChipGroup.addView(c);
            }
        }


        Picasso.get().load(post.imageDownloadUrl).into(holder.mImageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                mFinishedLoadingImages++;
                if (mFinishedLoadingImages == mDataset.size()) {
                    mActivity.onAllImagesLoaded();
                }
            }

            @Override
            public void onError(Exception e) {
                int x = 2;
            }
        });
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