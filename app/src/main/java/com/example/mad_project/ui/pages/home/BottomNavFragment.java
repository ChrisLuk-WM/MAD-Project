package com.example.mad_project.ui.pages.home;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mad_project.R;

public class BottomNavFragment extends Fragment {
    private LinearLayout cardProfile;
    private LinearLayout cardRoute;
    private LinearLayout cardStatistics;
    private NavController navController;
    private View currentSelectedView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_nav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        initializeViews(view);
        setupClickListeners();
    }

    private void initializeViews(View view) {
        cardProfile = view.findViewById(R.id.card_profile);
        cardRoute = view.findViewById(R.id.card_route);
        cardStatistics = view.findViewById(R.id.card_statistics);

        // Add animations to buttons
        if (cardProfile != null) addButtonAnimations(cardProfile);
        if (cardRoute != null) addButtonAnimations(cardRoute);
        if (cardStatistics != null) addButtonAnimations(cardStatistics);
    }

    private void setupClickListeners() {
        cardProfile.setOnClickListener(v -> {
            animatePress(cardProfile);
            navController.navigate(R.id.action_home_to_profile);
        });

        cardRoute.setOnClickListener(v -> {
            animatePress(cardRoute);
            navController.navigate(R.id.action_home_to_route);
        });

        cardStatistics.setOnClickListener(v -> {
            animatePress(cardStatistics);
            navController.navigate(R.id.action_home_to_statistics);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addButtonAnimations(View button) {
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ObjectAnimator.ofFloat(v, "alpha", 1f, 0.7f)
                            .setDuration(100)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    ObjectAnimator.ofFloat(v, "alpha", 0.7f, 1f)
                            .setDuration(100)
                            .start();
                    break;
            }
            return false;
        });
    }

    private void animatePress(View view) {
        // Reset previous selection if exists
        if (currentSelectedView != null) {
            currentSelectedView.setAlpha(1f);
        }

        // Animate new selection
        ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f)
                .setDuration(100)
                .start();

        currentSelectedView = view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset any active selections
        if (currentSelectedView != null) {
            currentSelectedView.setAlpha(1f);
            currentSelectedView = null;
        }
    }
}