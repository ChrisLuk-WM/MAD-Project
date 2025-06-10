package com.example.mad_project.ui.pages.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mad_project.ui.BaseActivity;
import com.google.android.material.card.MaterialCardView;
import com.example.mad_project.R;

public class BottomNavFragment extends Fragment {
    private MaterialCardView cardProfile;
    private MaterialCardView cardRoute;
    private MaterialCardView cardStatistics;
    private NavController navController;

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

        // Add animations to cards
        if (cardProfile != null) addCardAnimations(cardProfile);
        if (cardRoute != null) addCardAnimations(cardRoute);
        if (cardStatistics != null) addCardAnimations(cardStatistics);
    }

    private void setupClickListeners() {
        cardProfile.setOnClickListener(v -> {
            cardProfile.setPressed(true);
            navController.navigate(R.id.action_home_to_profile);
        });

        cardRoute.setOnClickListener(v -> {
            cardRoute.setPressed(true);
            navController.navigate(R.id.action_home_to_route);
        });

        cardStatistics.setOnClickListener(v -> {
            cardStatistics.setPressed(true);
            navController.navigate(R.id.action_home_to_statistics);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addCardAnimations(MaterialCardView card) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    card.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                    break;
            }
            return false;
        });
    }
}