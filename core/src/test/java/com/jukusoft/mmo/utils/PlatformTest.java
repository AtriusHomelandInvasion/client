package com.jukusoft.mmo.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlatformTest {

    @Test
    public void testConstructor () {
        new Platform();
    }

    @Test
    public void testAdd () {
        assertEquals(0, Platform.uiQueue.size());

        Platform.runOnUIThread(() -> {
            //
        });

        assertEquals(1, Platform.uiQueue.size());
    }

    @Test
    public void testExecute () {
        assertEquals(0, Platform.uiQueue.size());

        Platform.runOnUIThread(() -> {
            //
        });

        assertEquals(1, Platform.uiQueue.size());

        //execute queue
        Platform.executeQueue();

        assertEquals(0, Platform.uiQueue.size());

        //execute again
        Platform.executeQueue();
    }

}
