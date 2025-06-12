package com.example.mad_project.ui.pages.route.planning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.ui.pages.route.RouteDetailsViewModel;

import java.util.ArrayList;
import java.util.List;

public class RoutePlanningFragment extends Fragment implements ForecastAdapter.OnForecastSelectedListener {
    private static final String ARG_TRAIL_ID = "trail_id";
    private RecyclerView forecastRecycler;
    private TextView weatherDetails;
    private TextView suggestions;
    private Button btnStartHiking;
    private ForecastAdapter adapter;
    private RouteDetailsViewModel viewModel;
    private long trailId;

    public static RoutePlanningFragment newInstance(long trailId) {
        RoutePlanningFragment fragment = new RoutePlanningFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TRAIL_ID, trailId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        forecastRecycler = view.findViewById(R.id.forecast_recycler);
        weatherDetails = view.findViewById(R.id.weather_details);
        suggestions = view.findViewById(R.id.suggestions);
        btnStartHiking = view.findViewById(R.id.btn_start_hiking);

        // Setup RecyclerView
        setupRecyclerView();

        // Get ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RouteDetailsViewModel.class);

        // Load data
        loadForecastData();

        // Disable start button initially
        btnStartHiking.setEnabled(false);
        btnStartHiking.setOnClickListener(v -> showStartHikingDialog());
    }

    private void setupRecyclerView() {
        adapter = new ForecastAdapter();
        adapter.setOnForecastSelectedListener(this);

        forecastRecycler.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        forecastRecycler.setAdapter(adapter);
    }

    private void loadForecastData() {
        // TODO: Replace with actual data from API
        List<ForecastItem> dummyData = createDummyForecastData();
        adapter.setItems(dummyData);
    }

    @Override
    public void onForecastSelected(ForecastItem item, int position) {
        adapter.setSelectedPosition(position);
        btnStartHiking.setEnabled(position == 0);
        updateWeatherDetails(item);
        updateSuggestions(item);
    }

    private List<ForecastItem> createDummyForecastData() {
        List<ForecastItem> items = new ArrayList<>();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday"};
        String[] dates = {"13 Jun", "14 Jun", "15 Jun", "16 Jun", "17 Jun", "18 Jun", "19 Jun", "20 Jun", "21 Jun", "22 Jun"};
        ForecastAdapter.RecommendationStatus[] statuses = {
                ForecastAdapter.RecommendationStatus.RECOMMENDED,
                ForecastAdapter.RecommendationStatus.CAUTION,
                ForecastAdapter.RecommendationStatus.NOT_RECOMMENDED,
                ForecastAdapter.RecommendationStatus.RECOMMENDED,
                ForecastAdapter.RecommendationStatus.CAUTION,
                ForecastAdapter.RecommendationStatus.RECOMMENDED,
                ForecastAdapter.RecommendationStatus.RECOMMENDED,
                ForecastAdapter.RecommendationStatus.CAUTION,
                ForecastAdapter.RecommendationStatus.RECOMMENDED,
                ForecastAdapter.RecommendationStatus.NOT_RECOMMENDED
        };

        for (int i = 0; i < 10; i++) {
            ForecastItem item = new ForecastItem();
            item.setDayOfWeek(days[i]);
            item.setDate(dates[i]);
            item.setRecommendationStatus(statuses[i]);

            // Add weather details
            ForecastItem.WeatherDetails weatherDetails = new ForecastItem.WeatherDetails();
            weatherDetails.setForecastWeather("Partly Cloudy");
            weatherDetails.setMaxTemp(28 + i);
            weatherDetails.setMinTemp(22 + i);
            weatherDetails.setWind("E Force 3");
            weatherDetails.setHumidity(75 + i);
            weatherDetails.setWarning(i % 3 == 0 ? "Thunderstorm Warning" : null);
            item.setWeatherDetails(weatherDetails);

            items.add(item);
        }
        return items;
    }

    private void updateWeatherDetails(ForecastItem item) {
        // TODO: Format and display weather details
    }

    private void updateSuggestions(ForecastItem item) {
        String suggestionText = "";
        switch (item.getRecommendationStatus()) {
            case RECOMMENDED:
                suggestionText = "Great conditions for hiking! Remember to:\n" +
                        "• Bring sufficient water\n" +
                        "• Wear appropriate hiking shoes\n" +
                        "• Bring sun protection";
                break;
            case CAUTION:
                suggestionText = "Proceed with caution:\n" +
                        "• Check weather updates frequently\n" +
                        "• Bring rain gear\n" +
                        "• Consider shorter route options\n" +
                        "• Be prepared to turn back if conditions worsen";
                break;
            case NOT_RECOMMENDED:
                suggestionText = "Hiking not recommended due to weather conditions:\n" +
                        "• Consider rescheduling\n" +
                        "• Monitor weather warnings\n" +
                        "• Choose indoor activities instead";
                break;
        }
        suggestions.setText(suggestionText);
    }

    private void showStartHikingDialog() {
        // TODO: Add selected date to dialog message
        new AlertDialog.Builder(requireContext())
                .setTitle("Start Hiking")
                .setMessage("Are you ready to start hiking?")
                .setPositiveButton("Start", (dialog, which) -> {
                    viewModel.startHiking(requireContext());
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}