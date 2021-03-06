package com.jukusoft.mmo.engine.graphics.screen.impl;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.jukusoft.mmo.engine.GameUnitTest;
import com.jukusoft.mmo.engine.exception.RequiredServiceNotFoundException;
import com.jukusoft.mmo.engine.exception.ScreenNotFoundException;
import com.jukusoft.mmo.engine.graphics.screen.*;
import com.jukusoft.mmo.engine.service.ServiceManager;
import com.jukusoft.mmo.engine.service.impl.DefaultServiceManager;
import com.jukusoft.mmo.engine.service.impl.DummyService;
import com.jukusoft.mmo.utils.Platform;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DefaultScreenManagerTest extends GameUnitTest {

    @Test
    public void testConstructor () {
        ServiceManager serviceManager = new DefaultServiceManager();
        new DefaultScreenManager(serviceManager);
    }

    @Test (expected = NullPointerException.class)
    public void testAddNullScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen(null, new DummyScreen());
    }

    @Test (expected = NullPointerException.class)
    public void testAddNullScreen1 () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testAddEmptyScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("", new DummyScreen());
    }

    @Test (expected = IllegalStateException.class)
    public void testAddExistentScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.addScreen("dummy_screen", new DummyScreen());
    }

    @Test
    public void testAddScreen () {
        DefaultScreenManager manager = this.createDefaultScreenManager();

        assertEquals(0, manager.screens.size());
        assertEquals(0, manager.cachedScreenList.size());
        assertEquals(0, manager.activeScreens.size());
        assertEquals(0, manager.backActiveScreens.size());

        manager.addScreen("dummy_screen", new DummyScreen());

        assertEquals(1, manager.screens.size());
        assertEquals(1, manager.cachedScreenList.size());
        assertEquals(0, manager.activeScreens.size());
        assertEquals(0, manager.backActiveScreens.size());

        assertEquals(1, manager.listScreens().size());
        assertEquals(0, manager.listActiveScreens().size());

    }

    @Test
    public void testAddScreen1 () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new OtherDummyScreen());
    }

    @Test (expected = RequiredServiceNotFoundException.class)
    public void testAddScreen2 () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new AnotherDummyScreen());
    }

    @Test
    public void testAddScreen3 () {
        ServiceManager serviceManager = new DefaultServiceManager();
        serviceManager.addService(new DummyService(), DummyService.class);

        ScreenManager<IScreen> manager = new DefaultScreenManager(serviceManager);

        manager.addScreen("dummy_screen", new AnotherDummyScreen());
    }

    @Test (expected = IllegalStateException.class)
    public void testAddScreen4 () {
        ScreenManager<IScreen> manager = createDefaultScreenManager();

        manager.addScreen("dummy_screen", new Dummy1Screen());
    }

    @Test (expected = RequiredServiceNotFoundException.class)
    public void testAddScreen5 () {
        ServiceManager serviceManager = new DefaultServiceManager();
        serviceManager.addService(new DummyService(), DummyService.class);

        ScreenManager<IScreen> manager = new DefaultScreenManager(serviceManager);

        manager.addScreen("dummy_screen", new Dummy2Screen());
    }

    @Test
    public void testAddScreen6 () {
        ScreenManager<IScreen> manager = createDefaultScreenManager();

        manager.addScreen("dummy_screen", new DummyNullableInjectionScreen());
    }

    @Test
    public void testAddScreen7 () {
        ScreenManager<IScreen> manager = createDefaultScreenManager();

        Dummy3Screen screen = new Dummy3Screen();

        assertNull(screen.screenManager);

        manager.addScreen("dummy_screen", screen);

        assertNotNull(screen.screenManager);
    }

    @Test (expected = IllegalStateException.class)
    public void testAddScreen8 () {
        ScreenManager<IScreen> manager = createDefaultScreenManager();

        manager.addScreen("dummy_screen", new Dummy4Screen());
    }

    @Test (expected = NullPointerException.class)
    public void testRemoveNullScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.removeScreen(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testRemoveEmptyScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.removeScreen("");
    }

    @Test
    public void testRemoveScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();

        manager.removeScreen("not-existent-screen");

        assertNull(manager.getScreenByName("not-existent-screen"));

        manager.addScreen("dummy_screen", new DummyScreen());

        assertNotNull(manager.getScreenByName("dummy_screen"));

        manager.removeScreen("dummy_screen");

        assertEquals(true, manager.getScreenByName("dummy_screen") == null);
    }

    @Test (expected = NullPointerException.class)
    public void testPushNullScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.push(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testPushEmptyScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.push("");
    }

    @Test (expected = ScreenNotFoundException.class)
    public void testPushNotExistentScreen () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.push("not-existent-screen");
    }

    @Test
    public void testPush () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.push("dummy_screen");
    }

    @Test
    public void testPop () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.push("dummy_screen");

        assertNotNull(manager.pop());

        //pop again
        assertNull(manager.pop());

        assertEquals(1, manager.listScreens().size());
        assertEquals(0, manager.listActiveScreens().size());
    }

    @Test
    public void testPop1 () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.push("dummy_screen");

        assertNotNull(manager.pop());

        Platform.executeQueue();

        //pop again
        assertNull(manager.pop());

        assertEquals(1, manager.listScreens().size());
        assertEquals(0, manager.listActiveScreens().size());
    }

    @Test
    public void testLeaveAllAndEnter () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.leaveAllAndEnter("dummy_screen");
    }

    @Test
    public void testLeaveAllAndEnter1 () {
        IScreen screen1 = new DummyScreen();
        IScreen screen2 = new DummyScreen();

        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", screen1);
        manager.addScreen("screen2", screen2);

        assertEquals(false, manager.listActiveScreens().contains(screen1));
        assertEquals(false, manager.listActiveScreens().contains(screen2));

        manager.push("screen2");

        assertEquals(false, manager.listActiveScreens().contains(screen1));
        assertEquals(true, manager.listActiveScreens().contains(screen2));

        manager.leaveAllAndEnter("dummy_screen");

        assertEquals(true, manager.listActiveScreens().contains(screen1));
        assertEquals(false, manager.listActiveScreens().contains(screen2));
    }

    @Test
    public void testProcessInput () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.push("dummy_screen");

        manager.processInput();
    }

    @Test
    public void testProcessInput1 () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new IScreen() {
            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void onPause() {

            }

            @Override
            public void onResume() {

            }

            @Override
            public boolean processInput() {
                return true;
            }

            @Override
            public void update() {

            }

            @Override
            public void draw() {

            }
        });
        manager.push("dummy_screen");

        manager.processInput();
    }

    @Test
    public void testUpdate () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.push("dummy_screen");

        manager.update();
    }

    @Test
    public void testDraw () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.push("dummy_screen");

        manager.draw();
    }

    @Test
    public void testDispose () {
        ScreenManager<IScreen> manager = this.createScreenManager();
        manager.addScreen("dummy_screen", new DummyScreen());
        manager.push("dummy_screen");

        manager.dispose();
    }

    protected ScreenManager<IScreen> createScreenManager () {
        ServiceManager serviceManager = new DefaultServiceManager();
        return new DefaultScreenManager(serviceManager);
    }

    protected DefaultScreenManager createDefaultScreenManager () {
        ServiceManager serviceManager = new DefaultServiceManager();
        return new DefaultScreenManager(serviceManager);
    }

}
