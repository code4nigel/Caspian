package com.caspian.betac.tabs;

import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

public class TabControllerTest {

    private TabController controller;

    @Before
    public void setUp() {
        controller = new TabController();
        controller.loadSession(TabStateRepository.SessionSnapshot.createDefault());
    }

    @Test
    public void testInitialStateHasDefaultHubTab() {
        assertEquals(1, controller.getTabCount());
        TabState active = controller.getActiveTab();
        assertNotNull(active);
        assertEquals("Caspian Hub", active.title);
        assertEquals(1, active.id);
        assertEquals(1, controller.getActiveTabId());
    }

    @Test
    public void testAddTabAndSwitch() {
        TabState tab2 = controller.addTab("ChatGPT", "https://chatgpt.com", "ai", false, "default");
        assertEquals(2, controller.getTabCount());
        assertEquals(tab2.id, controller.getActiveTabId());
        assertEquals("ChatGPT", controller.getActiveTab().title);

        TabState tab3 = controller.addTab("YouTube", "https://youtube.com", "media", false, "work");
        assertEquals(3, controller.getTabCount());
        assertEquals(tab3.id, controller.getActiveTabId());

        // Switch back to tab2
        boolean switched = controller.switchToTab(tab2.id);
        assertTrue(switched);
        assertEquals(tab2.id, controller.getActiveTabId());
    }

    @Test
    public void testCloseTabSelectsAdjacentActiveTab() {
        TabState tab2 = controller.addTab("Tab 2", "https://two.com", "web", false, null);
        TabState tab3 = controller.addTab("Tab 3", "https://three.com", "web", false, null);
        assertEquals(tab3.id, controller.getActiveTabId());

        // Close tab3 (active tab)
        controller.closeTab(tab3.id);
        assertEquals(2, controller.getTabCount());
        // Should fall back to tab2
        assertEquals(tab2.id, controller.getActiveTabId());
    }

    @Test
    public void testCloseAllTabsCreatesFreshHub() {
        controller.addTab("Tab 2", "https://two.com", "web", false, null);
        controller.closeAllTabs();

        assertEquals(1, controller.getTabCount());
        assertEquals("Caspian Hub", controller.getActiveTab().title);
    }

    @Test
    public void testUndoClosedTab() {
        TabState tab2 = controller.addTab("Undo Me", "https://undo.com", "web", false, null);
        int tab2Id = tab2.id;
        controller.closeTab(tab2Id);

        // Undo close
        List<TabState> restored = controller.restoreLastClosedBatch();
        assertNotNull(restored);
        assertEquals(1, restored.size());
        assertEquals("Undo Me", restored.get(0).title);
        assertEquals(restored.get(0).id, controller.getActiveTabId());
    }

    @Test
    public void testIncognitoTabsAreNotRestorableViaUndo() {
        TabState incognitoTab = controller.addTab("Private Search", "https://duckduckgo.com", "web", true, null);
        controller.closeTab(incognitoTab.id);

        List<TabState> restored = controller.restoreLastClosedBatch();
        assertNull(restored);
    }

    @Test
    public void testReorderTabs() {
        TabState tabA = controller.getActiveTab(); // index 0
        TabState tabB = controller.addTab("Tab B", "https://b.com", "web", false, null); // index 1
        TabState tabC = controller.addTab("Tab C", "https://c.com", "web", false, null); // index 2

        // Move index 2 (Tab C) to index 0
        controller.reorderTabs(2, 0);
        assertEquals(tabC.id, controller.getTabs().get(0).id);
        assertEquals(tabA.id, controller.getTabs().get(1).id);
        assertEquals(tabB.id, controller.getTabs().get(2).id);
    }

    @Test
    public void testSplitModeManagement() {
        TabState tab1 = controller.getActiveTab();
        TabState tab2 = controller.addTab("Tab 2", "https://tab2.com", "web", false, null);

        // Enter split mode
        boolean entered = controller.enterSplitMode(tab1.id, tab2.id, 1, 0.4f);
        assertTrue(entered);
        assertEquals(1, controller.getSplitModeState());
        assertEquals(tab1.id, controller.getActiveTabId());
        assertEquals(tab2.id, controller.getSecondarySplitTabId());
        assertEquals(0.4f, controller.getSplitRatio(), 0.001f);

        // Modify split ratio
        controller.setSplitRatio(0.6f);
        assertEquals(0.6f, controller.getSplitRatio(), 0.001f);

        // Closing secondary tab exits split mode
        controller.closeTab(tab2.id);
        assertEquals(0, controller.getSplitModeState());
        assertEquals(-1, controller.getSecondarySplitTabId());

        // Re-enter and exit split mode explicitly
        TabState tab3 = controller.addTab("Tab 3", "https://tab3.com", "web", false, null);
        controller.enterSplitMode(tab1.id, tab3.id, 2, 0.5f);
        assertEquals(2, controller.getSplitModeState());
        controller.exitSplitMode();
        assertEquals(0, controller.getSplitModeState());
        assertEquals(-1, controller.getSecondarySplitTabId());
    }

