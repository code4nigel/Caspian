package com.caspian.betac.browser;

import org.junit.Test;

import static com.caspian.betac.browser.BrowserGestures.Swipe.*;
import static org.junit.Assert.assertEquals;

public class BrowserGesturesTest {
    @Test public void determinesDirectionFromTheDominantAxis() {
        assertEquals(LEFT, BrowserGestures.classify(-48, 12, 26));
        assertEquals(RIGHT, BrowserGestures.classify(48, -12, 26));
        assertEquals(UP, BrowserGestures.classify(9, -55, 26));
        assertEquals(DOWN, BrowserGestures.classify(-9, 55, 26));
    }

    @Test public void ignoresJitterAndAmbiguousDiagonals() {
        assertEquals(NONE, BrowserGestures.classify(22, 8, 26));
        assertEquals(NONE, BrowserGestures.classify(-55, 51, 26));
    }
}
