package oops;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class IphoneWebCrawler {
    // Regular expression pattern for URLs like https://en.wikipedia.org/wiki/IPhone_{model_no}
    private static final Pattern IPHONE_URL_PATTERN = Pattern.compile(
        "https://en\\.wikipedia\\.org/wiki/IPhone_\\d+"
    );

    public static void bfsCrawl(String startUrl, FileWriter writer) {
        // Queue for BFS
        Queue<String> q = new LinkedList<>();
        
        // Initially mark all URLs as not visited
        Set<String> visitedUrls = new HashSet<>();
        
        // Mark the source URL as visited and enqueue it
        visitedUrls.add(startUrl);
        q.add(startUrl);
        
        // Iterate over the queue
        while (!q.isEmpty()) {
            // Dequeue a URL and process it
            String url = q.poll();
            System.out.println("Crawling: " + url);
            
            try {
                // Fetch and parse the HTML content
                Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000);
                
                Document doc = connection.get();
                
                // Select infobox and main content
                Elements infobox = doc.select(".infobox");
                Elements mainContent = doc.select("#mw-content-text");

                // Write the URL and title
                writer.write("URL: " + url + "\n");
                writer.write("Title: " + doc.title() + "\n");

                // Parse and format the infobox data
                writer.write("\nInfobox:\n");
                if (!infobox.isEmpty()) {
                    for (Element row : infobox.select("tr")) {
                        Elements header = row.select("th");
                        Elements data = row.select("td");

                        if (!header.isEmpty() && !data.isEmpty()) {
                            String label = header.text().trim();
                            String value = data.text().trim();
                            writer.write(label + ": " + value + "\n");
                        } else if (!data.isEmpty()) {
                            writer.write(data.text().trim() + "\n");
                        }
                    }
                } else {
                    writer.write("Infobox: Not available\n");
                }

                // Parse and format main content by sections
                writer.write("\nMain Content:\n");
                for (Element element : mainContent.select("h2, h3, h4, p")) {
                    // Check the tag name and format accordingly
                    switch (element.tagName()) {
                        case "h2":
                        case "h3":
                        case "h4":
                            writer.write("\n" + element.text() + "\n");
                            writer.write("--------------------------------------------------\n");
                            break;
                        case "p":
                            writer.write(element.text() + "\n\n");
                            break;
                        default:
                            break;
                    }
                }

                writer.write("----------------------------------------\n\n");

                // Find and enqueue all adjacent (linked) URLs that match the pattern
                for (Element link : doc.select("a[href]")) {
                    String newUrl = link.absUrl("href");
                    if (!visitedUrls.contains(newUrl) && isIPhoneModelUrl(newUrl)) {
                        visitedUrls.add(newUrl);
                        q.add(newUrl);
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to retrieve content from: " + url);
                e.printStackTrace();
            }
        }
    }

    // Method to check if the URL matches the iPhone model pattern
    private static boolean isIPhoneModelUrl(String url) {
        Matcher matcher = IPHONE_URL_PATTERN.matcher(url);
        return matcher.matches();
    }

    public static void main(String[] args) {
        List<String> startUrls = Arrays.asList(
            "https://en.wikipedia.org/wiki/IPhone_16",
            "https://en.wikipedia.org/wiki/IPhone_15",
            "https://en.wikipedia.org/wiki/IPhone_14",
            "https://en.wikipedia.org/wiki/Apple_Watch",  // This URL should be skipped
            "https://en.wikipedia.org/wiki/IPhone_XR"    // This URL will be skipped (no digits)
        );

        String outputFile = "iphone_data_filtered.txt";
        
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String url : startUrls) {
                bfsCrawl(url, writer);
            }
            System.out.println("Crawling complete. Data saved to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
