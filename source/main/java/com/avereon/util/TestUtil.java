package com.avereon.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for checking if any thread is running in the JUnit test framework.
 */
public class TestUtil {

    private static final Set<String> runners;

    private static Boolean test;

    static {
        runners = new HashSet<>();
        runners.add("org.junit.platform.launcher.core.DelegatingLauncher");
        runners.add("org.junit.platform.launcher.core.DefaultLauncher");
        runners.add("org.junit.runners.ParentRunner");
        runners.add("org.junit.runner.JUnitCore");
        runners.add("junit.framework.TestSuite");
        runners.add("org.apache.maven.surefire.junit4.JUnit4Provider");
        runners.add("org.gradle.api.internal.tasks.testing.worker.TestWorker");
    }

    /**
     * Check if any thread is running in the JUnit test framework. This is done by
     * searching the thread stack for the class {@code junit.framework.TestSuite}
     * If the TestSuite class is found the method will return true. The result is
     * cached for faster performance after the first call.
     *
     * @return If any thread is being run by JUnit
     */
    public static boolean isTest() {
        if (test != null) return test;

        boolean result = false;
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        for (Thread thread : stacks.keySet()) {
            StackTraceElement[] elements = stacks.get(thread);
            for (StackTraceElement element : elements) {
                if (runners.contains(element.getClassName())) {
                    result = true;
                    break;
                }
            }
        }

        //if( !result ) printClasses();

        return test = result;
    }

    /**
     * Prints the class names in the stack trace of the main thread with ID 1.
     * This method is intended for debugging purposes.
     */
    @SuppressWarnings("unused")
    private static void printClasses() {
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        for (Thread thread : stacks.keySet()) {
            if (thread.getId() == 1) {
                StackTraceElement[] elements = stacks.get(thread);
                for (StackTraceElement element : elements) {
                    System.out.println(element.getClassName());
                }
            }
        }
    }

}
