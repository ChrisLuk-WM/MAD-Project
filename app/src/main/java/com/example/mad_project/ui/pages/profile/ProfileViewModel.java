package com.example.mad_project.ui.pages.profile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.database.repository.ProfileRepository;
import com.example.mad_project.utils.ImageUtils;

public class ProfileViewModel extends AndroidViewModel {
    private final ProfileRepository repository;
    private Long currentProfileId;

    public ProfileViewModel(Application application) {
        super(application);
        repository = new ProfileRepository(application);
    }

    public LiveData<ProfileEntity> getCurrentProfile() {
        return repository.getCurrentProfile();
    }

    public LiveData<Boolean> saveProfile(ProfileEntity profile) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        repository.insert(profile);
        result.setValue(true);
        return result;
    }

    public void updateProfilePhoto(String photoPath) {
        ProfileEntity currentProfile = getCurrentProfile().getValue();
        if (currentProfile != null) {
            // Delete old photo if exists
            if (currentProfile.getProfilePhotoPath() != null) {
                ImageUtils.deleteProfilePhoto(getApplication(),
                        currentProfile.getProfilePhotoPath());
            }
            currentProfile.setProfilePhotoPath(photoPath);
            repository.update(currentProfile);
            currentProfileId = currentProfile.getId();
        }
    }
}