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
import com.google.android.material.navigation.NavigationView;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private NavController navController;

    // UI Components
    private MaterialCardView cardProfile;
    private MaterialCardView cardRoute;
    private MaterialCardView cardStatistics;
    private Toolbar fragmentToolbar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        // Hide the base activity's toolbar
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).hideMainToolbar();
        }

        // Setup this fragment's toolbar
        fragmentToolbar = view.findViewById(R.id.toolbar_layout);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(fragmentToolbar);

        // Enable drawer toggle
        DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                requireActivity(),
                drawerLayout,
                fragmentToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navController = Navigation.findNavController(view);

        initializeViews(view);
        setupClickListeners();
    }

    private void initializeViews(View view) {
        cardProfile = view.findViewById(R.id.card_profile);
        cardRoute = view.findViewById(R.id.card_route);
        cardStatistics = view.findViewById(R.id.card_statistics);

        // Add animation to cards
        addCardAnimations(cardProfile);
        addCardAnimations(cardRoute);
        addCardAnimations(cardStatistics);
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
                    // Scale down the card when pressed
                    card.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Scale back to original size
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show the base activity's toolbar when leaving this fragment
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showMainToolbar();
        }
    }
}