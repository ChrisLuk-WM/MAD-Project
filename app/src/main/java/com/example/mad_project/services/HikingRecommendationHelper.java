package com.example.mad_project.services;

import static com.example.mad_project.services.RecommendationDetails.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    private static final int WEATHER_INPUT_LENGTH = 30;
    private static final int HIKER_FEATURES_COUNT = 8;
    private static final int NUM_CLASSES = 3;
    private static final String TAG = "HikingHelper";

    // Model selection - change this to switch between models
    private static final boolean USE_FLOAT_MODEL = true; // Set to false for quantized
    private static final String MODEL_PATH = USE_FLOAT_MODEL ?
            "hiking_model_float.tflite" : "hiking_model_quantized.tflite";

    private final Context context;
    private Interpreter tflite;
    private final double[] means;
    private final double[] scales;
    private final Map<String, Integer> vocabulary;
    private final int vocabSize;
    private final int oovIndex;

    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^a-z0-9\\s]");

    public HikingRecommendationHelper(Context context) {
        this.context = context;

//        // Log.d(TAG, "Initializing HikingRecommendationHelper with model: " + MODEL_PATH);

        // Load vocabulary first to verify it's correct
        JSONObject vocabJson = Common.loadJsonFromAsset(context, "vocabulary.json");
        vocabulary = parseVocabulary(vocabJson.optJSONObject("vocabulary"));
        vocabSize = vocabJson.optInt("vocab_size", 5000);
        oovIndex = vocabJson.optInt("oov_index", 1);

        // Log vocabulary info
//        // Log.d(TAG, "Vocabulary loaded: " + vocabulary.size() + " words");
//        // Log.d(TAG, "Vocab size limit: " + vocabSize);
//        // Log.d(TAG, "OOV index: " + oovIndex);

        // Log some weather-related vocabulary entries
        String[] weatherWords = {"sunny", "rain", "hot", "cold", "wind", "storm", "clear",
                "cloudy", "heavy", "light", "moderate", "fine", "typhoon"};
        // Log.d(TAG, "=== Weather vocabulary check ===");
        for (String word : weatherWords) {
            Integer tokenId = vocabulary.get(word);
            if (tokenId != null) {
                // Log.d(TAG, "Found: '" + word + "' -> " + tokenId);
            } else {
                // Log.d(TAG, "Missing: '" + word + "'");
            }
        }

        // Load scaling parameters
        JSONObject scalerParams = Common.loadJsonFromAsset(context, "scaler_params.json");
        means = parseDoubleArray(scalerParams.optJSONArray("mean"));
        scales = parseDoubleArray(scalerParams.optJSONArray("scale"));

        // Verify scaler dimensions
        if (means.length != HIKER_FEATURES_COUNT || scales.length != HIKER_FEATURES_COUNT) {
            throw new RuntimeException(String.format(
                    "Scaler parameter mismatch. Expected %d features, got means=%d, scales=%d",
                    HIKER_FEATURES_COUNT, means.length, scales.length));
        }

        // Log.d(TAG, "Scaler loaded - Features: " + HIKER_FEATURES_COUNT);
        for (int i = 0; i < HIKER_FEATURES_COUNT; i++) {
            // Log.d(TAG, String.format("Feature %d: mean=%.2f, scale=%.2f", i, means[i], scales[i]));
        }

        // Load model
        tflite = loadModel(MODEL_PATH);
    }

    public HikingRecommendation getPrediction(String weatherDescription, HikerProfile profile) {
        try {
            // Add detailed logging
            // Log.d(TAG, "=== PREDICTION START ===");
            // Log.d(TAG, "Model: " + MODEL_PATH);
            // Log.d(TAG, "Original weather: " + weatherDescription);

            // Prepare inputs
            int[][] weatherInput = tokenizeWeatherDescription(weatherDescription);
            float[][] hikerInput = normalizeHikerProfile(profile);

            // Log the complete input values for debugging
            // Log.d(TAG, "Complete weather input: " + Arrays.toString(weatherInput[0]));
            // Log.d(TAG, "Complete hiker input: " + Arrays.toString(hikerInput[0]));

            float[][] output = new float[1][NUM_CLASSES];

            // Run inference
            Object[] inputs = new Object[]{weatherInput, hikerInput};
            Map<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, output);

            tflite.runForMultipleInputsOutputs(inputs, outputs);

            // Log output with more precision
            // Log.d(TAG, String.format("Model output probabilities: [%.6f, %.6f, %.6f]",
            //        output[0][0], output[0][1], output[0][2]));

            // Check for uniform probabilities
            boolean isUniform = true;
            for (int i = 1; i < NUM_CLASSES; i++) {
                if (Math.abs(output[0][i] - output[0][0]) > 0.01) {
                    isUniform = false;
                    break;
                }
            }

            // Process output
            int predictedClass = argmax(output[0]);
            float confidence = output[0][predictedClass];

            // Log.d(TAG, "Predicted class: " + predictedClass + " with confidence: " + confidence);
            // Log.d(TAG, "=== PREDICTION END ===");

            // Generate detailed recommendation
            String detailedRecommendation = generateDetailedRecommendation(
                    output[0], weatherDescription, profile);

            return new HikingRecommendation(
                    predictedClass,
                    confidence,
                    getRecommendationText(predictedClass),
                    detailedRecommendation,
                    output[0]
            );
        } catch (Exception e) {
            Log.e(TAG, "Error making prediction", e);
            e.printStackTrace();
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

    private float[][] normalizeHikerProfile(HikerProfile profile) {
        float[][] normalized = new float[1][HIKER_FEATURES_COUNT];
        float[] features = profile.toArray();

        // Log.d(TAG, "Normalization - Raw features: " + Arrays.toString(features));
        // Log.d(TAG, "Normalization - Feature names: [age, weight, height, fitness, experience, exercise, altitude, distance]");

        // Normalize using (value - mean) / scale
        for (int i = 0; i < HIKER_FEATURES_COUNT; i++) {
            if (scales[i] != 0) {
                normalized[0][i] = (float) ((features[i] - means[i]) / scales[i]);
                // Log.d(TAG, String.format("Feature %d: %.2f -> %.4f (mean=%.2f, scale=%.2f)",
                //        i, features[i], normalized[0][i], means[i], scales[i]));
            } else {
                // Log.e(TAG, "ERROR: Scale is zero for feature " + i);
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
            // Log all input/output details
            // Log.d(TAG, "=== Model Specification ===");
            // Log.d(TAG, "Input count: " + interpreter.getInputTensorCount());
            // Log.d(TAG, "Output count: " + interpreter.getOutputTensorCount());

            // Check weather input
            int[] weatherInputShape = interpreter.getInputTensor(0).shape();
            // Log.d(TAG, "Input 0 (weather) shape: " + Arrays.toString(weatherInputShape));
            // Log.d(TAG, "Input 0 dtype: " + interpreter.getInputTensor(0).dataType());

            // Check hiker input
            int[] hikerInputShape = interpreter.getInputTensor(1).shape();
            // Log.d(TAG, "Input 1 (hiker) shape: " + Arrays.toString(hikerInputShape));
            // Log.d(TAG, "Input 1 dtype: " + interpreter.getInputTensor(1).dataType());

            // Check output
            int[] outputShape = interpreter.getOutputTensor(0).shape();
            // Log.d(TAG, "Output shape: " + Arrays.toString(outputShape));
            // Log.d(TAG, "Output dtype: " + interpreter.getOutputTensor(0).dataType());

            // Validate shapes
            if (weatherInputShape[1] != WEATHER_INPUT_LENGTH) {
                throw new RuntimeException("Weather input shape mismatch");
            }
            if (hikerInputShape[1] != HIKER_FEATURES_COUNT) {
                throw new RuntimeException("Hiker input shape mismatch");
            }
            if (outputShape[1] != NUM_CLASSES) {
                throw new RuntimeException("Output shape mismatch");
            }

            // Log.d(TAG, "Model validation passed!");
            // Log.d(TAG, "=== End Model Specification ===");

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
}