package com.caspian.betac.tabs;

import android.content.SharedPreferences;

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
        void onSplitModeChanged(int splitModeState, int secondaryTabId, float splitRatio);
    }

    public static class TabGroupState {
        public String id;
        public String title;
        public String color;
        public String icon;
        public boolean isFavorite = false;
        public final List<Integer> tabIds = new ArrayList<>();

        public TabGroupState(String id, String title, String color, String icon) {
            this.id = id;
            this.title = title;
            this.color = color != null ? color : "#ef4444";
            this.icon = icon != null ? icon : "📁";
        }
    }

    private final List<TabState> tabs = new ArrayList<>();
    private final List<List<TabState>> closedTabBatches = new ArrayList<>();
    private final List<TabGroupState> tabGroups = new ArrayList<>();
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

    public void setSplitRatio(float ratio) {
        this.splitRatio = Math.max(0.1f, Math.min(0.9f, ratio));
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

    public void loadSession(SharedPreferences prefs) {
        TabStateRepository.SessionSnapshot snapshot = TabStateRepository.restoreSession(prefs);
        loadSession(snapshot);
    }

    public void saveSession(SharedPreferences prefs) {
        TabStateRepository.saveSession(prefs, tabs, activeTabId, secondarySplitTabId, splitModeState, splitRatio, nextTabId);
    }

    public void syncFromTabStates(List<TabState> updatedTabs, int activeId, int secondarySplitId, int splitState, float ratio) {
        tabs.clear();
        if (updatedTabs != null) {
            tabs.addAll(updatedTabs);
        }
        this.activeTabId = activeId;
        this.secondarySplitTabId = secondarySplitId;
        this.splitModeState = splitState;
        this.splitRatio = ratio;
        for (TabState tab : tabs) {
            if (tab.id >= nextTabId) {
                nextTabId = tab.id + 1;
            }
        }
    }

    public TabState addTab(String title, String url, String service, boolean isIncognito, String caskId) {
        return addTab(title, url, service, isIncognito, caskId, true);
    }

    public TabState addTab(String title, String url, String service, boolean isIncognito, String caskId, boolean switchTo) {
        int id = generateNextId();
        TabState tab = new TabState(id, title, url, service, isIncognito);
        if (caskId != null) {
            tab.caskId = caskId;
        }
        tabs.add(tab);
        if (switchTo || activeTabId == -1) {
            switchToTab(id);
        }
        if (eventListener != null) {
            eventListener.onTabAdded(tab);
        }
        return tab;
    }

    public void addTab(TabState tab, boolean switchTo) {
        if (tab == null) return;
        if (tab.id >= nextTabId) {
            nextTabId = tab.id + 1;
        }
        tabs.add(tab);
        if (switchTo || activeTabId == -1) {
            switchToTab(tab.id);
        }
        if (eventListener != null) {
            eventListener.onTabAdded(tab);
        }
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
        return closeTab(tabId, true);
    }

    public boolean closeTab(int tabId, boolean recordHistory) {
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
        if (recordHistory && !tabToClose.isIncognito) {
            List<TabState> singleBatch = new ArrayList<>();
            singleBatch.add(tabToClose);
            closedTabBatches.add(singleBatch);
            if (closedTabBatches.size() > 30) {
                closedTabBatches.remove(0);
            }
        }

        tabs.remove(index);

        // Adjust split state if this tab was participating in split
        int newActiveId = activeTabId;
        if (secondarySplitTabId == tabId) {
            secondarySplitTabId = -1;
            splitModeState = 0;
            if (eventListener != null) eventListener.onSplitModeChanged(0, -1, splitRatio);
        } else if (activeTabId == tabId && secondarySplitTabId != -1) {
            newActiveId = secondarySplitTabId;
            secondarySplitTabId = -1;
            splitModeState = 0;
            if (eventListener != null) eventListener.onSplitModeChanged(0, -1, splitRatio);
        }

        for (TabState t : tabs) {
            if (t.splitPartnerId == tabId) {
                t.splitPartnerId = -1;
                t.splitRole = "";
                t.splitOrientation = 0;
                t.splitName = "";
            }
        }

        // Invariant: Clean up group memberships
        for (TabGroupState g : tabGroups) {
            g.tabIds.remove((Integer) tabId);
        }
        tabGroups.removeIf(g -> g.tabIds.isEmpty());

        if (activeTabId == tabId) {
            if (newActiveId == tabId) {
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
            }
            activeTabId = newActiveId;
        }

        if (eventListener != null) {
            eventListener.onTabClosed(tabId, activeTabId);
            eventListener.onTabsUpdated();
        }
        return true;
    }

    public boolean reorderTabs(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= tabs.size() || toPosition < 0 || toPosition >= tabs.size()) {
            return false;
        }
        TabState item = tabs.remove(fromPosition);
        tabs.add(toPosition, item);
        if (eventListener != null) {
            eventListener.onTabsUpdated();
        }
        return true;
    }

    public int getTabPosition(int tabId) {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).id == tabId) return i;
        }
        return -1;
    }

    public boolean hasClosedTabsToUndo() {
        return !closedTabBatches.isEmpty();
    }

    public void clearUndoHistory() {
        closedTabBatches.clear();
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

    public boolean enterSplitMode(int primaryTabId, int secondaryTabId, int orientation, float ratio) {
        TabState primary = getTabById(primaryTabId);
        TabState secondary = getTabById(secondaryTabId);
        if (primary == null || secondary == null || primaryTabId == secondaryTabId) return false;

        this.activeTabId = primaryTabId;
        this.secondarySplitTabId = secondaryTabId;
        this.splitModeState = (orientation == 1) ? 1 : 2;
        this.splitRatio = ratio;

        primary.splitPartnerId = secondaryTabId;
        primary.splitRole = "primary";
        primary.splitOrientation = orientation;

        secondary.splitPartnerId = primaryTabId;
        secondary.splitRole = "secondary";
        secondary.splitOrientation = orientation;

        if (eventListener != null) {
            eventListener.onSplitModeChanged(splitModeState, secondarySplitTabId, splitRatio);
            eventListener.onTabsUpdated();
        }
        return true;
    }

    public void exitSplitMode() {
        if (splitModeState == 0 && secondarySplitTabId == -1) return;
        if (secondarySplitTabId != -1) {
            TabState sec = getTabById(secondarySplitTabId);
            if (sec != null) {
                sec.splitPartnerId = -1;
                sec.splitRole = "";
            }
        }
        TabState prim = getActiveTab();
        if (prim != null) {
            prim.splitPartnerId = -1;
            prim.splitRole = "";
        }
        this.secondarySplitTabId = -1;
        this.splitModeState = 0;
        if (eventListener != null) {
            eventListener.onSplitModeChanged(0, -1, splitRatio);
            eventListener.onTabsUpdated();
        }
    }

    public void setTabFavorite(int tabId, boolean isFav) {
        TabState tab = getTabById(tabId);
        if (tab != null) {
            tab.isFavorite = isFav;
            if (eventListener != null) eventListener.onTabsUpdated();
        }
    }

    public void setTabMuted(int tabId, boolean isMuted) {
        TabState tab = getTabById(tabId);
        if (tab != null) {
            tab.isMuted = isMuted;
            if (eventListener != null) eventListener.onTabsUpdated();
        }
    }

    public void setTabDesktop(int tabId, boolean isDesktop) {
        TabState tab = getTabById(tabId);
        if (tab != null) {
            tab.isDesktop = isDesktop;
            if (eventListener != null) eventListener.onTabsUpdated();
        }
    }

    public void updateTabDetails(int tabId, String title, String url, String nickname) {
        TabState tab = getTabById(tabId);
        if (tab != null) {
            if (title != null) tab.title = title;
            if (url != null && !url.isEmpty()) tab.url = url;
            if (nickname != null) tab.nickname = nickname;
            if (eventListener != null) eventListener.onTabsUpdated();
        }
    }

    public void updateTabMetadata(int tabId, String nickname, String caskId) {
        TabState tab = getTabById(tabId);
        if (tab != null) {
            if (nickname != null) tab.nickname = nickname;
            if (caskId != null && !caskId.isEmpty()) tab.caskId = caskId;
            if (eventListener != null) eventListener.onTabsUpdated();
        }
    }

    public List<TabGroupState> getTabGroups() {
        return Collections.unmodifiableList(tabGroups);
    }

    public void addTabGroup(TabGroupState group) {
        if (group != null && !tabGroups.contains(group)) {
            tabGroups.add(group);
            if (eventListener != null) eventListener.onTabsUpdated();
        }
    }

    public void removeTabGroup(String groupId) {
        if (groupId == null) return;
        tabGroups.removeIf(g -> groupId.equals(g.id));
        if (eventListener != null) eventListener.onTabsUpdated();
    }

    /**
     * Validates that runtime invariants are preserved:
     * 1. Every activeTabId matches an existing tab.
     * 2. When split mode is active, secondarySplitTabId is valid and not equal to activeTabId.
     * 3. No group references non-existent tab IDs.
     * 4. Tab IDs are unique.
     */
    public boolean validateInvariants() {
        if (tabs.isEmpty()) {
            return activeTabId == -1;
        }
        if (getTabById(activeTabId) == null) {
            return false;
        }
        if (splitModeState > 0) {
            if (secondarySplitTabId == -1 || secondarySplitTabId == activeTabId || getTabById(secondarySplitTabId) == null) {
                return false;
            }
        }
        for (TabGroupState group : tabGroups) {
            for (Integer id : group.tabIds) {
                if (getTabById(id) == null) return false;
            }
        }
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (TabState tab : tabs) {
            if (!seen.add(tab.id)) return false;
        }
        return true;
    }
}
