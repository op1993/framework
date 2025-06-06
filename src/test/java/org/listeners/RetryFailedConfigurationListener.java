package org.listeners;


import org.configuration.ConfigurationLoader;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;

public class RetryFailedConfigurationListener implements IConfigurable {

    /**
     * Retry mechanism for @Before and @After methods
     */

    @Override
    public void run(IConfigureCallBack callBack, ITestResult testResult) {
        callBack.runConfigurationMethod(testResult);
        if (testResult.getThrowable() != null) {
            for (int i = 0; i < ConfigurationLoader.getAutomationConfiguration().getExecution().getRetry(); i++) {
                callBack.runConfigurationMethod(testResult);
                if (testResult.getThrowable() == null) {
                    break;
                }
            }
        }
    }

}