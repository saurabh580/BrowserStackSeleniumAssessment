import io.github.bonigarcia.wdm.WebDriverManager;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Optional;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Test1 {

    WebDriver driver;
    String browserName;

    @Test
    public void runTest() {
        boolean testPassed = true;

        try {
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
            driver.get("https://elpais.com/");
            String lang = driver.findElement(By.tagName("html")).getAttribute("lang");
            if (!lang.contains("es")) throw new Exception("Site is not in Spanish");

            driver.get("https://elpais.com/opinion/");
            List<WebElement> articles = driver.findElements(By.cssSelector("article h2 a"));
            List<String> titles = new ArrayList<>();
            List<String> links = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                titles.add(articles.get(i).getText());
                links.add(articles.get(i).getAttribute("href"));
            }
            List<String> contents = new ArrayList<>();
            for (int i = 0; i < links.size(); i++) {
                String url = links.get(i);
                driver.navigate().to(url);
                try {
                    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
                    WebElement contentElement = driver.findElement(By.xpath("//h2[@class=\"a_st\"]"));
                    contents.add(contentElement.getText());
                } catch (Exception e) {
                    System.out.println("Content not found for article: " + titles.get(i));
                    contents.add("Content not available.");
                }
            }

            List<String> translatedTitles = titles.stream()
                    .map(Test1::translateToEnglish)
                    .collect(Collectors.toList());

            for (int i = 0; i < titles.size(); i++) {
                System.out.println("Article " + (i + 1));
                System.out.println("Original: " + titles.get(i));
                System.out.println("Translated: " + translatedTitles.get(i));
                System.out.println("Content: " + contents.get(i));
                System.out.println("-----------------------------------");
            }

            analyzeHeaders(translatedTitles);

        } catch (Exception e) {
            testPassed = false;
            System.out.println("Test failed: " + e.getMessage());
        } finally {
            updateBrowserStackSession(testPassed);
            driver.quit();
        }
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    private static void downloadImage(String urlStr, String fileName) {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Failed to download image: " + e.getMessage());
        }
    }

    private static String translateToEnglish(String text) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("from", "es");
        requestBody.put("to", "en");
        requestBody.put("text", text);
        HttpResponse<JsonNode> response = Unirest.post("https://google-translate113.p.rapidapi.com/api/v1/translator/text")
                .header("content-type", "application/json")
                .header("X-RapidAPI-Key", "8ce37df254mshe207247dcad33cep1338aajsnf3424557b48f")
                .header("X-RapidAPI-Host", "google-translate113.p.rapidapi.com")
                .body(requestBody.toString())
                .asJson();

        if (response.getBody() == null) return "Translation failed";
        JSONObject json = response.getBody().getObject();
        return json.has("trans") ? json.getString("trans") : "Translation failed";
    }

    private static void analyzeHeaders(List<String> titles) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String title : titles) {
            for (String word : title.toLowerCase().split("\\W+")) {
                if (!word.trim().isEmpty()) wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        System.out.println("Repeated Words in Translated Headers:");
        wordCount.entrySet().stream()
                .filter(e -> e.getValue() > 2)
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
    }

    private void updateBrowserStackSession(boolean passed) {

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        JSONObject nameArgs = new JSONObject();
        nameArgs.put("name", "ElPais Test");

        JSONObject nameCommand = new JSONObject();
        nameCommand.put("action", "setSessionName");
        nameCommand.put("arguments", nameArgs);

        jse.executeScript("browserstack_executor: " + nameCommand.toString());



        JSONObject statusArgs = new JSONObject();
        statusArgs.put("status", "passed"); // or "failed"
        statusArgs.put("reason", "Scraping completed");

        JSONObject statusCommand = new JSONObject();
        statusCommand.put("action", "setSessionStatus");
        statusCommand.put("arguments", statusArgs);

        jse.executeScript("browserstack_executor: " + statusCommand.toString());

    }
    @Parameters("browser")
    @BeforeMethod
    public void setUp(@Optional("SafariTest") String browser) throws MalformedURLException {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        Map<String, Object> bstackOptions = new HashMap<>();

        switch (browser.toLowerCase()) {
            case "iphone 14":
                capabilities.setCapability("browserName", "Safari");
                bstackOptions.put("deviceName", "iPhone 14");
                bstackOptions.put("realMobile", "true");
                bstackOptions.put("osVersion", "16");
                break;

            case "safari":
                capabilities.setCapability("browserName", "Safari");
                bstackOptions.put("os", "OS X");
                bstackOptions.put("osVersion", "Monterey");
                bstackOptions.put("seleniumVersion", "4.0.0");
                break;

            case "chrome":
            default:
                capabilities.setCapability("browserName", "Chrome");
                bstackOptions.put("os", "Windows");
                bstackOptions.put("osVersion", "10");
                break;
        }

        bstackOptions.put("sessionName", "ElPais " + browser);
        bstackOptions.put("local", "true");
        bstackOptions.put("localIdentifier", capabilities);
        bstackOptions.put("seleniumVersion", "4.0.0");

        capabilities.setCapability("bstack:options", bstackOptions);

        String username = "saurabhgaur_qsJRB9";
        String accessKey = "zB7FRxDWivKsxnAvRKxB";

        driver = new RemoteWebDriver(new URL("https://" + username + ":" + accessKey + "@hub-cloud.browserstack.com/wd/hub"), capabilities);
    }
}