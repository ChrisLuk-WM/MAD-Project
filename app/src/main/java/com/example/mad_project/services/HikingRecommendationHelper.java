package com.example.mad_project.services;

import static com.example.mad_project.services.RecommendationDetails.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.example.mad_project.utils.Common;

public class HikingRecommendationHelper {
    private int weatherInputIndex = 0;
    private int hikerInputIndex = 1;
    private int trailInputIndex = 2;
    private static final TrailProfile DEFAULT_TRAIL = new TrailProfile(2.0f, 5.0f, 2.5f);
    private static final int WEATHER_INPUT_LENGTH = 30;
    private static final int HIKER_FEATURES_COUNT = 8;
    private static final int TRAIL_FEATURES_COUNT = 3;
    private static final int NUM_CLASSES = 3;
    private static final String TAG = "HikingHelper";

    // Model selection - change this to switch between models
    private static final boolean USE_FLOAT_MODEL = true; // Set to false for quantized
    private static final String MODEL_PATH = USE_FLOAT_MODEL ?
            "hiking_model_float.tflite" : "hiking_model_quantized.tflite";


    private final Context context;
    private Interpreter tflite;
    private final double[] trailMeans;
    private final double[] trailScales;
    private final double[] hikerMeans;
    private final double[] hikerScales;
    private final Map<String, Integer> vocabulary;
    private final int vocabSize;
    private final int oovIndex;

    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^a-z0-9\\s]");

    public HikingRecommendationHelper(Context context) {
        this.context = context;

        // Load vocabulary (same as before)
        JSONObject vocabJson = Common.loadJsonFromAsset(context, "vocabulary.json");
        vocabulary = parseVocabulary(vocabJson.optJSONObject("vocabulary"));
        vocabSize = vocabJson.optInt("vocab_size", 5000);
        oovIndex = vocabJson.optInt("oov_index", 1);

        // Load hiker scaling parameters
        JSONObject hikerScalerParams = Common.loadJsonFromAsset(context, "hiker_scaler_params.json");
        hikerMeans = parseDoubleArray(hikerScalerParams.optJSONArray("mean"));
        hikerScales = parseDoubleArray(hikerScalerParams.optJSONArray("scale"));

        // Load trail scaling parameters - NEW
        JSONObject trailScalerParams = Common.loadJsonFromAsset(context, "trail_scaler_params.json");
        trailMeans = parseDoubleArray(trailScalerParams.optJSONArray("mean"));
        trailScales = parseDoubleArray(trailScalerParams.optJSONArray("scale"));

        // Verify dimensions
        if (hikerMeans.length != HIKER_FEATURES_COUNT || hikerScales.length != HIKER_FEATURES_COUNT) {
            throw new RuntimeException("Hiker scaler parameter mismatch");
        }

        if (trailMeans.length != TRAIL_FEATURES_COUNT || trailScales.length != TRAIL_FEATURES_COUNT) {
            throw new RuntimeException("Trail scaler parameter mismatch");
        }

        // Load model
        tflite = loadModel(MODEL_PATH);
    }

    public HikingRecommendation getPrediction(String weatherDescription, HikerProfile hikerProfile) {
        try {
            // Log.d(TAG, "=== PREDICTION START (General Path) ===");
            // Log.d(TAG, "Using default trail: difficulty=2.0, length=5.0km, duration=2.5hrs");

            // Use the existing method with default trail
            HikingRecommendation baseRecommendation = getPrediction(weatherDescription, hikerProfile, DEFAULT_TRAIL);

            // Generate a more general recommendation without specific trail details
            String generalRecommendation = generateGeneralRecommendation(
                    baseRecommendation.allProbabilities,
                    weatherDescription,
                    hikerProfile
            );

            // Return new recommendation with general advice
            return new HikingRecommendation(
                    baseRecommendation.recommendationClass,
                    baseRecommendation.confidence,
                    baseRecommendation.recommendationText,
                    generalRecommendation,
                    baseRecommendation.allProbabilities
            );
        } catch (Exception e) {
            Log.e(TAG, "Error making general prediction", e);
            throw new RuntimeException("Failed to make prediction: " + e.getMessage(), e);
        }
    }

    public HikingRecommendation getPrediction(String weatherDescription,
                                              HikerProfile hikerProfile,
                                              TrailProfile trailProfile) {
        try {
            Log.d(TAG, "=== PREDICTION START ===");
            Log.d(TAG, "Model: " + MODEL_PATH);
            Log.d(TAG, "Weather: " + weatherDescription);
            Log.d(TAG, String.format("Trail: difficulty=%.1f, length=%.1f km, duration=%.1f hrs",
                    trailProfile.difficulty, trailProfile.lengthKm, trailProfile.durationHours));

            // Prepare inputs
            int[][] weatherInput = tokenizeWeatherDescription(weatherDescription);
            float[][] hikerInput = normalizeHikerProfile(hikerProfile);
            float[][] trailInput = normalizeTrailProfile(trailProfile);

            // Log inputs
            Log.d(TAG, "Weather tokens (first 10): " +
                    Arrays.toString(Arrays.copyOfRange(weatherInput[0], 0, 10)));
            Log.d(TAG, "Hiker normalized: " + Arrays.toString(hikerInput[0]));
            Log.d(TAG, "Trail normalized: " + Arrays.toString(trailInput[0]));

            float[][] output = new float[1][NUM_CLASSES];

            // Create input array in the correct order based on detected indices
            Object[] inputs = new Object[3];
            inputs[weatherInputIndex] = weatherInput;
            inputs[hikerInputIndex] = hikerInput;
            inputs[trailInputIndex] = trailInput;

            // Create output map
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, output);

            // Run inference
            tflite.runForMultipleInputsOutputs(inputs, outputs);

            Log.d(TAG, String.format("Model output: [%.6f, %.6f, %.6f]",
                    output[0][0], output[0][1], output[0][2]));

            // Process output
            int predictedClass = argmax(output[0]);
            float confidence = output[0][predictedClass];

            // Generate detailed recommendation with trail info
            String detailedRecommendation = generateDetailedRecommendation(
                    output[0], weatherDescription, hikerProfile, trailProfile);

            return new HikingRecommendation(
                    predictedClass,
                    confidence,
                    getRecommendationText(predictedClass),
                    detailedRecommendation,
                    output[0]
            );
        } catch (Exception e) {
            Log.e(TAG, "Error making prediction", e);
            throw new RuntimeException("Failed to make prediction: " + e.getMessage(), e);
        }
    }

    private String getRecommendationText(int predictedClass) {
        switch (predictedClass) {
            case 0:
                return "Not Recommended";
            case 1:
                return "Caution Advised";
            case 2:
                return "Recommended";
            default:
                return "Unknown";
        }
    }

    private int[][] tokenizeWeatherDescription(String text) {
        int[][] tokens = new int[1][WEATHER_INPUT_LENGTH];

        // Convert to lowercase and clean the text
        String cleanedText = text.toLowerCase().trim();

        // Remove all punctuation and special characters (matching Python tokenizer)
        cleanedText = CLEAN_PATTERN.matcher(cleanedText).replaceAll(" ");

        // Replace multiple spaces with single space
        cleanedText = cleanedText.replaceAll("\\s+", " ").trim();

        // Log.d(TAG, "Tokenization - Original: '" + text + "'");
        // Log.d(TAG, "Tokenization - Cleaned: '" + cleanedText + "'");

        // Split into words
        String[] words = cleanedText.split(" ");
        // Log.d(TAG, "Tokenization - Words: " + Arrays.toString(words));

        // Convert words to tokens with detailed logging
        List<String> tokenizedWords = new ArrayList<>();
        List<String> unknownWords = new ArrayList<>();

        for (int i = 0; i < WEATHER_INPUT_LENGTH; i++) {
            if (i < words.length && !words[i].isEmpty()) {
                Integer tokenId = vocabulary.get(words[i]);
                if (tokenId != null && tokenId < vocabSize) {
                    tokens[0][i] = tokenId;
                    if (i < 10) {
                        tokenizedWords.add(words[i] + ":" + tokenId);
                    }
                } else {
                    tokens[0][i] = oovIndex; // OOV token
                    if (i < 10) {
                        unknownWords.add(words[i]);
                        tokenizedWords.add(words[i] + ":OOV(" + oovIndex + ")");
                    }
                }
            } else {
                tokens[0][i] = 0; // Padding
            }
        }

        // Log.d(TAG, "Tokenization - Mapped (first 10): " + tokenizedWords);
        if (!unknownWords.isEmpty()) {
            Log.w(TAG, "Tokenization - Unknown words: " + unknownWords);
        }

        // Count statistics
        int nonZeroCount = 0;
        int oovCount = 0;
        for (int token : tokens[0]) {
            if (token != 0) nonZeroCount++;
            if (token == oovIndex) oovCount++;
        }
        // Log.d(TAG, String.format("Tokenization stats: %d words, %d OOV, %d padding",
        //        nonZeroCount, oovCount, WEATHER_INPUT_LENGTH - nonZeroCount));

        return tokens;
    }

    private float[][] normalizeTrailProfile(TrailProfile profile) {
        float[][] normalized = new float[1][TRAIL_FEATURES_COUNT];
        float[] features = profile.toArray();

        // Log.d(TAG, "Trail normalization - Raw: " + Arrays.toString(features));
        // Log.d(TAG, "Trail features: [difficulty, length_km, duration_hours]");

        // Normalize using (value - mean) / scale
        for (int i = 0; i < TRAIL_FEATURES_COUNT; i++) {
            if (trailScales[i] != 0) {
                normalized[0][i] = (float) ((features[i] - trailMeans[i]) / trailScales[i]);
                // Log.d(TAG, String.format("Trail feature %d: %.2f -> %.4f (mean=%.2f, scale=%.2f)",
                //     i, features[i], normalized[0][i], trailMeans[i], trailScales[i]));
            } else {
                normalized[0][i] = 0f;
            }
        }

        return normalized;
    }

    private float[][] normalizeHikerProfile(HikerProfile profile) {
        float[][] normalized = new float[1][HIKER_FEATURES_COUNT];
        float[] features = profile.toArray();

        // Use hikerMeans and hikerScales instead of just means/scales
        for (int i = 0; i < HIKER_FEATURES_COUNT; i++) {
            if (hikerScales[i] != 0) {
                normalized[0][i] = (float) ((features[i] - hikerMeans[i]) / hikerScales[i]);
            } else {
                normalized[0][i] = 0f;
            }
        }

        return normalized;
    }

    public void testTokenization() {
        String[] testPhrases = {
                "sunny",
                "rain",
                "heavy rain",
                "thunderstorm",
                "fine weather",
                "Fine and sunny. Light winds with good visibility.",
                "Thunderstorm warning in force. Heavy rain with squally thunderstorms.",
                "extreme heat warning"
        };

        // Log.d(TAG, "=== TOKENIZATION TEST ===");
        for (String phrase : testPhrases) {
            // Log.d(TAG, "Testing: '" + phrase + "'");
            int[][] tokens = tokenizeWeatherDescription(phrase);
            // Show first 15 tokens to see actual words
            // Log.d(TAG, "Result: " + Arrays.toString(Arrays.copyOfRange(tokens[0], 0, 15)));
        }
        // Log.d(TAG, "=== END TOKENIZATION TEST ===");
    }

    private Interpreter loadModel(String modelPath) {
        try {
            // Log.d(TAG, "Loading model: " + modelPath);
            ByteBuffer buffer = loadModelFile(modelPath);
            // Log.d(TAG, "Model file loaded, size: " + buffer.capacity() + " bytes");

            Interpreter.Options options = new Interpreter.Options();

            // For float model, GPU can be beneficial
            if (USE_FLOAT_MODEL) {
                CompatibilityList compatList = new CompatibilityList();
                if (compatList.isDelegateSupportedOnThisDevice()) {
                    try {
                        GpuDelegate gpuDelegate = new GpuDelegate();
                        options.addDelegate(gpuDelegate);
                        // Log.d(TAG, "GPU delegation enabled for float model");
                    } catch (Exception e) {
                        Log.w(TAG, "GPU delegation failed, using CPU: " + e.getMessage());
                    }
                } else {
                    // Log.d(TAG, "GPU not supported on this device");
                }
            }

            // Use multiple threads for better performance
            options.setNumThreads(4);
            // Log.d(TAG, "Using 4 threads for inference");

            Interpreter interpreter = new Interpreter(buffer, options);
            // Log.d(TAG, "Interpreter created successfully");

            // Verify input/output specifications
            validateModelSpecs(interpreter);

            return interpreter;
        } catch (Exception e) {
            Log.e(TAG, "Error loading model: " + modelPath, e);
            throw new RuntimeException("Failed to load TFLite model: " + modelPath, e);
        }
    }

    private void validateModelSpecs(Interpreter interpreter) {
        try {
            Log.d(TAG, "=== Model Specification ===");
            Log.d(TAG, "Input count: " + interpreter.getInputTensorCount());
            Log.d(TAG, "Output count: " + interpreter.getOutputTensorCount());

            // Validate we have 3 inputs
            if (interpreter.getInputTensorCount() != 3) {
                throw new RuntimeException("Model expects 3 inputs, got " +
                        interpreter.getInputTensorCount());
            }

            // Log all input details first
            for (int i = 0; i < interpreter.getInputTensorCount(); i++) {
                int[] shape = interpreter.getInputTensor(i).shape();
                Log.d(TAG, String.format("Input %d: shape=%s, dtype=%s",
                        i, Arrays.toString(shape), interpreter.getInputTensor(i).dataType()));
            }

            // Find which input is which by examining shapes and data types
            int weatherInputIndex = -1;
            int hikerInputIndex = -1;
            int trailInputIndex = -1;

            for (int i = 0; i < 3; i++) {
                int[] shape = interpreter.getInputTensor(i).shape();
                DataType dataType = interpreter.getInputTensor(i).dataType();

                // Weather input should be INT32 and have shape [1, 30] or [-1, 30]
                if (dataType == DataType.INT32 && shape.length == 2 &&
                        (shape[1] == WEATHER_INPUT_LENGTH || shape[0] == WEATHER_INPUT_LENGTH)) {
                    weatherInputIndex = i;
                    Log.d(TAG, "Found weather input at index " + i);
                }
                // Hiker input should be FLOAT32 and have shape [1, 8] or [-1, 8]
                else if (dataType == DataType.FLOAT32 && shape.length == 2 &&
                        (shape[1] == HIKER_FEATURES_COUNT || shape[0] == HIKER_FEATURES_COUNT)) {
                    hikerInputIndex = i;
                    Log.d(TAG, "Found hiker input at index " + i);
                }
                // Trail input should be FLOAT32 and have shape [1, 3] or [-1, 3]
                else if (dataType == DataType.FLOAT32 && shape.length == 2 &&
                        (shape[1] == TRAIL_FEATURES_COUNT || shape[0] == TRAIL_FEATURES_COUNT)) {
                    trailInputIndex = i;
                    Log.d(TAG, "Found trail input at index " + i);
                }
            }

            // Validate all inputs were found
            if (weatherInputIndex == -1) {
                Log.e(TAG, "Could not find weather input (expected INT32 with shape [?, 30])");
                throw new RuntimeException("Weather input not found in model");
            }
            if (hikerInputIndex == -1) {
                Log.e(TAG, "Could not find hiker input (expected FLOAT32 with shape [?, 8])");
                throw new RuntimeException("Hiker input not found in model");
            }
            if (trailInputIndex == -1) {
                Log.e(TAG, "Could not find trail input (expected FLOAT32 with shape [?, 3])");
                throw new RuntimeException("Trail input not found in model");
            }

            // Store the input indices for later use
            this.weatherInputIndex = weatherInputIndex;
            this.hikerInputIndex = hikerInputIndex;
            this.trailInputIndex = trailInputIndex;

            // Check output
            int[] outputShape = interpreter.getOutputTensor(0).shape();
            Log.d(TAG, "Output shape: " + Arrays.toString(outputShape));
            Log.d(TAG, "Output dtype: " + interpreter.getOutputTensor(0).dataType());

            // Validate output shape (should be [1, 3] or [-1, 3])
            if (outputShape.length != 2 ||
                    (outputShape[1] != NUM_CLASSES && outputShape[0] != NUM_CLASSES)) {
                throw new RuntimeException("Output shape mismatch. Expected [?, 3], got " +
                        Arrays.toString(outputShape));
            }

            Log.d(TAG, "Model validation passed!");
            Log.d(TAG, "Input order: weather=" + weatherInputIndex +
                    ", hiker=" + hikerInputIndex + ", trail=" + trailInputIndex);
            Log.d(TAG, "=== End Model Specification ===");

        } catch (Exception e) {
            Log.e(TAG, "Error validating model specifications", e);
            throw new RuntimeException("Model specification validation failed", e);
        }
    }

    private ByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        inputStream.close();
        fileChannel.close();
        fileDescriptor.close();
        return buffer;
    }

    private double[] parseDoubleArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            throw new RuntimeException("JSON array is null");
        }

        double[] result = new double[jsonArray.length()];
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                result[i] = jsonArray.getDouble(i);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing double array", e);
            throw new RuntimeException("Failed to parse double array", e);
        }
        return result;
    }

    private Map<String, Integer> parseVocabulary(JSONObject vocab) {
        if (vocab == null) {
            throw new RuntimeException("Vocabulary JSON object is null");
        }

        Map<String, Integer> result = new HashMap<>();
        Iterator<String> keys = vocab.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                result.put(key, vocab.getInt(key));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing vocabulary for key: " + key, e);
            }
        }
        return result;
    }

    // Inner classes remain the same
    public static class HikingRecommendation {
        public final int recommendationClass;
        public final float confidence;
        public final String recommendationText;
        public final String detailedAdvice;
        public final float[] allProbabilities;

        public HikingRecommendation(int recommendationClass, float confidence,
                                    String recommendationText, String detailedAdvice,
                                    float[] allProbabilities) {
            this.recommendationClass = recommendationClass;
            this.confidence = confidence;
            this.recommendationText = recommendationText;
            this.detailedAdvice = detailedAdvice;
            this.allProbabilities = allProbabilities;
        }

        @SuppressLint("DefaultLocale")
        public String getFullRecommendation() {
            return String.format("%s (%.0f%% confidence)\n\n%s",
                    recommendationText, confidence * 100, detailedAdvice);
        }
    }

    public static class HikerProfile {
        public final float age;
        public final float weight;
        public final float height;
        public final float fitnessLevel;
        public final float experienceYears;
        public final float weeklyExerciseHours;
        public final float maxAltitudeClimbed;
        public final float longestHikeKm;

        public HikerProfile(float age, float weight, float height, float fitnessLevel,
                            float experienceYears, float weeklyExerciseHours,
                            float maxAltitudeClimbed, float longestHikeKm) {
            this.age = age;
            this.weight = weight;
            this.height = height;
            this.fitnessLevel = fitnessLevel;
            this.experienceYears = experienceYears;
            this.weeklyExerciseHours = weeklyExerciseHours;
            this.maxAltitudeClimbed = maxAltitudeClimbed;
            this.longestHikeKm = longestHikeKm;
        }

        public float[] toArray() {
            return new float[]{
                    age, weight, height, fitnessLevel,
                    experienceYears, weeklyExerciseHours,
                    maxAltitudeClimbed, longestHikeKm
            };
        }
    }

    public static class TrailProfile {
        public final float difficulty;  // 1-5 scale
        public final float lengthKm;    // in kilometers
        public final float durationHours; // in hours

        public TrailProfile(float difficulty, float lengthKm, float durationHours) {
            this.difficulty = difficulty;
            this.lengthKm = lengthKm;
            this.durationHours = durationHours;
        }

        public float[] toArray() {
            return new float[]{difficulty, lengthKm, durationHours};
        }
    }
}