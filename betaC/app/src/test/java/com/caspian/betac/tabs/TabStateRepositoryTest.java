package com.caspian.betac.tabs;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TabStateRepositoryTest {

    @Test
    public void testSerializationAndDeserializationRoundTrip() {
        List<TabState> originalTabs = new ArrayList<>();
        TabState t1 = new TabState(1, "Google", "https://google.com", "web", false);
        t1.nickname = "Search";
        t1.isFavorite = true;
        t1.caskId = "work";

        TabState t2 = new TabState(2, "ChatGPT", "https://chatgpt.com", "ai", false);
        t2.isDesktop = true;

        originalTabs.add(t1);
        originalTabs.add(t2);

        String json = TabStateRepository.serializeTabsToJson(originalTabs);
        assertNotNull(json);
        assertTrue(json.contains("Google"));
        assertTrue(json.contains("ChatGPT"));

        List<TabState> restored = TabStateRepository.deserializeTabsFromJson(json);
        assertEquals(2, restored.size());

        TabState r1 = restored.get(0);
        assertEquals(1, r1.id);
        assertEquals("Google", r1.title);
        assertEquals("Search", r1.nickname);
        assertEquals("https://google.com", r1.url);
        assertTrue(r1.isFavorite);
        assertEquals("work", r1.caskId);
        assertFalse(r1.isIncognito);

        TabState r2 = restored.get(1);
        assertEquals(2, r2.id);
        assertEquals("ChatGPT", r2.title);
        assertTrue(r2.isDesktop);
    }

    @Test
    public void testIncognitoTabsAreNeverSerialized() {
        List<TabState> tabs = new ArrayList<>();
        tabs.add(new TabState(1, "Public Tab", "https://example.com", "web", false));
        tabs.add(new TabState(2, "Secret Tab", "https://secret.com", "web", true));

        String json = TabStateRepository.serializeTabsToJson(tabs);
        assertFalse(json.contains("Secret Tab"));
        assertFalse(json.contains("secret.com"));

        List<TabState> restored = TabStateRepository.deserializeTabsFromJson(json);
        assertEquals(1, restored.size());
        assertEquals("Public Tab", restored.get(0).title);
    }

    @Test
    public void testCorruptJsonHandlesSafelyWithoutCrash() {
        List<TabState> tabs1 = TabStateRepository.deserializeTabsFromJson("{not an array}");
        assertTrue(tabs1.isEmpty());

        List<TabState> tabs2 = TabStateRepository.deserializeTabsFromJson(null);
        assertTrue(tabs2.isEmpty());

        List<TabState> tabs3 = TabStateRepository.deserializeTabsFromJson("");
        assertTrue(tabs3.isEmpty());
    }

    @Test
    public void testDefaultSessionSnapshot() {
        TabStateRepository.SessionSnapshot snapshot = TabStateRepository.SessionSnapshot.createDefault();
        assertEquals(1, snapshot.tabs.size());
        assertEquals(1, snapshot.activeTabId);
        assertEquals(-1, snapshot.secondarySplitId);
        assertEquals("file:///android_asset/launch_hub.html", snapshot.tabs.get(0).url);
    }
}
