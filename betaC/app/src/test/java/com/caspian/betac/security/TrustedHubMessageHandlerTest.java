package com.caspian.betac.security;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class TrustedHubMessageHandlerTest {

    private final AtomicReference<String> lastUrl = new AtomicReference<>("");
    private final AtomicReference<String> lastService = new AtomicReference<>("");
    private final AtomicReference<String> lastToast = new AtomicReference<>("");

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
        public void onPlayAssetSound(String sound) {}

        @Override
        public void onShowToast(String message) {
            lastToast.set(message);
        }

        @Override
        public void onShowKeyboard() {}

        @Override
        public void onSaveWallpaper(String wallpaper) {}

        @Override
        public void onSwitchCask(String caskId) {}
    };

    @Before
    public void setUp() {
        lastUrl.set("");
        lastService.set("");
        lastToast.set("");
    }

    @Test
    public void testOriginAllowance() {
        assertTrue("launch_hub.html must be allowed",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/launch_hub.html"));
        assertTrue("Other local assets must be allowed",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/pdf_viewer.html"));

        assertFalse("Remote https must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("https://chatgpt.com"));
        assertFalse("Remote http must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("http://example.com"));
        assertFalse("Relative or null url must be rejected",
                TrustedHubMessageHandler.isOriginAllowed(null));
        assertFalse("Evil subpath must be rejected",
                TrustedHubMessageHandler.isOriginAllowed("file:///android_asset/../sdcard/evil.html"));
    }
}
