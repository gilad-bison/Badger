package com.example.badger;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.data.model.Resource;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ArrayList<Post> mDataset;
    private FeedActivity mActivity;
    private Boolean mIsPersonal;
    private String mUid;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mAuthorTextView;
        public TextView mPostDescriptionTextView;
        public ImageView mImageView;
        public Button mLikeButton;
        public ChipGroup mBadgesChipGroup;
        public ProgressBar mProgressBar;
        public AppCompatImageButton mOpenMenuButton;

        public ViewHolder(View v) {
            super(v);
            mAuthorTextView = v.findViewById(R.id.postAuthorTextView);
            mImageView = v.findViewById(R.id.imageView);
            mLikeButton = v.findViewById(R.id.likeButton);
            mPostDescriptionTextView = v.findViewById(R.id.postDescriptionTextView);
            mBadgesChipGroup = v.findViewById(R.id.badgesChipGroup);
            mProgressBar = v.findViewById(R.id.progress_bar);
            mOpenMenuButton = v.findViewById(R.id.openMenuImageButton);

        }
    }

    public PostAdapter(ArrayList<Post> myDataset, FeedActivity activity, Boolean isPersonal) {
        mDataset = myDataset;
        mActivity = activity;
        mIsPersonal = isPersonal;
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

    private void openMenu(ViewHolder holder, final Post post) {
        PopupMenu popup = new PopupMenu(holder.mOpenMenuButton.getContext(), holder.mOpenMenuButton);
        popup.getMenuInflater()
                .inflate(R.menu.image_menu, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_post:
                        mActivity.DeletePost(post);
                        return true;
                    case R.id.edit_post:
                        mActivity.EditPost(post);
                        return true;
                    default:
                        return true;
            }
        }});
        popup.show(); //showing popup menu
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post post = (Post) mDataset.get(position);
        if (!mUid.equals(post.userId)) {
            holder.mOpenMenuButton.setVisibility(View.GONE);
        }

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
                holder.mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
            }
        });

        String likeVerb = "Like";
        if (post.hasLiked) {
            likeVerb = "Unlike";
        }

        holder.mLikeButton.setText(likeVerb + " (" + post.likes + ")");
        if(post.hasLiked) {
            holder.mLikeButton.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
        } else {
            holder.mLikeButton.setBackgroundColor(mActivity.getResources().getColor(R.color.colorPrimary));
        }
        holder.mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.setLiked(post);
            }
        });

        holder.mOpenMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu(holder, post);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void addPost(Post post) {
        mDataset.add(0, post);
    }

    public void DeletePost(View view) {

    }
}