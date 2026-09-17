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
}
