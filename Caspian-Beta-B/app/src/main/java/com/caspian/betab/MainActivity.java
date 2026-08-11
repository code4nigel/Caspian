package com.caspian.betab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView rvChatMessages;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private EditText etNativePrompt;
    private ImageButton btnNativeSend;
    private ImageButton btnAttach;
    private ImageButton btnVoiceMic;
    private ImageButton btnOpenHistory;

    private LinearLayout btnModelSelector;
    private TextView tvCurrentModel;
    private TextView tvAuthStatus;
    private TextView btnSignInWeb;

    private FrameLayout webAuthContainer;
    private WebView authSheetWebview;
    private TextView btnCloseAuthSheet;

    private WebView controlWebView;
    private FrameLayout sheetOverlayContainer;
    private View sheetBackdrop;
    private CardView floatingCaspianCard;
    private ViewGroup rootContainer;

    private String activeModelSlug = "gpt-4o";
    private String activeModelName = "GPT-4o";
    private String chatgptSessionToken = null;
    private boolean isDebugRecording = false;

    private final List<ChatGPTApiClient.ModelItem> liveFetchedModels = new ArrayList<>();
    private ChatGPTApiClient chatGPTApiClient;

    // Touch Drag Variables for Floating Button
    private float dX, dY;
    private float startRawX, startRawY;
    private boolean isDragging = false;

    private static final String DEFAULT_CHROME_UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36";
    private static final int FILE_PICKER_REQUEST = 1001;
    private static final int VOICE_RECOGNIZER_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Maximum Window Hardware Acceleration
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        setContentView(R.layout.activity_native_chat);

        chatGPTApiClient = new ChatGPTApiClient();

        drawerLayout = findViewById(R.id.drawer_layout);
        rootContainer = findViewById(R.id.root_container);
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etNativePrompt = findViewById(R.id.et_native_prompt);
        btnNativeSend = findViewById(R.id.btn_native_send);
        btnAttach = findViewById(R.id.btn_attach);
        btnVoiceMic = findViewById(R.id.btn_voice_mic);
        btnOpenHistory = findViewById(R.id.btn_open_history);

        btnModelSelector = findViewById(R.id.btn_model_selector);
        tvCurrentModel = findViewById(R.id.tv_current_model);
        tvAuthStatus = findViewById(R.id.tv_auth_status);
        btnSignInWeb = findViewById(R.id.btn_sign_in_web);

        webAuthContainer = findViewById(R.id.web_auth_container);
        authSheetWebview = findViewById(R.id.auth_sheet_webview);
        btnCloseAuthSheet = findViewById(R.id.btn_close_auth_sheet);

        controlWebView = findViewById(R.id.control_webview);
        sheetOverlayContainer = findViewById(R.id.sheet_overlay_container);
        sheetBackdrop = findViewById(R.id.sheet_backdrop);
        floatingCaspianCard = findViewById(R.id.floating_caspian_card);

        setupRecyclerView();
        setupModelSelectorDialog();
        setupNativeSend();
        setupAttachmentAndVoice();
        setupAuthSheet();
        setupControlWebView();
        setupNativeFloatingButton();
        setupSmartKeyboardAvoidance();

        btnOpenHistory.setOnClickListener(v -> {
            triggerHapticFeedback();
            drawerLayout.openDrawer(findViewById(R.id.left_drawer_container));
        });

        sheetBackdrop.setOnClickListener(v -> closeControlSheet());

        // Background Web Auth Authenticator (extracts ChatGPT/Gemini session tokens & models)
        initBackgroundAuthWebView();

        // Welcome message
        addMessage(new ChatMessage(UUID.randomUUID().toString(), ChatMessage.TYPE_ASSISTANT, 
                "**Welcome to Caspian Beta B (v1.0.5)!**\n\nThis is a **100% Native Android Chat UI** with **0ms typing latency**.\n\nTap **Sign In** to log into your account, or chat immediately using guest session!", 
                activeModelName, false));
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(this, messageList);
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupModelSelectorDialog() {
        btnModelSelector.setOnClickListener(v -> {
            triggerHapticFeedback();

            if (liveFetchedModels.isEmpty()) {
                String[] defaultNames = {"GPT-4o (Flagship)", "GPT-4o Mini", "ChatGPT o1", "Google Gemini Pro"};
                String[] defaultSlugs = {"gpt-4o", "gpt-4o-mini", "o1", "gemini-pro"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Active Model");
                builder.setItems(defaultNames, (dialog, which) -> {
                    activeModelSlug = defaultSlugs[which];
                    activeModelName = defaultNames[which].split(" ")[0];
                    tvCurrentModel.setText(activeModelName + " ▾");
                });
                builder.show();
                return;
            }

            String[] modelNames = new String[liveFetchedModels.size()];
            for (int i = 0; i < liveFetchedModels.size(); i++) {
                modelNames[i] = liveFetchedModels.get(i).title + " (" + liveFetchedModels.get(i).slug + ")";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Live Discovered Models");
            builder.setItems(modelNames, (dialog, which) -> {
                ChatGPTApiClient.ModelItem selected = liveFetchedModels.get(which);
                activeModelSlug = selected.slug;
                activeModelName = selected.title;
                tvCurrentModel.setText(activeModelName + " ▾");
                Toast.makeText(this, "Active model: " + activeModelName, Toast.LENGTH_SHORT).show();
            });
            builder.show();
        });
    }

    private void setupAttachmentAndVoice() {
        btnAttach.setOnClickListener(v -> {
            triggerHapticFeedback();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select File to Attach"), FILE_PICKER_REQUEST);
        });

        btnVoiceMic.setOnClickListener(v -> {
            triggerHapticFeedback();
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak prompt...");
                startActivityForResult(intent, VOICE_RECOGNIZER_REQUEST);
            } catch (Exception e) {
                Toast.makeText(this, "Voice typing not supported on this device.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupAuthSheet() {
        btnSignInWeb.setOnClickListener(v -> {
            triggerHapticFeedback();
            webAuthContainer.setVisibility(View.VISIBLE);
            authSheetWebview.loadUrl("https://chatgpt.com/auth/login");
        });

        btnCloseAuthSheet.setOnClickListener(v -> {
            triggerHapticFeedback();
            webAuthContainer.setVisibility(View.GONE);
        });

        WebSettings s = authSheetWebview.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setUserAgentString(DEFAULT_CHROME_UA);
        CookieManager.getInstance().setAcceptThirdPartyCookies(authSheetWebview, true);

        authSheetWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                if (url != null && url.contains("chatgpt.com")) {
                    authSheetWebview.evaluateJavascript("(function(){ try { return localStorage.getItem('accessToken') || ''; } catch(e){ return ''; } })()", value -> {
                        if (value != null && value.length() > 10) {
                            chatgptSessionToken = value.replace("\"", "");
                            tvAuthStatus.setText("● Authenticated (Web Session)");
                            tvAuthStatus.setTextColor(0xFF10B981);
                            btnSignInWeb.setText("Signed In");
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            String fileUriStr = data.getData().toString();
            Toast.makeText(this, "Attached file: " + fileUriStr, Toast.LENGTH_SHORT).show();
        } else if (requestCode == VOICE_RECOGNIZER_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                etNativePrompt.setText(matches.get(0));
            }
        }
    }

    private void setupNativeSend() {
        btnNativeSend.setOnClickListener(v -> {
            String prompt = etNativePrompt.getText().toString().trim();
            if (TextUtils.isEmpty(prompt)) return;

            triggerHapticFeedback();
            etNativePrompt.setText("");

            addMessage(new ChatMessage(UUID.randomUUID().toString(), ChatMessage.TYPE_USER, prompt, activeModelName, false));

            String aiMsgId = UUID.randomUUID().toString();
            ChatMessage aiMsg = new ChatMessage(aiMsgId, ChatMessage.TYPE_ASSISTANT, "thinking...", activeModelName, true);
            addMessage(aiMsg);

            // Stream response via live Web-API Engine
            chatGPTApiClient.sendMessage(chatgptSessionToken, activeModelSlug, prompt, new ChatGPTApiClient.StreamCallback() {
                @Override
                public void onTokenReceived(String fullAccumulatedText) {
                    updateMessageText(aiMsgId, fullAccumulatedText);
                }

                @Override
                public void onError(String errorMessage) {
                    updateMessageText(aiMsgId, "⚠️ " + errorMessage + "\n\n(Tip: Tap 'Sign In' at top right to authenticate web session)");
                }

                @Override
                public void onComplete() {
                    markMessageComplete(aiMsgId);
                }
            });
        });
    }

    private void addMessage(ChatMessage msg) {
        messageList.add(msg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
    }

    private void updateMessageText(String msgId, String text) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).id.equals(msgId)) {
                messageList.get(i).text = text;
                chatAdapter.notifyItemChanged(i);
                rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                break;
            }
        }
    }

    private void markMessageComplete(String msgId) {
        for (int i = 0; i < messageList.size(); i++) {
            if (messageList.get(i).id.equals(msgId)) {
                messageList.get(i).isStreaming = false;
                chatAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void triggerHapticFeedback() {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                v.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (Exception ignored) {}
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initBackgroundAuthWebView() {
        WebView bgWebView = new WebView(this);
        WebSettings s = bgWebView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setUserAgentString(DEFAULT_CHROME_UA);
        CookieManager.getInstance().setAcceptThirdPartyCookies(bgWebView, true);

        bgWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                if (url != null && url.contains("chatgpt.com")) {
                    bgWebView.evaluateJavascript("(function(){ try { return localStorage.getItem('accessToken') || ''; } catch(e){ return ''; } })()", value -> {
                        if (value != null && value.length() > 10) {
                            chatgptSessionToken = value.replace("\"", "");
                            tvAuthStatus.setText("● Live Session Authenticated");
                            tvAuthStatus.setTextColor(0xFF10B981);
                            btnSignInWeb.setText("Signed In");

                            chatGPTApiClient.fetchAvailableModels(chatgptSessionToken, new ChatGPTApiClient.ModelsCallback() {
                                @Override
                                public void onModelsFetched(List<ChatGPTApiClient.ModelItem> models) {
                                    liveFetchedModels.clear();
                                    liveFetchedModels.addAll(models);
                                    if (!models.isEmpty()) {
                                        activeModelSlug = models.get(0).slug;
                                        activeModelName = models.get(0).title;
                                        tvCurrentModel.setText(activeModelName + " ▾");
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e("CaspianBetaB", "Models error: " + error);
                                }
                            });
                        }
                    });
                }
            }
        });

        bgWebView.loadUrl("https://chatgpt.com/");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupControlWebView() {
        controlWebView.getSettings().setJavaScriptEnabled(true);
        controlWebView.getSettings().setDomStorageEnabled(true);
        controlWebView.getSettings().setAllowFileAccess(true);
        controlWebView.setBackgroundColor(0);
        controlWebView.addJavascriptInterface(new CaspianBridge(this), "CaspianBridge");
        controlWebView.loadUrl("file:///android_asset/mobile_control.html");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupNativeFloatingButton() {
        floatingCaspianCard.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    startRawX = event.getRawX();
                    startRawY = event.getRawY();
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = Math.abs(event.getRawX() - startRawX);
                    float deltaY = Math.abs(event.getRawY() - startRawY);

                    if (deltaX > 10 || deltaY > 10) {
                        isDragging = true;
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        View parent = (View) view.getParent();
                        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                        int statusBarHeight = resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 40;

                        newX = Math.max(0, Math.min(parent.getWidth() - view.getWidth(), newX));
                        newY = Math.max(statusBarHeight + 10, Math.min(parent.getHeight() - view.getHeight(), newY));

                        view.animate()
                                .x(newX)
                                .y(newY)
                                .setDuration(0)
                                .start();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) {
                        triggerHapticFeedback();
                        toggleControlSheet();
                    }
                    return true;

                default:
                    return false;
            }
        });
    }

    private void setupSmartKeyboardAvoidance() {
        View rView = findViewById(R.id.root_container);
        rView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastKeyboardHeight = 0;
            private float originalCardY = -1;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rView.getHeight();
                int keyboardHeight = screenHeight - r.height();

                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                int statusBarHeight = resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 40;
                final int topCeiling = statusBarHeight + 20;

                if (keyboardHeight > 250) {
                    if (lastKeyboardHeight == 0) {
                        float currentY = floatingCaspianCard.getY();
                        float visibleBottom = r.bottom;
                        float cardHeight = floatingCaspianCard.getHeight();

                        if (currentY > visibleBottom - cardHeight - 20) {
                            originalCardY = currentY;
                            float shiftedY = visibleBottom - cardHeight - 20;
                            shiftedY = Math.max(topCeiling, shiftedY);
                            floatingCaspianCard.animate().y(shiftedY).setDuration(150).start();
                        }
                    }
                    lastKeyboardHeight = keyboardHeight;
                } else {
                    if (lastKeyboardHeight > 0) {
                        if (originalCardY != -1) {
                            floatingCaspianCard.animate().y(originalCardY).setDuration(150).start();
                            originalCardY = -1;
                        }
                    }
                    lastKeyboardHeight = 0;
                }
            }
        });
    }

    public void toggleControlSheet() {
        if (sheetOverlayContainer.getVisibility() == View.VISIBLE) {
            closeControlSheet();
        } else {
            openControlSheet();
        }
    }

    public void openControlSheet() {
        sheetOverlayContainer.setVisibility(View.VISIBLE);
        // Absolute Z-Order enforcement: Bring floating wave card to front!
        floatingCaspianCard.bringToFront();
        if (rootContainer != null) {
            rootContainer.bringChildToFront(floatingCaspianCard);
        }
        floatingCaspianCard.invalidate();
    }

    public void closeControlSheet() {
        sheetOverlayContainer.setVisibility(View.GONE);
    }

    // CaspianBridge Support Methods
    public String getOpenTabsJson() {
        return "[{\"id\":1,\"title\":\"Native Chat\",\"url\":\"native\",\"service\":\"" + activeModelSlug + "\",\"active\":true}]";
    }

    public void createNewTab(String service) {}
    public void switchTab(int tabId) {}
    public void closeTab(int tabId) {}
    public void closeAllTabs() {}
    public boolean isDebugRecordingActive() { return isDebugRecording; }
    public void startDebugRecording() { isDebugRecording = true; }
    public void stopAndSaveDebugLog() { isDebugRecording = false; }
    public void toggleHostPageTheme(boolean isDark) {}
    public void performExportOnMainWebView(String fmt) {}
    public void pauseInjectionTimer() {}
    public void resumeInjectionTimerLater() {}
    public void applyPrunerInMainWebView() {}
    public WebView getWebView() { return null; }
}
