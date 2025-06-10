package com.example.mad_project.ui.pages.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.example.mad_project.ui.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends BaseActivity implements EmergencyContactAdapter.ContactActionListener {
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

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void initViews() {
        // Initialize views
        genderSpinner = findViewById(R.id.spinner_gender);
        bloodTypeSpinner = findViewById(R.id.spinner_blood_type);
        experienceLevelSpinner = findViewById(R.id.spinner_experience_level);
        emergencyContactsRecyclerView = findViewById(R.id.recycler_emergency_contacts);
        btnAddContact = findViewById(R.id.btn_add_contact);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnEditPhoto = findViewById(R.id.btn_edit_photo);
        profileImage = findViewById(R.id.profile_image);

        setupSpinners();
        setupRecyclerView();

        contactAdapter = new EmergencyContactAdapter(this, this);
        emergencyContactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        emergencyContactsRecyclerView.setAdapter(contactAdapter);
    }

    private void setupSpinners() {
        // Setup gender spinner
        String[] genders = new String[]{"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, genders);
        genderSpinner.setAdapter(genderAdapter);

        // Setup blood type spinner
        String[] bloodTypes = new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        ArrayAdapter<String> bloodTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, bloodTypes);
        bloodTypeSpinner.setAdapter(bloodTypeAdapter);

        // Setup experience level spinner
        String[] experienceLevels = new String[]{"Beginner", "Intermediate", "Advanced", "Expert"};
        ArrayAdapter<String> experienceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, experienceLevels);
        experienceLevelSpinner.setAdapter(experienceAdapter);
    }

    private void setupRecyclerView() {
        emergencyContactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Set adapter for emergency contacts
    }

    @Override
    protected void setupActions() {
        btnAddContact.setOnClickListener(v -> showAddContactDialog());

        btnSaveProfile.setOnClickListener(v -> {
            // TODO: Implement save profile
        });

        btnEditPhoto.setOnClickListener(v -> {
            // TODO: Implement photo selection
        });
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