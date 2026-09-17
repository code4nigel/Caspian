package com.caspian.betac.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Controller responsible for all browser tab state, mutations, ordering, and history.
 * Decouples tab business logic completely from Android UI activities and views.
 */
public class TabController {

    public interface TabEventListener {
        void onTabAdded(TabState tab);
        void onTabSwitched(int oldTabId, int newTabId);
        void onTabClosed(int closedTabId, int newActiveTabId);
        void onTabsUpdated();
    }

    private final List<TabState> tabs = new ArrayList<>();
    private final List<List<TabState>> closedTabBatches = new ArrayList<>();
    private int activeTabId = -1;
    private int secondarySplitTabId = -1;
    private int splitModeState = 0;
    private float splitRatio = 0.5f;
    private int nextTabId = 1;

    private TabEventListener eventListener;

    public TabController() {}

    public void setEventListener(TabEventListener listener) {
        this.eventListener = listener;
    }

    public List<TabState> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

    public int getTabCount() {
        return tabs.size();
    }

    public int getActiveTabId() {
        return activeTabId;
    }

    public int getSecondarySplitTabId() {
        return secondarySplitTabId;
    }

    public int getSplitModeState() {
        return splitModeState;
    }

    public float getSplitRatio() {
        return splitRatio;
    }

    public int getNextTabId() {
        return nextTabId;
    }

    public TabState getTabById(int tabId) {
        for (TabState tab : tabs) {
            if (tab.id == tabId) return tab;
        }
        return null;
    }

    public TabState getActiveTab() {
        return getTabById(activeTabId);
    }

    public int generateNextId() {
        return nextTabId++;
    }

    public void loadSession(TabStateRepository.SessionSnapshot snapshot) {
        tabs.clear();
        closedTabBatches.clear();
        if (snapshot != null && !snapshot.tabs.isEmpty()) {
            tabs.addAll(snapshot.tabs);
            activeTabId = snapshot.activeTabId;
            secondarySplitTabId = snapshot.secondarySplitId;
            splitModeState = snapshot.splitModeState;
            splitRatio = snapshot.splitRatio;
            nextTabId = snapshot.nextTabId;
        } else {
            TabState defaultHub = new TabState(generateNextId(), "Caspian Hub", TabStateRepository.DEFAULT_HUB_URL, "hub", false);
            tabs.add(defaultHub);
            activeTabId = defaultHub.id;
            secondarySplitTabId = -1;
            splitModeState = 0;
            splitRatio = 0.5f;
        }
        if (eventListener != null) {
            eventListener.onTabsUpdated();
        }
    }

    public TabState addTab(String title, String url, String service, boolean isIncognito, String caskId) {
        int id = generateNextId();
        TabState tab = new TabState(id, title, url, service, isIncognito);
        if (caskId != null) {
            tab.caskId = caskId;
        }
        tabs.add(tab);
        switchToTab(id);
        if (eventListener != null) {
            eventListener.onTabAdded(tab);
        }
        return tab;
    }

    public boolean switchToTab(int tabId) {
        if (tabId == activeTabId) return false;
        TabState target = getTabById(tabId);
        if (target == null) return false;

        int oldId = activeTabId;
        activeTabId = tabId;
        if (eventListener != null) {
            eventListener.onTabSwitched(oldId, activeTabId);
        }
        return true;
    }

    public boolean closeTab(int tabId) {
        int index = -1;
        TabState tabToClose = null;
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).id == tabId) {
                index = i;
                tabToClose = tabs.get(i);
                break;
            }
        }
        if (tabToClose == null) return false;

        // Push to undo batch if not incognito
        if (!tabToClose.isIncognito) {
            List<TabState> singleBatch = new ArrayList<>();
            singleBatch.add(tabToClose);
            closedTabBatches.add(singleBatch);
        }

        tabs.remove(index);

        // Adjust split state if this tab was participating in split
        if (secondarySplitTabId == tabId) {
            secondarySplitTabId = -1;
            splitModeState = 0;
        }

        int newActiveId = activeTabId;
        if (activeTabId == tabId) {
            if (!tabs.isEmpty()) {
                // Pick adjacent tab
                int newIndex = Math.min(index, tabs.size() - 1);
                newActiveId = tabs.get(newIndex).id;
            } else {
                // Last tab was closed: spawn fresh hub
                TabState defaultHub = new TabState(generateNextId(), "Caspian Hub", TabStateRepository.DEFAULT_HUB_URL, "hub", false);
                tabs.add(defaultHub);
                newActiveId = defaultHub.id;
            }
            activeTabId = newActiveId;
        }

        if (eventListener != null) {
            eventListener.onTabClosed(tabId, activeTabId);
            eventListener.onTabsUpdated();
        }
        return true;
    }

    public void closeMultipleTabs(List<Integer> tabIds) {
        if (tabIds == null || tabIds.isEmpty()) return;
        List<TabState> batch = new ArrayList<>();
        Iterator<TabState> it = tabs.iterator();
        while (it.hasNext()) {
            TabState tab = it.next();
            if (tabIds.contains(tab.id)) {
                if (!tab.isIncognito) {
                    batch.add(tab);
                }
                if (secondarySplitTabId == tab.id) {
                    secondarySplitTabId = -1;
                    splitModeState = 0;
                }
                it.remove();
            }
        }
        if (!batch.isEmpty()) {
            closedTabBatches.add(batch);
        }

        if (getTabById(activeTabId) == null) {
            if (!tabs.isEmpty()) {
                activeTabId = tabs.get(0).id;
            } else {
                TabState defaultHub = new TabState(generateNextId(), "Caspian Hub", TabStateRepository.DEFAULT_HUB_URL, "hub", false);
                tabs.add(defaultHub);
                activeTabId = defaultHub.id;
            }
        }

        if (eventListener != null) {
            eventListener.onTabsUpdated();
        }
    }

    public List<TabState> restoreLastClosedBatch() {
        if (closedTabBatches.isEmpty()) return null;
        List<TabState> batch = closedTabBatches.remove(closedTabBatches.size() - 1);
        if (batch.isEmpty()) return null;

        for (TabState tab : batch) {
            // Re-assign a valid unique ID if collision
            if (getTabById(tab.id) != null) {
                tab.id = generateNextId();
            }
            tabs.add(tab);
        }
        activeTabId = batch.get(batch.size() - 1).id;
        if (eventListener != null) {
            eventListener.onTabsUpdated();
        }
        return batch;
    }

    public void reorderTabs(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= tabs.size() || toIndex < 0 || toIndex >= tabs.size()) return;
        TabState moved = tabs.remove(fromIndex);
        tabs.add(toIndex, moved);
        if (eventListener != null) {
            eventListener.onTabsUpdated();
        }
    }

    public void closeAllTabs() {
        List<TabState> batch = new ArrayList<>();
        for (TabState tab : tabs) {
            if (!tab.isIncognito) {
                batch.add(tab);
            }
        }
        if (!batch.isEmpty()) {
            closedTabBatches.add(batch);
        }
        tabs.clear();
        secondarySplitTabId = -1;
        splitModeState = 0;

        TabState defaultHub = new TabState(generateNextId(), "Caspian Hub", TabStateRepository.DEFAULT_HUB_URL, "hub", false);
        tabs.add(defaultHub);
        activeTabId = defaultHub.id;

        if (eventListener != null) {
            eventListener.onTabsUpdated();
        }
    }
}
