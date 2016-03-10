/**
 * Copyright 2015 Mike Baum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package mb.rxui;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.runners.model.Statement;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public class ThreadedTestHelper {
    
    public static final ThreadedTestHelper EDT_TEST_HELPER = new ThreadedTestHelper(SwingUtilities::invokeLater);
    
    private final Executor testExecutor;
    
    public ThreadedTestHelper(Executor testExecutor) {
        this.testExecutor = testExecutor;
    }
    
    public void invokeAndWait(Runnable runnable) {
        CountDownLatch latch = new CountDownLatch(1);
        
        testExecutor.execute(runnable);
        testExecutor.execute(latch::countDown);
        
        awaitLatch(latch);
    }

    public static boolean awaitLatch(CountDownLatch latch) {
        try {
            return latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Runs the provided runnable test on this test helpers thread. Any
     * failures will be properly notified on the JUnit test runner thread.
     * 
     * @param someTest
     *            some {@link Runnable} test that should be performed on
     *            this test helpers thread.
     */
    public void runTest(Runnable someTest) {
        AtomicReference<Throwable> error = runTestAndCaptureExceptionIfAny(someTest);
        
        if (error.get() != null)
            Assert.fail(error.get().getMessage());
    }

    private AtomicReference<Throwable> runTestAndCaptureExceptionIfAny(Runnable someTest) {
        AtomicReference<Throwable> error = new AtomicReference<>();
        
        Runnable errorCapturingRunnable = () -> {
            try {
                someTest.run();
            } catch (Throwable throwable) {
                error.set(throwable);
            }
        };
        
        invokeAndWait(errorCapturingRunnable);
        
        return error;
    }

    /**
     * Runs the provided test on this test helpers thread and re-throws any
     * exceptions that were thrown.
     * 
     * @param someTestThatShouldThrow
     *            some test to execute on this test helpers thread.
     * @throws Throwable
     *             some throwable if the test fails exceptionally.
     */
    public void runTestReThrowException(Runnable someTestThatShouldThrow) throws Throwable {
        AtomicReference<Throwable> error = runTestAndCaptureExceptionIfAny(someTestThatShouldThrow);
        
        if (error.get() != null) {
            if (error.get() instanceof AssertionError) {
                throw error.get().getCause();
            } else if (error.get() instanceof TestRunException) {
                throw error.get().getCause();
            } else {
                throw error.get();
            }
        }
    }
    
    public Statement wrapStatementToRunOnEDT(Statement statement) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                runTestReThrowException(createRunnable(statement));
            }
        };
    }
    
    private Runnable createRunnable(Statement statement) {
        return () -> {
            try {
                statement.evaluate();
            } catch (Throwable throwable) {
                throw new TestRunException(throwable);
            }
        };
    }
    
    /**
     * polls (at the provided interval) the provided getter until the provided
     * predicate is satisfied for the given timeout.
     * 
     * @param getter
     *            some supplier to poll
     * @param predicate
     *            some condition to wait for
     * @param timeout
     *            some timeout before giving up
     * @param pollInterval
     *            the time interval to poll at
     */
    public static <T> void waitForConditionOnEDT(Supplier<String> getter, 
                                                 Predicate<String> predicate,
                                                 Duration timeout,
                                                 Duration pollInterval) {
        Observable.interval(pollInterval.toMillis(), MILLISECONDS, Schedulers.from(SwingUtilities::invokeLater))
                  .map(tick -> getter.get())
                  .timeout(timeout.toMillis(), MILLISECONDS)
                  .takeUntil(predicate::test)
                  .toBlocking()
                  .first();
    }

    public static class TestRunException extends RuntimeException {
        public TestRunException(Throwable throwable) {
            super(throwable);
        }
    }

    public static <T> T createOnEDT(Supplier<T> factory) throws Exception {
        return callOnScheduler(factory::get, Schedulers.from(SwingUtilities::invokeLater));
    }
    
    public static <T> T callOnIoThread(Callable<T> factory) {
        return callOnScheduler(factory, Schedulers.io());
    }

    public static <T> T callOnScheduler(Callable<T> factory, Scheduler scheduler) {
        return Observable.fromCallable(() -> factory.call())
                         .subscribeOn(scheduler)
                         .toBlocking()
                         .first();
    }
    
    public static void doOnIoThread(Runnable runnable) {
        Observable.fromCallable(asCallable(runnable))
                  .subscribeOn(Schedulers.io())
                  .toBlocking()
                  .first();
    }

    public static Callable<Void> asCallable(Runnable runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }
}