package mb.rxui.property.javafx;

import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import mb.rxui.ThreadedTestHelper;

/**
 * A helper that should be used to run unit tests that must run on the JavaFx
 * Platform thread.
 */
public class JavaFxTestHelper
{
    private static volatile JavaFxTestHelper helper;
    private static CountDownLatch initLatch = new CountDownLatch(1);
    private final ThreadedTestHelper testHelper;
    
    private JavaFxTestHelper() {
        new Thread(JavaFxTestHelper::initJavaFx).start();
        testHelper = new ThreadedTestHelper(Platform::runLater);
        testHelper.awaitLatch(initLatch);
    }

    public static synchronized JavaFxTestHelper instance() {
        if (helper == null) {
            helper = new JavaFxTestHelper();
        }
        return helper;
    }
    
    private static void initJavaFx() {
        Platform.setImplicitExit(false);
        Application.launch(JavaFxTestApp.class);
    }
    
    /**
     * Runs the provided runnable on the JavaFx thread and blocks the current
     * thread until complete. This should not be used to run a test, use
     * {@link #runTest(Runnable)} for tests.
     * 
     * @param runnable
     *            some runnable to run on the JavaFx Platform thread.
     */
    public void invokeAndWait(Runnable runnable) {
        awaitFxInitialization();
        testHelper.invokeAndWait(runnable);
    }

    private void awaitFxInitialization() {
        testHelper.awaitLatch(initLatch);
    }
    
    public static class JavaFxTestApp extends Application 
    {
        @Override
        public void start(Stage paramStage) throws Exception 
        {
            initLatch.countDown();
        }
    }

    public void runTest(Runnable someTest) {
        testHelper.runTest(someTest);
    }

    public void runTestReThrowException(Runnable someTestThatShouldThrow) throws Throwable {
        testHelper.runTestReThrowException(someTestThatShouldThrow);
    }
}