
import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
public class check {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = null;
        try {
            driver = new ChromeDriver();
            driver.manage().window().maximize();
            driver.get("https://elpais.com/");
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
                // Wait for the content to load
                try {
                    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
                    WebElement contentElement = driver.findElement(By.xpath("//h2[@class=\"a_st\"] "));
                    contents.add(contentElement.getText());
                } catch (Exception e) {
                    System.out.println("Content not found for article: " + titles.get(i));
                    contents.add("Content not available.");
                }
                System.out.println((i + 1) + ". Title: " + titles.get(i));
                System.out.println("   URL: " + url);
                System.out.println("   Content: " + contents.get(i));
                System.out.println();
            }
            List<String> translatedTitles = titles.stream()
                    .map(check::translateToEnglish)
                    .collect(Collectors.toList());

            for (int i = 0; i < titles.size(); i++) {
                System.out.println("Article " + (i + 1) + ":");
                System.out.println("Original Title: " + titles.get(i));
                System.out.println("Translated Title: " + translatedTitles.get(i));
                System.out.println("Content: " + contents.get(i));
                System.out.println("-----------------------------------");
            }

            analyzeHeaders(titles);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    private static String translateToEnglish(String text) {
        return "Translated: " + text;
    }

    private static void analyzeHeaders(List<String> titles) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String title : titles) {
            String[] words = title.toLowerCase().split("\\W+");
            for (String word : words) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        System.out.println("Repeated Words in Translated Headers:");
        wordCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 2)
                .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }


}








