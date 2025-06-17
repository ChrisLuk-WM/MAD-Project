package com.example.mad_project.ui.pages.route.planning;

import static com.example.mad_project.ui.pages.route.planning.ForecastAdapter.RecommendationStatus.*;
import static com.example.mad_project.utils.Common.convertToFloat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.example.mad_project.api.WeatherRepository;
import com.example.mad_project.api.models.CurrentWeather;
import com.example.mad_project.api.models.NineDayForecast;
import com.example.mad_project.database.AppDatabase;
import com.example.mad_project.database.dao.ProfileDao;
import com.example.mad_project.database.entities.TrailEntity;
import com.example.mad_project.services.HikingRecommendationHelper;
import com.example.mad_project.services.WeatherService;
import com.example.mad_project.ui.pages.route.RouteDetailsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoutePlanningFragment extends Fragment implements ForecastAdapter.OnForecastSelectedListener {
    private static final String ARG_TRAIL_ID = "trail_id";
    private RecyclerView forecastRecycler;
    private TextView weatherDetails;
    private TextView suggestions;
    private Button btnStartHiking;
    private ForecastAdapter adapter;
    private RouteDetailsViewModel viewModel;
    private HikingRecommendationHelper hikingRecommendationHelper;
    private long trailId;
    private TrailEntity trail;
    private ProfileDao profileDao;
    private boolean isFragmentActive = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static RoutePlanningFragment newInstance(long trailId) {
        RoutePlanningFragment fragment = new RoutePlanningFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_TRAIL_ID, trailId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trailId = getArguments().getLong(ARG_TRAIL_ID, -1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive = true;
        if (adapter != null && adapter.getItemCount() == 0) {
            loadForecastData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainHandler.removeCallbacksAndMessages(null);
        forecastRecycler = null;
        weatherDetails = null;
        suggestions = null;
        btnStartHiking = null;
        adapter = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();

        // Get ViewModel and observe trail data
        viewModel = new ViewModelProvider(requireActivity()).get(RouteDetailsViewModel.class);

        // Observe trail data
        viewModel.getTrailWithImages().observe(getViewLifecycleOwner(), trailWithImages -> {
            if (trailWithImages != null) {
                trail = trailWithImages.getTrail();
                // Now you have access to the trail data
                loadForecastData();
            }
        });
    }

    private void initializeViews(View view) {
        forecastRecycler = view.findViewById(R.id.forecast_recycler);
        weatherDetails = view.findViewById(R.id.weather_details);
        suggestions = view.findViewById(R.id.suggestions);
        btnStartHiking = view.findViewById(R.id.btn_start_hiking);
        hikingRecommendationHelper = new HikingRecommendationHelper(requireContext());
        profileDao = AppDatabase.getDatabase(requireContext()).profileDao();

        btnStartHiking.setEnabled(true);
        btnStartHiking.setOnClickListener(v -> showStartHikingDialog());
    }
    private void setupRecyclerView() {
        adapter = new ForecastAdapter();
        adapter.setOnForecastSelectedListener(this);

        forecastRecycler.setLayoutManager(new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false));
        forecastRecycler.setAdapter(adapter);
    }

    private void fetchNineDayWeatherForecast() {
        if (!isAdded()) return;

        WeatherRepository repository = new WeatherRepository(requireContext());
        repository.getNineDayForecast(new WeatherRepository.WeatherCallback<NineDayForecast>() {
            @Override
            public void onSuccess(NineDayForecast forecast) {
                mainHandler.post(() -> {
                    // Check if fragment is still valid
                    if (!isAdded() || getActivity() == null) return;

                    if (forecast != null && forecast.getWeatherForecast() != null) {
                        List<ForecastItem> forecastItems = new ArrayList<>();

                        for (NineDayForecast.ForecastDay day : forecast.getWeatherForecast()) {
                            ForecastItem item = new ForecastItem();
                            HikingRecommendationHelper.HikingRecommendation recommendation = getHikingAdvise(buildWeatherConditionsString(day));

                            item.setDayOfWeek(day.getWeek());
                            item.setDate(formatDate(day.getForecastDate()));

                            ForecastItem.WeatherDetails weatherDetails = new ForecastItem.WeatherDetails();
                            weatherDetails.setForecastWeather(day.getForecastWeather());
                            weatherDetails.setMaxTemp(day.getForecastMaxTemp().getValue());
                            weatherDetails.setMinTemp(day.getForecastMinTemp().getValue());
                            weatherDetails.setWind(day.getForecastWind());
                            weatherDetails.setHumidity(day.getForecastMaxRh().getValue());
                            item.setWeatherDetails(weatherDetails);

                            item.setRecommendation(recommendation);

                            forecastItems.add(item);
                        }

                        if (adapter != null) {
                            adapter.setItems(forecastItems);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
//                mainHandler.post(() -> {
//                    // Check if fragment is still valid
//                    if (!isAdded() || getActivity() == null) return;
//
//                    // Show error message or load dummy data as fallback
//                    List<ForecastItem> dummyData = createDummyForecastData();
//                    if (adapter != null) {
//                        adapter.setItems(dummyData);
//                    }
//                });
            }
        });
    }

    private ForecastAdapter.RecommendationStatus determineRecommendationStatus(HikingRecommendationHelper.HikingRecommendation recommendation) {
        if (recommendation != null) {
            switch (recommendation.recommendationClass) {
                case 2:
                    return RECOMMENDED;
                case 1:
                    return ForecastAdapter.RecommendationStatus.CAUTION;
                case 0:
                    return ForecastAdapter.RecommendationStatus.NOT_RECOMMENDED;
                default:
                    return ForecastAdapter.RecommendationStatus.CAUTION;
            }
        }

        // Fallback logic based on weather conditions
        return ForecastAdapter.RecommendationStatus.CAUTION;
    }

    private String formatDate(String dateStr) {
        try {
            // Convert YYYYMMDD to DD MMM
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyyMMdd", Locale.CHINA);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("dd MMM", Locale.CHINA);
            java.util.Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String buildWeatherConditionsString(NineDayForecast.ForecastDay day) {
        return String.format(Locale.US,
                "Weather: %s, Temperature: %d-%d°C, Wind: %s, Humidity: %d-%d%%, PSR: %s",
                day.getForecastWeather(),
                day.getForecastMinTemp().getValue(),
                day.getForecastMaxTemp().getValue(),
                day.getForecastWind(),
                day.getForecastMinRh().getValue(),
                day.getForecastMaxRh().getValue(),
                day.getPsr()
        );
    }

    private void loadForecastData() {
        if (!isAdded()) return;

        fetchNineDayWeatherForecast();
    }

    @Override
    public void onForecastSelected(ForecastItem item, int position) {
        adapter.setSelectedPosition(position);
//        btnStartHiking.setEnabled(position == 0);
        updateWeatherDetails(item);
        updateSuggestions(item);
    }

    private HikingRecommendationHelper.HikingRecommendation getHikingAdvise(String weatherConditions) {
        if (weatherConditions.isEmpty()) return null;

        HikingRecommendationHelper.TrailProfile trailProfile = new HikingRecommendationHelper.TrailProfile(
                convertToFloat(trail.getDifficultyRating()),
                convertToFloat(trail.getLengthRating()),
                convertToFloat(trail.getDurationRating())
        );

        try {
            // Create a container for the profile data
            final HikingRecommendationHelper.HikerProfile[] hikerProfile = new HikingRecommendationHelper.HikerProfile[1];

            // Create and start the database thread
            Thread dbThread = new Thread(() -> {
                try {
                    ProfileDao.HikerProfileTuple profileTuple = profileDao.getHikerProfileDataSync(1);
                    if (profileTuple != null) {
                        hikerProfile[0] = profileTuple.toHikerProfile();
                    }
                } catch (Exception e) {
                    Log.e("WeatherCardHandler", "Database error: " + e.getMessage());
                }
            });

            dbThread.start();
            dbThread.join(); // Wait for the database operation to complete

            // If we got a profile, use it; otherwise use default
            if (hikerProfile[0] != null) {
                return hikingRecommendationHelper.getPrediction(weatherConditions, hikerProfile[0], trailProfile);
            } else {
                // Fallback to default profile
                HikingRecommendationHelper.HikerProfile defaultProfile = new HikingRecommendationHelper.HikerProfile(
                        35f,   // average age
                        75f,   // average weight in kg
                        170f,  // average height in cm
                        0.5f,  // medium fitness level
                        2f,    // 2 years experience
                        5f,    // 5 hours weekly exercise
                        500f,  // 500m max altitude
                        10f    // 10km longest hike
                );
                return hikingRecommendationHelper.getPrediction(weatherConditions, defaultProfile, trailProfile);
            }
        } catch (Exception e) {
            Log.e("WeatherCardHandler", "Error in getHikingAdvise: " + e.getMessage());
            return null;
        }

    }

    @SuppressLint("DefaultLocale")
    private void updateWeatherDetails(ForecastItem item) {
        if (item.getWeatherDetails() != null) {
            ForecastItem.WeatherDetails details = item.getWeatherDetails();
            StringBuilder weatherText = new StringBuilder();

            weatherText.append(String.format("Weather: %s\n", details.getForecastWeather()));
            weatherText.append(String.format("Temperature: %d°C - %d°C\n",
                    details.getMinTemp(), details.getMaxTemp()));
            weatherText.append(String.format("Wind: %s\n", details.getWind()));
            weatherText.append(String.format("Humidity: %d%%", details.getHumidity()));

            if (details.getWarning() != null && !details.getWarning().isEmpty()) {
                weatherText.append(String.format("\nWarning: %s", details.getWarning()));
            }

            weatherDetails.setText(weatherText.toString());
        }
    }

    private void updateSuggestions(ForecastItem item) {
        String suggestionText = "";
        if (item != null){
            suggestions.setText(item.getRecommendation().getFullRecommendation());
        }
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