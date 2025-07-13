package com.example;

import browserstack.shaded.org.json.JSONObject;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ElPaisScraper {

//    driver = new RemoteWebDriver(...);

    @Before
    public void setUp() throws MalformedURLException {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", "Chrome");

        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("os", "Mac");
        bstackOptions.put("osVersion", "10");
        bstackOptions.put("local", "false");
        bstackOptions.put("seleniumVersion", "4.0.0");
        bstackOptions.put("sessionName", "ElPaisScraper Test");

        capabilities.setCapability("bstack:options", bstackOptions);

        WebDriver driver = new RemoteWebDriver(
                new URL("https://saurabhgaur_qsJRB9:zB7FRxDWivKsxnAvRKxB@hub-cloud.browserstack.com/wd/hub"),
                capabilities
        );

        final JavascriptExecutor jse = (JavascriptExecutor) driver;
        JSONObject executorObject = new JSONObject();
        JSONObject argumentsObject = new JSONObject();
        argumentsObject.put("name", "ElPaisScraper Test");
        executorObject.put("action", "ElPaisScraper Test");
        executorObject.put("arguments", argumentsObject);
        jse.executeScript(String.format("browserstack_executor: %s", executorObject));

    }

}

