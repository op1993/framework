package org.listeners;

import org.annotations.NonRetryable;
import org.configuration.ConfigurationLoader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class RetryAnalyzer implements IRetryAnalyzer {

    private final ThreadLocal<Integer> retryCount = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_RETRY_COUNT = ConfigurationLoader.getAutomationConfiguration().getExecution().getRetry();

    @Override
    public boolean retry(ITestResult result) {
        if (!canBeRetried(result)) {
            retryCount.remove();
            return false;
        }

        int currentRetryCount = retryCount.get();
        if (currentRetryCount < MAX_RETRY_COUNT) {
            retryCount.set(currentRetryCount + 1);
            return true;
        }
        retryCount.remove();
        return false;
    }

    private boolean canBeRetried(ITestResult result) {
        ITestNGMethod testMethod = result.getMethod();
        Method method = testMethod.getConstructorOrMethod().getMethod();

        if (method.isAnnotationPresent(NonRetryable.class)) {
            return false;
        }

        Class<?> currentClass = testMethod.getRealClass();
        while (currentClass != null && currentClass != Object.class) {
            if (currentClass.isAnnotationPresent(NonRetryable.class)) {
                return false;
            }
            currentClass = currentClass.getSuperclass();
        }

        return true;
    }

}