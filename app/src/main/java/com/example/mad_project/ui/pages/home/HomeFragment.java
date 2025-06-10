package com.example.mad_project.ui.pages.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mad_project.R;
import com.example.mad_project.ui.BaseActivity;

public class HomeFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).hideMainToolbar();
        }

        // Ensure child fragments are properly initialized
        if (getChildFragmentManager().findFragmentById(R.id.dashboard_container) == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.dashboard_container, new DashboardFragment())
                    .commit();
        }

        if (getChildFragmentManager().findFragmentById(R.id.bottom_nav_container) == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.bottom_nav_container, new BottomNavFragment())
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showMainToolbar();
        }
    }
}