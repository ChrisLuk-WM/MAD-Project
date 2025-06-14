package com.example.mad_project.ui.pages.profile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.database.repository.ProfileRepository;

import java.util.List;

public class EmergencyContactViewModel extends AndroidViewModel {
    private final ProfileRepository repository;
    private final LiveData<List<EmergencyContactEntity>> emergencyContacts;
    private final LiveData<EmergencyContactEntity> primaryContact;
    private final LiveData<ProfileEntity> currentProfile;
    private final long profileId;

    public EmergencyContactViewModel(Application application, long profileId) {
        super(application);
        this.profileId = profileId;
        repository = new ProfileRepository(application);
        emergencyContacts = repository.getEmergencyContacts(profileId);
        primaryContact = repository.getPrimaryContact(profileId);
        currentProfile = repository.getProfileById(profileId);
    }

    public LiveData<ProfileEntity> getCurrentProfile() {
        return currentProfile;
    }

    public LiveData<List<EmergencyContactEntity>> getEmergencyContacts() {
        return emergencyContacts;
    }

    public LiveData<EmergencyContactEntity> getPrimaryContact() {
        return primaryContact;
    }

    public void addEmergencyContact(EmergencyContactEntity contact) {
        contact.setProfileId(profileId);
        repository.insertEmergencyContact(contact);
    }

    public void updateEmergencyContact(EmergencyContactEntity contact) {
        repository.updateEmergencyContact(contact);
    }

    public void deleteEmergencyContact(EmergencyContactEntity contact) {
        repository.deleteEmergencyContact(contact);
    }

    public void setPrimaryContact(EmergencyContactEntity contact) {
        contact.setPrimaryContact(true);
        repository.updateEmergencyContact(contact);
    }

    // Add Factory class
    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final long profileId;

        public Factory(Application application, long profileId) {
            this.application = application;
            this.profileId = profileId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(EmergencyContactViewModel.class)) {
                return (T) new EmergencyContactViewModel(application, profileId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}