package com.example.mad_project.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mad_project.R;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        Button profileBtn = view.findViewById(R.id.btn_profile);
        Button routeBtn = view.findViewById(R.id.btn_route);
        Button statsBtn = view.findViewById(R.id.btn_statistics);

        profileBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_profile));

        routeBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_route));

        statsBtn.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_statistics));
    }
}