    @Test
    public void testTabPropertyMutations() {
        TabState tab = controller.getActiveTab();
        assertFalse(tab.isFavorite);
        assertFalse(tab.isMuted);
        assertFalse(tab.isDesktop);

        controller.setTabFavorite(tab.id, true);
        assertTrue(controller.getActiveTab().isFavorite);

        controller.setTabMuted(tab.id, true);
        assertTrue(controller.getActiveTab().isMuted);

        controller.setTabDesktop(tab.id, true);
        assertTrue(controller.getActiveTab().isDesktop);

        controller.updateTabDetails(tab.id, "Updated Title", "https://updated.com", "Updated Nick");
        assertEquals("Updated Title", controller.getActiveTab().title);
        assertEquals("https://updated.com", controller.getActiveTab().url);
        assertEquals("Updated Nick", controller.getActiveTab().nickname);
    }

    @Test
    public void testTabGroupManagement() {
        TabState tab1 = controller.getActiveTab();
        TabState tab2 = controller.addTab("Tab 2", "https://tab2.com", "web", false, null);

        TabController.TabGroupState group = new TabController.TabGroupState("grp1", "Work", "#ff0000", "💼");
        group.tabIds.add(tab1.id);
        group.tabIds.add(tab2.id);

        controller.addTabGroup(group);
        assertEquals(1, controller.getTabGroups().size());
        assertEquals("Work", controller.getTabGroups().get(0).title);

        controller.removeTabGroup("grp1");
        assertEquals(0, controller.getTabGroups().size());
    }

    @Test
    public void testValidateInvariantsHoldUnderMutations() {
        controller.validateInvariants();

        TabState tabA = controller.getActiveTab();
        TabState tabB = controller.addTab("Tab B", "https://b.com", "web", false, null);
        controller.validateInvariants();

        controller.enterSplitMode(tabA.id, tabB.id, 1, 0.5f);
        controller.validateInvariants();

        controller.exitSplitMode();
        controller.validateInvariants();

        controller.closeTab(tabB.id);
        controller.validateInvariants();
    }

    @Test
    public void testOnSplitModeChangedListenerFired() {
        TabState tabA = controller.getActiveTab();
        TabState tabB = controller.addTab("Tab B", "https://b.com", "web", false, null);

        final int[] lastSplitState = {-1};
        final int[] lastSecondaryId = {-99};
        final float[] lastRatio = {0f};

        controller.setEventListener(new TabController.TabEventListener() {
            @Override public void onTabAdded(TabState tab) {}
            @Override public void onTabSwitched(int oldTabId, int newTabId) {}
            @Override public void onTabClosed(int closedTabId, int newActiveTabId) {}
            @Override public void onTabsUpdated() {}
            @Override public void onSplitModeChanged(int splitState, int secondaryId, float ratio) {
                lastSplitState[0] = splitState;
                lastSecondaryId[0] = secondaryId;
                lastRatio[0] = ratio;
            }
        });

        controller.enterSplitMode(tabA.id, tabB.id, 2, 0.6f);
        assertEquals(2, lastSplitState[0]);
        assertEquals(tabB.id, lastSecondaryId[0]);
        assertEquals(0.6f, lastRatio[0], 0.001f);

        controller.exitSplitMode();
        assertEquals(0, lastSplitState[0]);
        assertEquals(-1, lastSecondaryId[0]);
    }

    @Test
    public void testUpdateTabDetailsUpdatesStateAndFiresListener() {
        TabState tab = controller.getActiveTab();
        final boolean[] updated = {false};
        controller.setEventListener(new TabController.TabEventListener() {
            @Override public void onTabAdded(TabState tab) {}
            @Override public void onTabSwitched(int oldTabId, int newTabId) {}
            @Override public void onTabClosed(int closedTabId, int newActiveTabId) {}
            @Override public void onTabsUpdated() { updated[0] = true; }
            @Override public void onSplitModeChanged(int splitState, int secondaryId, float ratio) {}
        });

        controller.updateTabDetails(tab.id, "YouTube Music", "https://music.youtube.com", "MyMusic");
        assertTrue(updated[0]);
        assertEquals("YouTube Music", tab.title);
        assertEquals("https://music.youtube.com", tab.url);
        assertEquals("MyMusic", tab.nickname);
    }
}

