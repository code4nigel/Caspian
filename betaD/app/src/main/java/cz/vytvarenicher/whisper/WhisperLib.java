package cz.vytvarenicher.whisper;

import android.util.Log;

public class WhisperLib {
    private static final String TAG = "WhisperLib";
    private static boolean isLoaded = false;

    static {
        try {
            System.loadLibrary("ggml");
            System.loadLibrary("ggml-base");
            System.loadLibrary("ggml-cpu");
            System.loadLibrary("whisper");
            System.loadLibrary("whisper-jni");
            isLoaded = true;
            Log.i(TAG, "Whisper native libraries loaded successfully");
        } catch (Throwable t) {
            Log.e(TAG, "Failed to load whisper native libraries: " + t.getMessage(), t);
            isLoaded = false;
        }
    }

    public static boolean isAvailable() {
        return isLoaded;
    }

    public static native long initFromFile(String modelPath);

    public static native String transcribe(long ctxPtr, float[] samples, String language);

    public static native void free(long ctxPtr);

    public static native String systemInfo();
}
