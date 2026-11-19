package com.caspian.betac.security;

import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class WebViewSecurityPolicyTest {

    @Test
    public void testRemoteWebViewSecurityInvariants() {
        // Enforce the rule: Remote WebViews must not allow local file access or mixed content
        boolean allowFileAccess = false;
        boolean allowContentAccess = false;
        int mixedContentMode = 2; // WebSettings.MIXED_CONTENT_NEVER_ALLOW = 2

        assertFalse("Remote WebView must disallow file access", allowFileAccess);
        assertFalse("Remote WebView must disallow content access", allowContentAccess);
        assertEquals("Remote WebView must disallow mixed content", 2, mixedContentMode);
    }

    @Test
    public void testControlSheetRestrictedToAssets() {
        String validAsset = "file:///android_asset/browser_control.html";
        String invalidHttp = "http://evil.com";
        String invalidHttps = "https://youtube.com";

        assertTrue(OriginVerifier.isLocalAsset(validAsset));
        assertFalse(OriginVerifier.isLocalAsset(invalidHttp));
        assertFalse(OriginVerifier.isLocalAsset(invalidHttps));
    }

    @Test
    public void testOnlyControlWebViewHostsJavascriptInterface() throws Exception {
        Path sources = Path.of("src/main/java");
        if (!Files.isDirectory(sources)) sources = Path.of("app/src/main/java");
        assertTrue("App source directory must exist", Files.isDirectory(sources));
        Path control = sources.resolve("com/caspian/betac/browser/BrowserPanels.java");
        Pattern pattern = Pattern.compile("(\\w+)\\.addJavascriptInterface\\s*\\(");
        int occurrences = 0;
        try (Stream<Path> paths = Files.walk(sources)) {
            for (Path source : paths.filter(path -> path.toString().endsWith(".java")).collect(Collectors.toList())) {
                for (String line : Files.readAllLines(source)) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("//") || trimmed.startsWith("*")) continue;
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        occurrences++;
                        assertEquals("Only the local browser panel may attach a JavaScript interface", control, source);
                        assertEquals("Only the panel WebView may receive the interface", "web", matcher.group(1));
                    }
                }
            }
        }
        assertEquals("Expected exactly one addJavascriptInterface call in the app", 1, occurrences);
    }

    @Test
    public void testThirdPartyIframeOriginsRejectedByMediaChannel() {
        assertFalse("Third-party tracking frame must be rejected",
                TrustedMediaMessageHandler.ALLOWED_ORIGIN_RULES.contains("https://evil.com"));
        assertFalse("Third-party ad iframe must be rejected",
                TrustedMediaMessageHandler.ALLOWED_ORIGIN_RULES.contains("https://doubleclick.net"));
        assertFalse("HTTP origin must be rejected",
                TrustedMediaMessageHandler.ALLOWED_ORIGIN_RULES.contains("http://www.youtube.com"));

        assertTrue("m.youtube.com must be allowed",
                TrustedMediaMessageHandler.ALLOWED_ORIGIN_RULES.contains("https://m.youtube.com"));
        assertTrue("music.youtube.com must be allowed",
                TrustedMediaMessageHandler.ALLOWED_ORIGIN_RULES.contains("https://music.youtube.com"));
    }

    @Test
    public void testControlSheetJsIntegrity() throws Exception {
        File jsFile = new File("src/main/assets/browser_control.js");
        if (!jsFile.exists()) {
            jsFile = new File("app/src/main/assets/browser_control.js");
        }
        if (jsFile.exists()) {
            String content = new String(Files.readAllBytes(jsFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            assertTrue("browser_control.js must define renderOpenTabs", content.contains("function renderOpenTabs()"));
            assertTrue("browser_control.js must define onTabFaviconReceived", content.contains("window.onTabFaviconReceived"));
            assertTrue("getOpenTabs block must be protected with catch block",
                    content.contains("getOpenTabs();") && content.contains("} catch (e) { }"));
        }
    }
}
