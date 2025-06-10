package com.example.mad_project.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.mad_project.database.entities.EmergencyContactEntity;

import java.util.List;

@Dao
public interface EmergencyContactDao {
    @Insert
    long insert(EmergencyContactEntity contact);

    @Update
    void update(EmergencyContactEntity contact);

    @Delete
    void delete(EmergencyContactEntity contact);

    @Query("SELECT * FROM emergency_contacts WHERE profileId = :profileId")
    LiveData<List<EmergencyContactEntity>> getContactsForProfile(long profileId);

    @Query("SELECT * FROM emergency_contacts WHERE profileId = :profileId AND isPrimaryContact = 1")
    LiveData<EmergencyContactEntity> getPrimaryContact(long profileId);

    @Query("DELETE FROM emergency_contacts WHERE profileId = :profileId")
    void deleteAllContactsForProfile(long profileId);
}