package net.codjo.gui.toolkit;

import java.awt.Component;
import junit.extensions.jfcunit.JFCTestCase;

public abstract class AbstractJFCTestCase extends JFCTestCase {
    private static final int LOOP_DELAY = 10;
    private static final int LOOP_TIMEOUT = 500;


    public void setFocus(Component component) {
        long start = System.currentTimeMillis();
        do {
            component.requestFocusInWindow();
            flushAWT();
            sleep(LOOP_DELAY);
        }
        while (!component.hasFocus() && System.currentTimeMillis() - start < LOOP_TIMEOUT);
        assertTrue("Les tests considèrent que le champ a le focus", component.hasFocus());
    }


    public void transferFocus(Component component) {
        long start = System.currentTimeMillis();
        do {
            component.transferFocus();
            flushAWT();
            sleep(LOOP_DELAY);
        }
        while (component.hasFocus() && System.currentTimeMillis() - start < LOOP_TIMEOUT);
        assertTrue("Les tests considèrent que le champ n'a pas le focus", !component.hasFocus());
    }


    private static void sleep(int millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            }
            catch (InterruptedException e) {
                ; // ignore
            }
        }
    }
}
