package com.caspian.betac.tabs;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * On-device Android instrumentation tests for TabController and tab invariants.
 */
@RunWith(AndroidJUnit4.class)
public class TabControllerDeviceTest {

    private TabController controller;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        assertNotNull(context);
        controller = new TabController();
        controller.loadSession(TabStateRepository.SessionSnapshot.createDefault());
    }

    @Test
    public void testInvariantsHoldOnInitialLaunch() {
        controller.validateInvariants();
        assertEquals(1, controller.getTabCount());
        assertEquals(1, controller.getActiveTabId());
        assertEquals(0, controller.getSplitModeState());
        assertEquals(-1, controller.getSecondarySplitTabId());
    }

    @Test
    public void testSplitModeInvariantsAndListener() {
        TabState tabA = controller.getActiveTab();
        TabState tabB = controller.addTab("Secondary", "https://example.com", "web", false, null);

        AtomicInteger listenerState = new AtomicInteger(-1);
        AtomicInteger listenerSecondary = new AtomicInteger(-99);
        controller.setEventListener(new TabController.TabEventListener() {
            @Override public void onTabAdded(TabState tab) {}
            @Override public void onTabSwitched(int oldTabId, int newTabId) {}
            @Override public void onTabClosed(int closedTabId, int newActiveTabId) {}
            @Override public void onTabsUpdated() {}
            @Override public void onSplitModeChanged(int splitState, int secondaryId, float ratio) {
                listenerState.set(splitState);
                listenerSecondary.set(secondaryId);
            }
        });

        // Enter split mode
        boolean success = controller.enterSplitMode(tabA.id, tabB.id, 1, 0.5f);
        assertTrue(success);
        controller.validateInvariants();
        assertEquals(1, listenerState.get());
        assertEquals(tabB.id, listenerSecondary.get());

        // Exit split mode
        controller.exitSplitMode();
        controller.validateInvariants();
        assertEquals(0, listenerState.get());
        assertEquals(-1, listenerSecondary.get());
    }

    @Test
    public void testIncognitoTabNotPersistedInUndoBatch() {
        TabState normalTab = controller.getActiveTab();
        TabState incognitoTab = controller.addTab("Secret", "https://duckduckgo.com", "web", true, null);
        controller.validateInvariants();

        // Close incognito tab
        controller.closeTab(incognitoTab.id, true);
        controller.validateInvariants();

        List<TabState> restored = controller.restoreLastClosedBatch();
        assertNull("Incognito tabs must never be retrievable from undo history", restored);
    }

    @Test
    public void testRapidTabCyclingMaintainsIntegrity() {
        for (int i = 0; i < 20; i++) {
            controller.addTab("Tab " + i, "https://test" + i + ".com", "web", false, null);
            controller.validateInvariants();
        }
        assertEquals(21, controller.getTabCount());

        // Reorder tabs
        controller.reorderTabs(20, 0);
        controller.validateInvariants();

        // Close tabs from middle
        for (int i = 0; i < 10; i++) {
            List<TabState> tabs = controller.getTabs();
            controller.closeTab(tabs.get(tabs.size() / 2).id);
            controller.validateInvariants();
        }
        assertEquals(11, controller.getTabCount());
    }
}
