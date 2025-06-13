package com.example.mad_project.ui.pages.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.example.mad_project.database.entities.ProfileEntity;
import com.example.mad_project.ui.BaseActivity;
import com.example.mad_project.utils.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends BaseActivity implements EmergencyContactAdapter.ContactActionListener {

    private TextInputEditText nameEdit;
    private TextInputEditText ageEdit;
    private TextInputEditText heightEdit;
    private TextInputEditText weightEdit;
    private TextInputEditText weeklyExerciseEdit;
    private TextInputEditText fitnessLevelEdit;
    private TextInputEditText yearsExperienceEdit;
    private AutoCompleteTextView genderSpinner;
    private AutoCompleteTextView bloodTypeSpinner;
    private AutoCompleteTextView experienceLevelSpinner;
    private RecyclerView emergencyContactsRecyclerView;
    private MaterialButton btnAddContact;
    private MaterialButton btnSaveProfile;
    private MaterialButton btnEditPhoto;
    private ShapeableImageView profileImage;
    private EmergencyContactAdapter contactAdapter;
    private final List<EmergencyContactEntity> tempContacts = new ArrayList<>();
    private ProfileViewModel viewModel;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Load existing profile
        viewModel.getCurrentProfile().observe(this, this::updateUI);
    }

    @Override
    protected void initViews() {
        // Initialize all views
        nameEdit = findViewById(R.id.edit_name);
        ageEdit = findViewById(R.id.edit_age);
        heightEdit = findViewById(R.id.edit_height);
        weightEdit = findViewById(R.id.edit_weight);
        weeklyExerciseEdit = findViewById(R.id.edit_weekly_exercise);
        fitnessLevelEdit = findViewById(R.id.edit_fitness_level);
        yearsExperienceEdit = findViewById(R.id.edit_years_experience);
        genderSpinner = findViewById(R.id.spinner_gender);
        bloodTypeSpinner = findViewById(R.id.spinner_blood_type);
        experienceLevelSpinner = findViewById(R.id.spinner_experience_level);
        emergencyContactsRecyclerView = findViewById(R.id.recycler_emergency_contacts);
        btnAddContact = findViewById(R.id.btn_add_contact);
        btnSaveProfile = findViewById(R.id.btn_save_profile);  // Initialize save button
        btnEditPhoto = findViewById(R.id.btn_edit_photo);
        profileImage = findViewById(R.id.profile_image);

        setupSpinners();
        setupValidation();
        setupRecyclerView();
    }


    private void setupValidation() {
        // Add input filters and validation
        fitnessLevelEdit.setFilters(new InputFilter[]{
                new InputFilterMinMax(1, 10)
        });

        TextWatcher validator = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };

        nameEdit.addTextChangedListener(validator);
        ageEdit.addTextChangedListener(validator);
        heightEdit.addTextChangedListener(validator);
        weightEdit.addTextChangedListener(validator);
        weeklyExerciseEdit.addTextChangedListener(validator);
        fitnessLevelEdit.addTextChangedListener(validator);
    }

    private void validateInputs() {
        if (btnSaveProfile == null) return;

        boolean isValid = nameEdit != null && !nameEdit.getText().toString().trim().isEmpty() &&
                ageEdit != null && !ageEdit.getText().toString().trim().isEmpty() &&
                heightEdit != null && !heightEdit.getText().toString().trim().isEmpty() &&
                weightEdit != null && !weightEdit.getText().toString().trim().isEmpty() &&
                weeklyExerciseEdit != null && !weeklyExerciseEdit.getText().toString().trim().isEmpty() &&
                fitnessLevelEdit != null && !fitnessLevelEdit.getText().toString().trim().isEmpty();

        btnSaveProfile.setEnabled(isValid);
    }

    private void updateUI(ProfileEntity profile) {
        if (profile != null) {
            nameEdit.setText(profile.getName());
            ageEdit.setText(String.valueOf(profile.getAge()));
            heightEdit.setText(String.valueOf(profile.getHeight()));
            weightEdit.setText(String.valueOf(profile.getWeight()));
            weeklyExerciseEdit.setText(String.valueOf(profile.getWeeklyExerciseHours()));
            fitnessLevelEdit.setText(String.valueOf(profile.getFitnessLevel()));
            yearsExperienceEdit.setText(String.valueOf(profile.getYearsOfExperience()));
            genderSpinner.setText(profile.getGender(), false);
            bloodTypeSpinner.setText(profile.getBloodType(), false);
            experienceLevelSpinner.setText(profile.getExperienceLevel(), false);

            if (profile.getProfilePhotoPath() != null) {
                Bitmap photo = ImageUtils.loadProfilePhoto(this, profile.getProfilePhotoPath());
                if (photo != null) {
                    profileImage.setImageBitmap(photo);
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile);
                }
            } else {
                profileImage.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    private void setupSpinners() {
        if (genderSpinner != null) {
            String[] genders = new String[]{"Male", "Female", "Other"};
            ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, genders);
            genderSpinner.setAdapter(genderAdapter);
        }

        if (bloodTypeSpinner != null) {
            String[] bloodTypes = new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
            ArrayAdapter<String> bloodTypeAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, bloodTypes);
            bloodTypeSpinner.setAdapter(bloodTypeAdapter);
        }

        if (experienceLevelSpinner != null) {
            String[] experienceLevels = new String[]{"Beginner", "Intermediate", "Advanced", "Expert"};
            ArrayAdapter<String> experienceAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, experienceLevels);
            experienceLevelSpinner.setAdapter(experienceAdapter);
        }
    }

    private void setupRecyclerView() {
        if (emergencyContactsRecyclerView != null) {
            contactAdapter = new EmergencyContactAdapter(this, this);
            emergencyContactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            emergencyContactsRecyclerView.setAdapter(contactAdapter);
        }
    }
    @Override
    protected void setupActions() {
        // Add null checks before setting listeners
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveProfile());
        }

        if (btnAddContact != null) {
            btnAddContact.setOnClickListener(v -> showAddContactDialog());
        }

        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v -> {
                // TODO: Implement photo selection
            });
        }

        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v -> pickImage());
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String fileName = ImageUtils.saveProfilePhoto(this, imageUri);
            if (fileName != null) {
                // Update profile photo
                profileImage.setImageURI(imageUri);
                // Store filename in profile entity
                viewModel.updateProfilePhoto(fileName);
            } else {
                showError("Failed to save profile photo");
            }
        }
    }

    private void saveProfile() {
        try {
            ProfileEntity profile = new ProfileEntity();
            profile.setName(nameEdit.getText().toString().trim());
            profile.setAge(Integer.parseInt(ageEdit.getText().toString().trim()));
            profile.setHeight(Float.parseFloat(heightEdit.getText().toString().trim()));
            profile.setWeight(Float.parseFloat(weightEdit.getText().toString().trim()));
            profile.setWeeklyExerciseHours(Float.parseFloat(weeklyExerciseEdit.getText().toString().trim()));
            profile.setFitnessLevel(Integer.parseInt(fitnessLevelEdit.getText().toString().trim()));
            profile.setYearsOfExperience(Integer.parseInt(yearsExperienceEdit.getText().toString().trim()));
            profile.setGender(genderSpinner.getText().toString());
            profile.setBloodType(bloodTypeSpinner.getText().toString());
            profile.setExperienceLevel(experienceLevelSpinner.getText().toString());

            // Set timestamps
            long now = System.currentTimeMillis();
            if (profile.getCreatedAt() == 0) {
                profile.setCreatedAt(now);
            }
            profile.setUpdatedAt(now);

            viewModel.saveProfile(profile).observe(this, success -> {
                if (success) {
                    showMessage("Profile saved successfully");
                    finish();
                } else {
                    showError("Failed to save profile");
                }
            });

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers");
        }
    }

    private void showMessage(String message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showError(String error) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(error)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onEditContact(EmergencyContactEntity contact) {
        // Will implement later
    }

    @Override
    public void onDeleteContact(EmergencyContactEntity contact) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this emergency contact?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    tempContacts.remove(contact);
                    contactAdapter.updateContacts(tempContacts);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSetPrimaryContact(EmergencyContactEntity contact) {
        for (EmergencyContactEntity existingContact : tempContacts) {
            existingContact.setPrimaryContact(existingContact == contact);
        }
        contactAdapter.updateContacts(tempContacts);
    }

    private void showAddContactDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_emergency_contact, null);

        TextInputEditText nameEdit = dialogView.findViewById(R.id.edit_contact_name);
        TextInputEditText relationshipEdit = dialogView.findViewById(R.id.edit_relationship);
        TextInputEditText phoneEdit = dialogView.findViewById(R.id.edit_phone);
        TextInputEditText alternativePhoneEdit = dialogView.findViewById(R.id.edit_alternative_phone);
        TextInputEditText emailEdit = dialogView.findViewById(R.id.edit_email);
        MaterialCheckBox primaryCheckbox = dialogView.findViewById(R.id.checkbox_primary_contact);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Add Emergency Contact")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    // Create a temporary contact for preview
                    EmergencyContactEntity contact = new EmergencyContactEntity();
                    contact.setName(nameEdit.getText().toString());
                    contact.setRelationship(relationshipEdit.getText().toString());
                    contact.setPhoneNumber(phoneEdit.getText().toString());
                    contact.setAlternativePhoneNumber(alternativePhoneEdit.getText().toString());
                    contact.setEmail(emailEdit.getText().toString());
                    contact.setPrimaryContact(primaryCheckbox.isChecked());

                    // For preview purposes, just add to temporary list
                    if (primaryCheckbox.isChecked()) {
                        // Remove primary status from other contacts
                        for (EmergencyContactEntity existingContact : tempContacts) {
                            existingContact.setPrimaryContact(false);
                        }
                    }
                    tempContacts.add(contact);
                    contactAdapter.updateContacts(tempContacts);
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Add validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                boolean isValid = !nameEdit.getText().toString().trim().isEmpty() &&
                        !relationshipEdit.getText().toString().trim().isEmpty() &&
                        !phoneEdit.getText().toString().trim().isEmpty();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(isValid);
            }
        };

        nameEdit.addTextChangedListener(textWatcher);
        relationshipEdit.addTextChangedListener(textWatcher);
        phoneEdit.addTextChangedListener(textWatcher);
    }

}