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

public class IphoneWebCrawler {
    private static final Set<String> visitedUrls = new HashSet<>();
    // Updated pattern to match only iPhone-related content
    private static final Pattern IPHONE_PATTERN = Pattern.compile(
        "(?i)(iphone(?!\\s+vs)|apple\\s+iphone)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Keywords that indicate the page is specifically about iPhones
    private static final Set<String> IPHONE_KEYWORDS = new HashSet<>(Arrays.asList(
        "iphone",
        "apple iphone"
    ));
    
    private static final int MAX_DEPTH = 1;

    public static void main(String[] args) {
        List<String> startUrls = Arrays.asList(
            "https://en.wikipedia.org/wiki/IPhone_16",
            "https://en.wikipedia.org/wiki/IPhone_15",
            "https://en.wikipedia.org/wiki/IPhone_14"
        );

        String outputFile = "iphone_data.txt";

        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String url : startUrls) {
                bfsCrawl(url, writer);
            }
            System.out.println("iPhone-specific crawling complete. Data saved to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void bfsCrawl(String startUrl, FileWriter writer) {
        Queue<CrawlNode> queue = new LinkedList<>();
        queue.add(new CrawlNode(startUrl, 0));

        while (!queue.isEmpty()) {
            CrawlNode node = queue.poll();
            String url = node.url;
            int depth = node.depth;

            if (visitedUrls.contains(url) || depth > MAX_DEPTH) {
                continue;
            }

            try {
                visitedUrls.add(url);
                System.out.println("Crawling: " + url + " (Depth: " + depth + ")");

                Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000);

                Document doc = connection.get();
                
                // Stricter check for iPhone-related content
                if (!isStrictlyIPhoneRelated(doc)) {
                    System.out.println("Skipping non-iPhone page: " + url);
                    continue;
                }

                writer.write("URL: " + url + "\n");
                writer.write("Title: " + doc.title() + "\n");

                // Process infobox with iPhone-specific data
                Elements infobox = doc.select(".infobox");
                writer.write("\nInfobox:\n");
                if (!infobox.isEmpty()) {
                    for (Element row : infobox.select("tr")) {
                        Elements header = row.select("th");
                        Elements data = row.select("td");

                        if (!header.isEmpty() && !data.isEmpty()) {
                            String label = header.text().trim();
                            String value = data.text().trim();
                            // Only include relevant iPhone specifications
                            if (isIPhoneSpecification(label)) {
                                writer.write(label + ": " + value + "\n");
                            }
                        }
                    }
                } else {
                    writer.write("Infobox: Not available\n");
                }

                // Process main content
                Elements mainContent = doc.select("#mw-content-text");
                writer.write("\nMain Content:\n");
                for (Element element : mainContent.select("h2, h3, h4, p")) {
                    String text = element.text();
                    // Only include sections that are iPhone-specific
                    if (isIPhoneContent(text)) {
                        switch (element.tagName()) {
                            case "h2":
                            case "h3":
                            case "h4":
                                writer.write("\n" + text + "\n");
                                writer.write("--------------------------------------------------\n");
                                break;
                            case "p":
                                writer.write(text + "\n\n");
                                break;
                        }
                    }
                }
                writer.write("----------------------------------------\n\n");

                // Add new links to queue if they're strictly iPhone-related
                for (Element link : doc.select("a[href]")) {
                    String newUrl = link.absUrl("href");
                    if (isValidWikipediaUrl(newUrl) && !visitedUrls.contains(newUrl)) {
                        if (isStrictlyIPhoneLink(link)) {
                            queue.add(new CrawlNode(newUrl, depth + 1));
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Failed to retrieve content from: " + url);
                e.printStackTrace();
            }
        }
    }

    private static boolean isStrictlyIPhoneRelated(Document doc) {
        // Check title must contain "iPhone"
        if (!doc.title().toLowerCase().contains("iphone")) {
            return false;
        }

        // Check first paragraph must be about iPhone
        Elements firstParagraph = doc.select("#mw-content-text p").first();
        if (firstParagraph == null || !firstParagraph.text().toLowerCase().contains("iphone")) {
            return false;
        }

        // Ensure it's not a comparison page with other devices
        String content = doc.text().toLowerCase();
        return !content.contains("vs ipad") && 
               !content.contains("versus ipad") && 
               !content.contains("compared to ipad");
    }

    private static boolean isStrictlyIPhoneLink(Element link) {
        String linkText = link.text().toLowerCase();
        String href = link.attr("href").toLowerCase();
        
        // Only match links that specifically mention iPhone
        return (linkText.contains("iphone") || href.contains("iphone")) &&
               !linkText.contains("ipad") && 
               !linkText.contains("ios") &&
               !href.contains("ipad") && 
               !href.contains("ios");
    }

    private static boolean isIPhoneSpecification(String label) {
        // List of relevant iPhone specification fields
        Set<String> relevantFields = new HashSet<>(Arrays.asList(
            "model", "generation", "release date", "price", "display",
            "camera", "processor", "memory", "storage", "battery",
            "dimensions", "weight", "operating system", "features"
        ));
        
        return relevantFields.contains(label.toLowerCase());
    }

    private static boolean isIPhoneContent(String text) {
        // Check if the content is specifically about iPhones
        String lowerText = text.toLowerCase();
        return IPHONE_PATTERN.matcher(lowerText).find() &&
               !lowerText.contains("ipad") &&
               !lowerText.contains("ipod") &&
               !lowerText.contains("ios version") &&
               !lowerText.contains("ios update");
    }

    private static boolean isValidWikipediaUrl(String url) {
        return url.startsWith("https://en.wikipedia.org/wiki/") &&
               !url.contains(":") &&
               !url.contains("#") &&
               !url.contains("Main_Page") &&
               !url.contains("Special:") &&
               !url.contains("File:") &&
               !url.contains("Category:") &&
               !url.contains("Template:") &&
               !url.contains("Wikipedia:") &&
               !url.contains("Portal:");
    }

    private static class CrawlNode {
        String url;
        int depth;

        CrawlNode(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}