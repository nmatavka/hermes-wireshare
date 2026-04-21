package org.limewire.ui.swing.util;

import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.limewire.util.ExceptionUtils;


public class SwingUtils {
    
    private SwingUtils() {}
    
    /**
     * Calls {@link SwingUtilities#invokeAndWait(Runnable)}
     * only if this is not currently on the Swing thread.
     */
    public static void invokeNowOrWaitWithInterrupted(Runnable runnable) throws InterruptedException {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (InvocationTargetException ite) {
                ExceptionUtils.rethrow(ite.getCause());
            }
        }
    }

    /**
     * Calls {@link SwingUtilities#invokeAndWait(Runnable)}
     * only if this is not currently on the Swing thread.
     */
    public static void invokeNowOrWait(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (InvocationTargetException ite) {
                ExceptionUtils.rethrow(ite.getCause());
            } catch(InterruptedException ignored) {
                throw new RuntimeException(ignored);
            }
        }
    }
    
    /**
     * Calls {@link SwingUtilities#invokeLater(Runnable)} only
     * if this is not currently on the Swing thread.
     */
    public static void invokeNowOrLater(Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            EventQueue.invokeLater(runnable);
        }
    }

    /**
     * Runs a blocking task off the event dispatch thread. If called from the
     * EDT, a standard AWT secondary loop keeps UI events pumping until the
     * background work completes.
     */
    public static <T> T runOffEventDispatchThread(final Callable<T> callable, final String threadName) {
        if (!EventQueue.isDispatchThread()) {
            return callUnchecked(callable);
        }

        final AtomicReference<T> result = new AtomicReference<T>();
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        final SecondaryLoop secondaryLoop = Toolkit.getDefaultToolkit()
                .getSystemEventQueue()
                .createSecondaryLoop();

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Thread worker = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.set(callUnchecked(callable));
                        } catch (Throwable t) {
                            failure.set(t);
                        } finally {
                            secondaryLoop.exit();
                        }
                    }
                }, threadName);
                worker.setDaemon(true);
                worker.start();
            }
        });

        secondaryLoop.enter();

        Throwable t = failure.get();
        if (t != null) {
            ExceptionUtils.rethrow(t);
        }

        return result.get();
    }

    private static <T> T callUnchecked(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable t) {
            ExceptionUtils.rethrow(t);
            return null;
        }
    }

}
