package com.caspian.betac.security;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class TrustedMediaMessageHandlerTest {

    private final AtomicBoolean timeCalled = new AtomicBoolean(false);
    private final AtomicBoolean stateCalled = new AtomicBoolean(false);
    private final AtomicReference<String> lastTitle = new AtomicReference<>("");
    private final AtomicBoolean videoEndedCalled = new AtomicBoolean(false);

    private final TrustedMediaMessageHandler.MediaMessageCallback callback = new TrustedMediaMessageHandler.MediaMessageCallback() {
        @Override
        public void onTimeUpdate(int tabId, double currentTime, double duration) {
            timeCalled.set(true);
        }

        @Override
        public void onStateUpdate(int tabId, boolean isPlaying, boolean isMuted) {
            stateCalled.set(true);
        }

        @Override
        public void onMetadataUpdate(int tabId, String title, String artist, String thumbnailUrl) {
            lastTitle.set(title);
        }

        @Override
        public void onPlaybackModesUpdate(int tabId, int repeatMode, boolean shuffleOn) {}

        @Override
        public void onVideoEnded(int tabId) {
            videoEndedCalled.set(true);
        }

        @Override
        public void onShowSettingsMenu(int tabId) {}

        @Override
        public void onScrobbyPlayerState(int tabId, String stateJson) {}
    };

    @Before
    public void setUp() {
        timeCalled.set(false);
        stateCalled.set(false);
        lastTitle.set("");
        videoEndedCalled.set(false);
    }

    @Test
    public void testOriginAllowance() {
        assertTrue(TrustedMediaMessageHandler.isOriginAllowed("https://www.youtube.com"));
        assertTrue(TrustedMediaMessageHandler.isOriginAllowed("https://m.youtube.com"));
        assertTrue(TrustedMediaMessageHandler.isOriginAllowed("https://music.youtube.com"));
        assertTrue(TrustedMediaMessageHandler.isOriginAllowed("https://youtube.com"));

        assertFalse(TrustedMediaMessageHandler.isOriginAllowed("https://evil.youtube.com.attacker.com"));
        assertFalse(TrustedMediaMessageHandler.isOriginAllowed("https://fake-youtube.com"));
        assertFalse(TrustedMediaMessageHandler.isOriginAllowed("http://www.youtube.com"));
        assertFalse(TrustedMediaMessageHandler.isOriginAllowed(null));
    }

    @Test
    public void testValidTimeUpdateDispatch() {
        String json = "{\"action\":\"updateTime\",\"currentTime\":42.5,\"duration\":180.0}";
        boolean result = TrustedMediaMessageHandler.parseAndDispatch(1, json, callback);
        assertTrue(result);
        assertTrue(timeCalled.get());
    }

    @Test
    public void testValidStateUpdateDispatch() {
        String json = "{\"action\":\"updateState\",\"isPlaying\":true,\"isMuted\":false}";
        boolean result = TrustedMediaMessageHandler.parseAndDispatch(1, json, callback);
        assertTrue(result);
        assertTrue(stateCalled.get());
    }

    @Test
    public void testMetadataUpdateClamping() {
        StringBuilder longTitle = new StringBuilder();
        for (int i = 0; i < 400; i++) longTitle.append("A");

        String json = "{\"action\":\"updateMetadata\",\"title\":\"" + longTitle + "\",\"artist\":\"Artist\"}";
        boolean result = TrustedMediaMessageHandler.parseAndDispatch(1, json, callback);
        assertTrue(result);
        assertEquals(300, lastTitle.get().length());
    }

    @Test
    public void testRejectsUnknownCommands() {
        String attackJson = "{\"action\":\"getPdfBase64\",\"path\":\"/etc/passwd\"}";
        boolean result = TrustedMediaMessageHandler.parseAndDispatch(1, attackJson, callback);
        assertFalse(result);

        String evalJson = "{\"action\":\"eval\",\"code\":\"alert(1)\"}";
        boolean evalResult = TrustedMediaMessageHandler.parseAndDispatch(1, evalJson, callback);
        assertFalse(evalResult);
    }

    @Test
    public void testRejectsOversizedPayloads() {
        StringBuilder huge = new StringBuilder();
        huge.append("{\"action\":\"updateTime\",\"garbage\":\"");
        for (int i = 0; i < 20000; i++) huge.append("X");
        huge.append("\"}");

        boolean result = TrustedMediaMessageHandler.parseAndDispatch(1, huge.toString(), callback);
        assertFalse(result);
    }
}
