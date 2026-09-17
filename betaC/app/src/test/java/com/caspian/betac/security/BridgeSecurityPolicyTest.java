package com.caspian.betac.security;

import org.junit.Test;
import static org.junit.Assert.*;

public class BridgeSecurityPolicyTest {

    @Test
    public void testControlSheetHasLocalUIAccess() {
        assertTrue(BridgeSecurityPolicy.isAuthorized(
                "saveSetting",
                BridgeSecurityPolicy.Category.LOCAL_UI,
                null,
                true
        ));
        assertTrue(BridgeSecurityPolicy.isAuthorized(
                "getOpenTabsJson",
                BridgeSecurityPolicy.Category.LOCAL_UI,
                null,
                true
        ));
    }

    @Test
    public void testRemoteUntrustedPageBlockedFromPrivilegedMethods() {
        String remoteUrl = "https://example-untrusted-site.com/index.html";

        // Local UI operations like preferences and tabs must be rejected
        assertFalse(BridgeSecurityPolicy.isAuthorized(
                "saveSetting",
                BridgeSecurityPolicy.Category.LOCAL_UI,
                remoteUrl,
                false
        ));
        assertFalse(BridgeSecurityPolicy.isAuthorized(
                "getOpenTabsJson",
                BridgeSecurityPolicy.Category.LOCAL_UI,
                remoteUrl,
                false
        ));
        assertFalse(BridgeSecurityPolicy.isAuthorized(
                "deleteDownload",
                BridgeSecurityPolicy.Category.LOCAL_UI,
                remoteUrl,
                false
        ));

        // PDF reading must be rejected
        assertFalse(BridgeSecurityPolicy.isAuthorized(
                "getPdfBase64",
                BridgeSecurityPolicy.Category.PDF_VIEWER,
                remoteUrl,
                false
        ));

        // Media controls must be rejected from non-media domains
        assertFalse(BridgeSecurityPolicy.isAuthorized(
                "updateYouTubeState",
                BridgeSecurityPolicy.Category.TRUSTED_MEDIA,
                remoteUrl,
                false
        ));
    }

    @Test
    public void testLocalPdfViewerAuthorizedForPdfMethodsOnly() {
        String pdfUrl = "file:///android_asset/pdf_viewer.html";

        assertTrue(BridgeSecurityPolicy.isAuthorized(
                "getPdfBase64",
                BridgeSecurityPolicy.Category.PDF_VIEWER,
                pdfUrl,
                false
        ));
    }

    @Test
    public void testYouTubeAuthorizedForMediaMethodsOnly() {
        String ytUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

        assertTrue(BridgeSecurityPolicy.isAuthorized(
                "updateYouTubeState",
                BridgeSecurityPolicy.Category.TRUSTED_MEDIA,
                ytUrl,
                false
        ));

        // YouTube must NOT be authorized to read PDFs or manipulate Caspian preferences
        assertFalse(BridgeSecurityPolicy.isAuthorized(
                "getPdfBase64",
                BridgeSecurityPolicy.Category.PDF_VIEWER,
                ytUrl,
                false
        ));
        assertFalse(BridgeSecurityPolicy.isAuthorized(
                "saveSetting",
                BridgeSecurityPolicy.Category.LOCAL_UI,
                ytUrl,
                false
        ));
    }
}
