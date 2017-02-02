package com.globant.automation.trainings.webdriver.tests;

import com.globant.automation.trainings.logging.Logging;
import com.globant.automation.trainings.runner.Parallelism;
import com.globant.automation.trainings.runner.ParametrizedParallelism;
import com.globant.automation.trainings.webdriver.browsers.Browser;
import com.globant.automation.trainings.webdriver.server.SeleniumServerStandAlone;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.globant.automation.trainings.webdriver.config.Framework.CONFIGURATION;
import static com.globant.automation.trainings.webdriver.tests.WebDriverContext.WEB_DRIVER_CONTEXT;
import static java.lang.Thread.currentThread;
import static org.junit.runner.Description.createTestDescription;

/**
 * JUnit runner for parallel WebDriver based tests execution.
 *
 * @author Juan Krzemien
 */

public class WebDriverRunner implements Logging {

    public static class ParametrizedParallel extends ParametrizedParallelism {
        public ParametrizedParallel(Class<?> clazz) throws Throwable {
            super(clazz);
            Runtime.getRuntime().addShutdownHook(new Thread(SeleniumServerStandAlone.INSTANCE::shutdown));
        }
    }

    public static class Parallel extends Parallelism {

        public Parallel(Class<?> clazz) throws Throwable {
            super(clazz);
            Runtime.getRuntime().addShutdownHook(new Thread(SeleniumServerStandAlone.INSTANCE::shutdown));
        }

        @Override
        public Description getDescription() {
            return Description.EMPTY;
        }

        @Override
        protected List<FrameworkMethod> getChildren() {
            final List<FrameworkMethod> methods = super.getChildren();
            final Set<Browser> browsers = CONFIGURATION.AvailableDrivers();
            final List<FrameworkMethod> expandedMethods = new ArrayList<>(methods.size() * browsers.size());
            methods.forEach(m -> browsers.forEach(b -> expandedMethods.add(new WebDriverFrameworkMethod(m, b))));
            return expandedMethods;
        }

//        @Override
//        protected Object createTest() throws Exception {
//            final Object test = super.createTest();
//            stream(test.getClass().getDeclaredFields()).filter(f -> Reflection.fieldIsSubClassOf(f, PageObject.class)).forEach(f -> injectFieldOwnInstance(f, test));
//            return test;
//        }

        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier) {

            final Browser browser = ((WebDriverFrameworkMethod) method).getBrowser();

            currentThread().setName(browser.name() + "-" + currentThread().getName());

            try {
                WEB_DRIVER_CONTEXT.set(new WebDriverContext.BrowserDriverPair(browser, WebDriverProvider.createDriverWith(browser)));
            } catch (MalformedURLException e) {
                notifier.fireTestFailure(new Failure(getDescription(), e));
                return;
            }

            final Description description = createTestDescription(getTestClass().getJavaClass(), browser.name() + "-" + method.getName());

            runLeaf(methodBlock(method), description, notifier);

            WEB_DRIVER_CONTEXT.remove();
        }

    }
}
