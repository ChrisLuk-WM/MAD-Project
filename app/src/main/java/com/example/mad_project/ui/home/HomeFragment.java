package com.example.mad_project.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.mad_project.R;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        Button profileBtn = view.findViewById(R.id.btn_profile);
        Button routeBtn = view.findViewById(R.id.btn_route);
        Button statsBtn = view.findViewById(R.id.btn_statistics);

        profileBtn.setOnClickListener(v ->
                navController.navigate(R.id.action_home_to_profile));

        routeBtn.setOnClickListener(v ->
                navController.navigate(R.id.action_home_to_route));

        statsBtn.setOnClickListener(v ->
                navController.navigate(R.id.action_home_to_statistics));

        Log.d(TAG, "onViewCreated called");
    }
}