package com.example.mad_project.ui.pages.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.EmergencyContactEntity;
import com.example.mad_project.database.entities.ProfileEntity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class EmergencySheetFragment extends BottomSheetDialogFragment implements EmergencyContactAdapter.ContactActionListener{
    private View rootView;
    private EmergencyContactViewModel viewModel;
    private DialogInterface.OnDismissListener dismissListener;
    private EmergencyContactAdapter contactAdapter;
    private TextView bloodTypeText;
    private TextView ageText;
    private RecyclerView contactsRecyclerView;

    private TextView nameText;
    private TextView weightText;
    private TextView healthConditionsText;
    private TextView medicationsText;
    private TextView allergiesText;
    private MaterialButton btnCallEmergency;
    private MaterialButton btnCallRescue;

    public static EmergencySheetFragment newInstance() {
        return new EmergencySheetFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_App_BottomSheetDialog);

        // Initialize ViewModel with current profile ID
        EmergencyContactViewModel.Factory factory =
                new EmergencyContactViewModel.Factory(requireActivity().getApplication(), getCurrentProfileId());
        viewModel = new ViewModelProvider(this, factory).get(EmergencyContactViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.layout_emergency_info, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> dismiss());

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupBottomSheet(view);
        initializeViews();
        setupRecyclerView();
        observeData();
    }

    private void setupBottomSheet(View view) {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
    }

    private void setupRecyclerView() {
        contactAdapter = new EmergencyContactAdapter(requireContext(), this);
        contactsRecyclerView.setAdapter(contactAdapter);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void observeData() {
        // Observe emergency contacts
        viewModel.getEmergencyContacts().observe(getViewLifecycleOwner(), contacts -> {
            if (contacts != null && !contacts.isEmpty()) {
                contactAdapter.updateContacts(contacts);
                contactsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                contactsRecyclerView.setVisibility(View.GONE);
                // Show empty state for contacts
                showEmptyContactsState();
            }
        });

        // Observe profile data for personal information
        viewModel.getCurrentProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                updatePersonalInfo(profile);
            } else {
                showEmptyPersonalInfo();
            }
        });
    }

    private void showEmptyContactsState() {
        // Add a TextView to your layout for this
        View emptyContactsView = rootView.findViewById(R.id.text_empty_contacts);
        if (emptyContactsView != null) {
            emptyContactsView.setVisibility(View.VISIBLE);
        }
    }

    private void updatePersonalInfo(ProfileEntity profile) {
        if (profile != null) {
            // Name
            nameText.setText(getString(R.string.name_format, profile.getName()));
            nameText.setVisibility(View.VISIBLE);

            // Age
            if (profile.getAge() > 0) {
                ageText.setText(getString(R.string.age_format, profile.getAge()));
                ageText.setVisibility(View.VISIBLE);
            } else {
                ageText.setVisibility(View.GONE);
            }

            // Blood Type
            if (profile.getBloodType() != null && !profile.getBloodType().isEmpty()) {
                bloodTypeText.setText(getString(R.string.blood_type_format, profile.getBloodType()));
                bloodTypeText.setVisibility(View.VISIBLE);
            } else {
                bloodTypeText.setVisibility(View.GONE);
            }

            // Weight
            if (profile.getWeight() > 0) {
                weightText.setText(getString(R.string.weight_format, profile.getWeight()));
                weightText.setVisibility(View.VISIBLE);
            } else {
                weightText.setVisibility(View.GONE);
            }

            // Show empty state if no data is available
            boolean hasNoData = profile.getAge() <= 0
                    && (profile.getBloodType() == null || profile.getBloodType().isEmpty())
                    && profile.getWeight() <= 0;

            if (hasNoData) {
                showEmptyPersonalInfo();
            }
        } else {
            showEmptyPersonalInfo();
        }
    }

    private void showEmptyPersonalInfo() {
        bloodTypeText.setVisibility(View.GONE);
        ageText.setVisibility(View.GONE);

        // Show empty state message
        View emptyInfoView = rootView.findViewById(R.id.text_empty_personal_info);
        if (emptyInfoView != null) {
            emptyInfoView.setVisibility(View.VISIBLE);
        }
    }

    private long getCurrentProfileId() {
        // Get this from your profile management system
        // For now, return a default value or get from shared preferences
        return 1L; // Replace with actual implementation
    }

    private void initializeViews() {
        // Existing views
        bloodTypeText = rootView.findViewById(R.id.text_blood_type);
        ageText = rootView.findViewById(R.id.text_age);
        contactsRecyclerView = rootView.findViewById(R.id.recycler_emergency_contacts);

        // New views
        nameText = rootView.findViewById(R.id.text_name);
        weightText = rootView.findViewById(R.id.text_weight);
        healthConditionsText = rootView.findViewById(R.id.text_health_conditions);
        medicationsText = rootView.findViewById(R.id.text_medications);
        allergiesText = rootView.findViewById(R.id.text_allergies);

        btnCallEmergency = rootView.findViewById(R.id.btn_call_emergency);
        btnCallRescue = rootView.findViewById(R.id.btn_call_rescue);

        setupEmergencyButtons();
    }

    private void setupEmergencyButtons() {
        btnCallEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:999"));
            startActivity(intent);
        });

        btnCallRescue.setOnClickListener(v -> {
            // Add your mountain rescue number
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:YOUR_RESCUE_NUMBER"));
            startActivity(intent);
        });
    }

    private void loadEmergencyData() {
        // Load emergency contact data, personal info, etc.
    }

    @Override
    public void onEditContact(EmergencyContactEntity contact) {
        // Handle edit contact
    }

    @Override
    public void onDeleteContact(EmergencyContactEntity contact) {
        viewModel.deleteEmergencyContact(contact);
    }

    @Override
    public void onSetPrimaryContact(EmergencyContactEntity contact) {
        viewModel.setPrimaryContact(contact);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null) {
            dismissListener.onDismiss(dialog);
        }
    }
}