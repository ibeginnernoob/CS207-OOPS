package oops;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MultiplePageWebCrawler {
    private static final Set<String> visitedUrls = new HashSet<>();

    public static void main(String[] args) {
        List<String> startUrls = Arrays.asList(
            "https://en.wikipedia.org/wiki/IPhone_16",
            "https://en.wikipedia.org/wiki/IPhone_15",
            "https://en.wikipedia.org/wiki/IPhone_14"
        );

        String outputFile = "output.txt";

        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String url : startUrls) {
                bfsCrawl(url, writer);
            }
            System.out.println("BFS Crawling complete. Data saved to " + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void bfsCrawl(String startUrl, FileWriter writer) {
        Queue<String> queue = new LinkedList<>();
        queue.add(startUrl);

        while (!queue.isEmpty()) {
            String url = queue.poll();
            if (visitedUrls.contains(url)) {
                continue;
            }

            try {
                visitedUrls.add(url);
                System.out.println("Crawling: " + url);

                // Fetch and parse the HTML content
                Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000);

                Document doc = connection.get();

                // Write URL and title
                writer.write("URL: " + url + "\n");
                writer.write("Title: " + doc.title() + "\n");

                // Process infobox
                Elements infobox = doc.select(".infobox");
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

                // Process main content
                Elements mainContent = doc.select("#mw-content-text");
                writer.write("\nMain Content:\n");
                for (Element element : mainContent.select("h2, h3, h4, p")) {
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

                // Add new links to queue
                for (Element link : doc.select("a[href]")) {
                    String newUrl = link.absUrl("href");
                    if (!visitedUrls.contains(newUrl) && newUrl.contains("wikipedia.org")) {
                        queue.add(newUrl);
                    }
                }

            } catch (IOException e) {
                System.err.println("Failed to retrieve content from: " + url);
                e.printStackTrace();
            }
        }
    }
}
