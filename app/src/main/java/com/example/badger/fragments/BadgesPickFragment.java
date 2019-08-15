package com.example.badger.fragments;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.badger.R;
import com.example.badger.viewModels.PostEditViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class BadgesPickFragment extends Fragment {

    private PostEditViewModel mViewModel;
    private ChipGroup mBadgesChipGroup;

    public static BadgesPickFragment newInstance() {
        return new BadgesPickFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.badges_pick_fragment, container, false);
    }

    private ArrayList<String> getBadgesFromUI() {
        ArrayList<String> badges = new ArrayList<>();
        for (int i = 0; i < mBadgesChipGroup.getChildCount(); i++) {
            Chip currentChip  = (Chip)mBadgesChipGroup.getChildAt(i);
            if (currentChip.isChecked()) {
                badges.add(currentChip.getText().toString());
            }
        }

        return badges;
    }

    private void populateBadges(List<String> currentBadges) {
        if (currentBadges == null || currentBadges.size() == 0) {
            return;
        }

        for (String currBadge : currentBadges) {
            for (int i = 0; i < mBadgesChipGroup.getChildCount(); i++) {
                Chip currentChip  = (Chip)mBadgesChipGroup.getChildAt(i);
                if (currentChip.getText().toString().equals(currBadge)) {
                    currentChip.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this.getActivity()).get(PostEditViewModel.class);
        mBadgesChipGroup = getView().findViewById(R.id.filter_chip_group);
        for (int i = 0; i < mBadgesChipGroup.getChildCount(); i++) {
            Chip currentChip  = (Chip)mBadgesChipGroup.getChildAt(i);
            currentChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mViewModel.setBadges(getBadgesFromUI());
                }
            });
        }

        Bundle args = getArguments();
        ArrayList<String> currentBadges = args.getStringArrayList("badges");
        mViewModel.setBadgesIfNotInitialized(currentBadges);
        populateBadges(currentBadges);

    }

    public void disableView() {
        mBadgesChipGroup.setEnabled(false);
    }
}
