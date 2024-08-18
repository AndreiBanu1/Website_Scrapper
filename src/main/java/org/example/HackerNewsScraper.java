package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HackerNewsScraper {

    public static void main(String[] args) {

        WebDriverManager.chromedriver().setup();
        String chromiumPath = "C:\\Users\\andre\\chrome\\win64-127.0.6533.119\\chrome-win64\\chrome.exe";

        ChromeOptions options = new ChromeOptions();
        options.setBinary(chromiumPath);
        options.addArguments("disable-infobars", "disable-notifications", "disable-popup-blocking", "start-maximized", "incognito", "disable-extensions", "disable-plugins");

        WebDriver driver = new ChromeDriver(options);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            List<WebElement> allRows = new ArrayList<>();
            int totalRowsToCollect = 100;

            driver.get("https://news.ycombinator.com/newest");

            while (allRows.size() < totalRowsToCollect) {
                // Collect rows currently displayed on the page
                List<WebElement> rows = driver.findElements(By.cssSelector("tr.athing"));

                for (WebElement row : rows) {
                    if (allRows.size() >= totalRowsToCollect) break;

                    // Only add unique rows
                    if (!allRows.contains(row)) {
                        allRows.add(row);
                    }
                }

                // Process the current batch of rows
                processRows(driver, allRows);

                // Click the "More" button to load more rows if needed
                if (allRows.size() < totalRowsToCollect) {
                    List<WebElement> moreButtons = driver.findElements(By.cssSelector("a.morelink"));
                    if (moreButtons.size() > 0) {
                        WebElement moreButton = moreButtons.get(0);
                        wait.until(ExpectedConditions.elementToBeClickable(moreButton)).click();

                        // Wait for new rows to load
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("tr.athing")));
                    } else {
                        break; // No more rows to load
                    }
                }
            }

            System.out.println("Total rows collected: " + allRows.size());
        } finally {
            driver.quit();
        }
    }

    private static void processRows(WebDriver driver, List<WebElement> rows) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String previousTitleAttr = null;
        int assertionCount = 0;
        int failedAssertions = 0;

        for (int i = 0; i < rows.size(); i++) {
            try {
                // Re-query the row element
                WebElement refreshedRow = driver.findElement(By.xpath("//tr[@class='athing'][position()=" + (i + 1) + "]"));

                WebElement secondTd = refreshedRow.findElement(By.xpath("./following-sibling::tr[1]/td[2]"));
                WebElement sublineSpan = secondTd.findElement(By.className("subline"));
                WebElement ageSpan = sublineSpan.findElement(By.className("age"));
                String currentTitleAttr = ageSpan.getAttribute("title");

                if (currentTitleAttr != null) {
                    // Parse the current date-time
                    LocalDateTime currentDateTime = LocalDateTime.parse(currentTitleAttr, formatter);

                    if (previousTitleAttr != null) {
                        // Parse the previous date-time
                        LocalDateTime previousDateTime = LocalDateTime.parse(previousTitleAttr, formatter);

                        // Assert that the current date-time is newer than the previous one
                        if (currentDateTime.isBefore(previousDateTime)) {
                            assertionCount++;
                        } else {
                            failedAssertions++;
                        }
                    }

                    previousTitleAttr = currentTitleAttr;
                }
                 } catch (Exception e) {
            }
        }

        // Final report
        System.out.println("Total assertions made: " + assertionCount);
        System.out.println("Total failed assertions: " + failedAssertions);
        if (failedAssertions == 0) {
            System.out.println("All assertions passed.");
        } else {
            System.out.println("Some assertions failed.");
        }
    }
}
