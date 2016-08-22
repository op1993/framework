import com.codeborne.selenide.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Created by Oleh on 23.08.2016.
 */
public class FirstTest {

    final String browserName = "chrome";
    final int timeOut = 5000;
    final String browserPropertyName = "webdriver.chrome.driver";
    //    final String browserPropertyPath = "./src/main/resources/chromedriver";
    final String browserPropertyPath = "./src/main/resources/chromedriver.exe";

    @Test
    public void test() throws InterruptedException {

        open("https://www.google.com.ua/");
        $(By.id("lst-ib")).setValue("test");
        Thread.sleep(5000);
    }

    @Before
    public void before(){
        Configuration.browser = browserName;
        Configuration.timeout = timeOut;
        System.setProperty(browserPropertyName, browserPropertyPath);
    }}
