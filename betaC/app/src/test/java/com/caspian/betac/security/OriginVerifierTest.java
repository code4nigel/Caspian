package com.caspian.betac.security;

import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class OriginVerifierTest {

    @Test
    public void testIsLocalAsset() {
        assertTrue(OriginVerifier.isLocalAsset("file:///android_asset/launch_hub.html"));
        assertTrue(OriginVerifier.isLocalAsset("file:///android_asset/browser_control.html"));
        assertTrue(OriginVerifier.isLocalAsset("file:///android_asset/pdf_viewer.html"));

        assertFalse(OriginVerifier.isLocalAsset("https://google.com"));
        assertFalse(OriginVerifier.isLocalAsset("http://evil.com/fake/file:///android_asset/"));
        assertFalse(OriginVerifier.isLocalAsset(null));
        assertFalse(OriginVerifier.isLocalAsset(""));
    }

    @Test
    public void testIsPdfViewer() {
        assertTrue(OriginVerifier.isPdfViewer("file:///android_asset/pdf_viewer.html"));
        assertTrue(OriginVerifier.isPdfViewer("file:///android_asset/pdf_viewer.html?file=/sdcard/doc.pdf"));

        assertFalse(OriginVerifier.isPdfViewer("file:///android_asset/launch_hub.html"));
        assertFalse(OriginVerifier.isPdfViewer("https://evil.com/pdf_viewer.html"));
        assertFalse(OriginVerifier.isPdfViewer(null));
    }

    @Test
    public void testIsTrustedMediaHost() {
        assertTrue(OriginVerifier.isTrustedMediaHost("https://www.youtube.com/watch?v=12345"));
        assertTrue(OriginVerifier.isTrustedMediaHost("https://m.youtube.com/watch?v=12345"));
        assertTrue(OriginVerifier.isTrustedMediaHost("https://music.youtube.com/"));
        assertTrue(OriginVerifier.isTrustedMediaHost("https://youtube.com/"));

        // Host spoofing attempts
        assertFalse(OriginVerifier.isTrustedMediaHost("https://youtube.com.evil.com/"));
        assertFalse(OriginVerifier.isTrustedMediaHost("https://evil-youtube.com/"));
        assertFalse(OriginVerifier.isTrustedMediaHost("https://notyoutube.com/"));
        assertFalse(OriginVerifier.isTrustedMediaHost(null));
        assertFalse(OriginVerifier.isTrustedMediaHost(""));
    }

    @Test
    public void testIsTrustedAiHost() {
        assertTrue(OriginVerifier.isTrustedAiHost("https://chatgpt.com/"));
        assertTrue(OriginVerifier.isTrustedAiHost("https://www.chatgpt.com/c/12345"));
        assertTrue(OriginVerifier.isTrustedAiHost("https://gemini.google.com/app"));

        assertFalse(OriginVerifier.isTrustedAiHost("https://chatgpt.com.attacker.com/"));
        assertFalse(OriginVerifier.isTrustedAiHost("https://fakegemini.com/"));
        assertFalse(OriginVerifier.isTrustedAiHost("https://google.com/search"));
        assertFalse(OriginVerifier.isTrustedAiHost(null));
    }

    @Test
    public void testIsSafePdfPath() throws IOException {
        // Traversal and malformed paths
        assertFalse(OriginVerifier.isSafePdfPath(null));
        assertFalse(OriginVerifier.isSafePdfPath(""));
        assertFalse(OriginVerifier.isSafePdfPath("../../../etc/passwd"));
        assertFalse(OriginVerifier.isSafePdfPath("/data/data/com.caspian.betac/shared_prefs/prefs.xml"));
        assertFalse(OriginVerifier.isSafePdfPath("/data/data/com.caspian.betac/databases/history.db"));
        assertFalse(OriginVerifier.isSafePdfPath("/etc/hosts.pdf"));
        assertFalse(OriginVerifier.isSafePdfPath("file.txt"));

        // Valid PDF file creation
        File tempPdf = File.createTempFile("sample_test_doc", ".pdf");
        try {
            try (FileWriter writer = new FileWriter(tempPdf)) {
                writer.write("%PDF-1.4 sample content");
            }
            assertTrue(OriginVerifier.isSafePdfPath(tempPdf.getAbsolutePath()));
        } finally {
            tempPdf.delete();
        }
    }
}
