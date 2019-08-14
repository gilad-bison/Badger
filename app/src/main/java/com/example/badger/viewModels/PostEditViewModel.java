package com.example.badger.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class PostEditViewModel extends ViewModel {
    private MutableLiveData<String> mDescription;
    private MutableLiveData<ArrayList<String>> mBadges;

    public PostEditViewModel() {
        mDescription = new MutableLiveData<>();
        mBadges = new MutableLiveData<>();
    }

    public LiveData<String> getDescription() {
        return mDescription;
    }

    public LiveData<ArrayList<String>> getBadges() {
        return mBadges;
    }

    public void setDescription(String description) {
        if (description.equals(mDescription.getValue())) {
            return;
        }

        mDescription.setValue(description);
    }

    public void setDescriptionIfNotInitialized(String description) {
        if (mDescription.getValue() != null) {
            setDescription(description);
        }
    }

    public void setBadgesIfNotInitialized(ArrayList<String> badges) {
        if (mBadges.getValue() != null) {
            setBadges(badges);
        }
    }

    public void setBadges(ArrayList<String> badges) {
        if (badges.equals(mBadges.getValue())) {
            return;
        }

        mBadges.setValue(badges);
    }

}
