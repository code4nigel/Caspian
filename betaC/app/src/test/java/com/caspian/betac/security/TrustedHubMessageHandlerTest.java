package com.caspian.betac.security;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class TrustedHubMessageHandlerTest {

    private final AtomicReference<String> lastUrl = new AtomicReference<>("");
    private final AtomicReference<String> lastService = new AtomicReference<>("");
    private final AtomicReference<String> lastToast = new AtomicReference<>("");
    private final AtomicReference<String> lastSound = new AtomicReference<>("");
    private final AtomicReference<String> lastWallpaper = new AtomicReference<>("");
    private final AtomicReference<String> lastCask = new AtomicReference<>("");

    private final TrustedHubMessageHandler.HubActionCallback callback = new TrustedHubMessageHandler.HubActionCallback() {
        @Override
        public void onOpenUrl(String url) {
            lastUrl.set(url);
        }

        @Override
        public void onSwitchService(String service) {
            lastService.set(service);
        }

        @Override
        public void onAddNewTab(String service, String url) {
            lastService.set(service);
            lastUrl.set(url);
        }

        @Override
        public void onPlayAssetSound(String sound) {
            lastSound.set(sound);
        }

        @Override
        public void onShowToast(String message) {
            lastToast.set(message);
        }

        @Override
        public void onShowKeyboard() {}

        @Override
        public void onSaveWallpaper(String wallpaper) {
            lastWallpaper.set(wallpaper);
        }

        @Override
        public void onSwitchCask(String caskId) {
            lastCask.set(caskId);
        }
    };

    private TrustedHubMessageHandler handler;

    @Before
    public void setUp() {
        lastUrl.set("");
        lastService.set("");
        lastToast.set("");
        lastSound.set("");
        lastWallpaper.set("");
        lastCask.set("");
        handler = new TrustedHubMessageHandler(callback);
    }

    @Test
    public void testOriginAllowance() {
        assertTrue("launch_hub.html must be allowed",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/launch_hub.html"));
        assertTrue("incognito_hub.html must be allowed",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/incognito_hub.html"));

        // Only exact Hub documents are permitted to dispatch Hub messages
        assertFalse("pdf_viewer.html must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/pdf_viewer.html"));
        assertFalse("browser_control.html must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/browser_control.html"));
        assertFalse("Remote https must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("https://chatgpt.com"));
        assertFalse("Remote http must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("http://example.com"));
        assertFalse("Null or empty url must be rejected",
                TrustedHubMessageHandler.isOriginAllowed(null));
        assertFalse("Evil subpath must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/../sdcard/evil.html"));
    }

    @Test
    public void testSourceOriginVerification() {
        assertTrue(TrustedHubMessageHandler.isLocalSourceOrigin((String) null));
        assertTrue(TrustedHubMessageHandler.isLocalSourceOrigin("file://"));
        assertTrue(TrustedHubMessageHandler.isLocalSourceOrigin("file:///android_asset/launch_hub.html"));
        assertTrue(TrustedHubMessageHandler.isLocalSourceOrigin("null"));

        assertFalse("HTTPS source origin must be rejected",
                TrustedHubMessageHandler.isLocalSourceOrigin("https://evil.com"));
        assertFalse("HTTP source origin must be rejected",
                TrustedHubMessageHandler.isLocalSourceOrigin("http://attacker.com"));
    }

    @Test
    public void testHttpsUrlValidation() {
        assertTrue(TrustedHubMessageHandler.isValidHttpsUrl("https://chatgpt.com"));
        assertTrue(TrustedHubMessageHandler.isValidHttpsUrl("https://google.com/search?q=test"));

        assertFalse("HTTP must be rejected", TrustedHubMessageHandler.isValidHttpsUrl("http://insecure.com"));
        assertFalse("javascript: must be rejected", TrustedHubMessageHandler.isValidHttpsUrl("javascript:alert(1)"));
        assertFalse("file: must be rejected", TrustedHubMessageHandler.isValidHttpsUrl("file:///etc/passwd"));
        assertFalse("data: must be rejected", TrustedHubMessageHandler.isValidHttpsUrl("data:text/html,evil"));
        assertFalse(TrustedHubMessageHandler.isValidHttpsUrl(null));
        assertFalse(TrustedHubMessageHandler.isValidHttpsUrl(""));
    }

    @Test
    public void testServiceValidation() {
        assertTrue(TrustedHubMessageHandler.isValidServiceName("chatgpt"));
        assertTrue(TrustedHubMessageHandler.isValidServiceName("gemini"));
        assertTrue(TrustedHubMessageHandler.isValidServiceName("claude"));
        assertTrue(TrustedHubMessageHandler.isValidServiceName("deepseek"));
        assertTrue(TrustedHubMessageHandler.isValidServiceName("youtube"));
        assertTrue(TrustedHubMessageHandler.isValidServiceName("google"));
        assertTrue(TrustedHubMessageHandler.isValidServiceName("hub"));
        assertTrue(TrustedHubMessageHandler.isValidServiceName("web"));

        assertFalse(TrustedHubMessageHandler.isValidServiceName("unknown"));
        assertFalse(TrustedHubMessageHandler.isValidServiceName("../evil"));
        assertFalse(TrustedHubMessageHandler.isValidServiceName(null));
    }

    @Test
    public void testSoundValidation() {
        assertTrue(TrustedHubMessageHandler.isValidSoundName("tap_button.mp3"));
        assertTrue(TrustedHubMessageHandler.isValidSoundName("sfx/pop_button_v2.mp3"));
        assertTrue(TrustedHubMessageHandler.isValidSoundName("click.wav"));

        assertFalse("Path traversal in sound must be rejected",
                TrustedHubMessageHandler.isValidSoundName("../evil.mp3"));
        assertFalse("Root paths must be rejected",
                TrustedHubMessageHandler.isValidSoundName("/etc/shadow"));
        assertFalse("Non-audio extensions must be rejected",
                TrustedHubMessageHandler.isValidSoundName("script.js"));
        assertFalse(TrustedHubMessageHandler.isValidSoundName(null));
    }

    @Test
    public void testWallpaperValidation() {
        assertTrue(TrustedHubMessageHandler.isValidWallpaper("data:image/png;base64,iVBORw0KGgoAAAANSUhEUg=="));
        assertTrue(TrustedHubMessageHandler.isValidWallpaper("#1b4264"));
        assertTrue(TrustedHubMessageHandler.isValidWallpaper("linear-gradient(135deg, #1b4264, #000000)"));
        assertTrue(TrustedHubMessageHandler.isValidWallpaper("https://images.unsplash.com/photo-12345"));

        assertFalse(TrustedHubMessageHandler.isValidWallpaper("javascript:alert(1)"));
        assertFalse(TrustedHubMessageHandler.isValidWallpaper(null));
        assertFalse(TrustedHubMessageHandler.isValidWallpaper(""));
    }

    @Test
    public void testCaskIdValidation() {
        assertTrue(TrustedHubMessageHandler.isValidCaskId("cask_caspian"));
        assertTrue(TrustedHubMessageHandler.isValidCaskId("cask_work_123"));

        assertFalse(TrustedHubMessageHandler.isValidCaskId("cask; rm -rf /"));
        assertFalse(TrustedHubMessageHandler.isValidCaskId("../etc"));
        assertFalse(TrustedHubMessageHandler.isValidCaskId(null));
        assertFalse(TrustedHubMessageHandler.isValidCaskId(""));
    }

    @Test
    public void testNonMainFrameMessageRejected() {
        String msg = "{\"action\":\"openUrl\",\"url\":\"https://chatgpt.com\"}";
        boolean handled = handler.handleMessage("file:///android_asset/launch_hub.html", msg, "file://", false);
        assertFalse("Non-main-frame message must be rejected", handled);
        assertEquals("Callback must not be invoked for iframe messages", "", lastUrl.get());
    }

    @Test
    public void testRemoteSourceOriginMessageRejected() {
        String msg = "{\"action\":\"openUrl\",\"url\":\"https://chatgpt.com\"}";
        boolean handled = handler.handleMessage("file:///android_asset/launch_hub.html", msg, "https://evil.com", true);
        assertFalse("Remote source origin must be rejected", handled);
        assertEquals("Callback must not be invoked for remote sourceOrigin", "", lastUrl.get());
    }

    @Test
    public void testNonHubDocumentMessageRejected() {
        String msg = "{\"action\":\"openUrl\",\"url\":\"https://chatgpt.com\"}";
        boolean handled = handler.handleMessage("file:///android_asset/pdf_viewer.html", msg, "file://", true);
        assertFalse("Non-hub document must be rejected", handled);
        assertEquals("Callback must not be invoked when top document is not Hub", "", lastUrl.get());
    }

    @Test
    public void testValidMainFrameMessageAccepted() {
        String msg = "{\"action\":\"openUrl\",\"url\":\"https://chatgpt.com\"}";
        boolean handled = handler.handleMessage("file:///android_asset/launch_hub.html", msg, "file://", true);
        assertTrue("Valid main frame message should be accepted", handled);
        assertEquals("https://chatgpt.com", lastUrl.get());
    }

    @Test
    public void testInsecureUrlActionRejected() {
        String msg = "{\"action\":\"openUrl\",\"url\":\"http://insecure.com\"}";
        boolean handled = handler.handleMessage("file:///android_asset/launch_hub.html", msg, "file://", true);
        assertTrue("Message is processed, but payload action rejected", handled);
        assertEquals("HTTP URL must be rejected", "", lastUrl.get());
    }
}
