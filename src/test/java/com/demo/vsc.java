package com.demo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class vsc {

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = null;

        try {
            // Setup WebDriver
            driver = new ChromeDriver();

            // Navigate to El País website and Opinion section
            driver.get("https://elpais.com/");
            driver.manage().window().maximize();
            driver.navigate().to("https://elpais.com/opinion/");

            // Fetch the first 5 articles

            List<WebElement> articles1 = driver.findElements(By.cssSelector("article h2 a"));
            List<String> titles = new ArrayList<>();
            List<String> contents = new ArrayList<>();

            // Fetch the title
            String title = driver.findElement(By.cssSelector("header h2 a ")).getText();
            titles.add(title);

            // Fetch the content
            String content = driver.findElement(By.cssSelector("article p")).getText();
            contents.add(content);

            // // Fetch the first 5 articles
            System.out.println("First 5 Opinion Articles:");

            for (int i = 0; i < 5; i++) {
            WebElement article = articles1.get(i);
            String title1 = article.getText();
            String url = article.getAttribute("href");
            System.out.println((i + 1) + ". " + title1);
            System.out.println(" URL: " + url);
            }

            // }

            // Translate article titles to English
            List<String> translatedTitles = titles.stream()
                    .map(vsc::translateToEnglish)
                    .collect(Collectors.toList());

            // Print the original and translated titles, content
            for (int i = 0; i < titles.size(); i++) {
                System.out.println("Article " + (i + 1) + ":");
                System.out.println("Original Title: " + titles.get(i));
                System.out.println("Translated Title: " + translatedTitles.get(i));
                System.out.println("Content: " + contents.get(i));
                System.out.println("-----------------------------------");
            }

            
            analyzeHeaders(translatedTitles);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private static String translateToEnglish(String text) {
        try {
            String apiKey = "YOUR_API_KEY"; // Replace with your Google Translate API key
            String urlStr = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            JsonObject request = new JsonObject();
            request.addProperty("q", text);
            request.addProperty("source", "es");
            request.addProperty("target", "en");
            request.addProperty("format", "text");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(request.toString().getBytes());
                os.flush();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.lines().collect(Collectors.joining());
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String translatedText = jsonResponse.getAsJsonObject("data").getAsJsonArray("translations").get(0)
                    .getAsJsonObject().get("translatedText").getAsString();

            conn.disconnect();
            return translatedText;

        } catch (Exception e) {
            System.out.println("Translation failed for text: " + text);
            return "Translation failed";
        }
    }

    private static void analyzeHeaders(List<String> translatedTitles) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String title : translatedTitles) {
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
