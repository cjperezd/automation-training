package com.globant.automation.trainings.tests.junit;

import com.globant.automation.trainings.logging.Logging;
import com.globant.automation.trainings.utils.Environment;
import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * This class is supposed to be used from within a JUnit's runner.
 * Adds support for parallel test runs (default is double the number of available processors).
 * Can be overridden using "junit.parallel.threads" JVM variable
 *
 * @author Juan Krzemien
 */
public class ThreadPoolScheduler implements RunnerScheduler, Logging {

    private static final int TIMEOUT_MINUTES = 10;

    private final ExecutorService executor;

    public ThreadPoolScheduler() {
        this.executor = Executors.newWorkStealingPool(Environment.getNumberOfThreads());
    }

    @Override
    public void finished() {
        executor.shutdown();
        try {
            executor.awaitTermination(TIMEOUT_MINUTES, MINUTES);
        } catch (InterruptedException exc) {
            Thread.currentThread().interrupt();
        }
        getLogger().info(format("Executor state: %s", executor.toString()));
    }

    @Override
    public void schedule(Runnable childStatement) {
        executor.submit(childStatement);
    }

    static class TestThreadPool implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "TestPool-Thread-" + poolNumber.getAndIncrement());
        }
    }
}
