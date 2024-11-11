package oops;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileWriter;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class FinalWebCrawler {

    public static void main(String[] args) {
        String startUrl = "https://en.wikipedia.org/wiki/IPhone_16"; // starting or root node.
        String outputFile = "Output_DFS.txt"; 

        try (FileWriter writer = new FileWriter(outputFile)) {
            // Start DFS crawl from the given start URL.
            Set<String> visitedUrls = new HashSet<>(); // stores all the visited nodes to prevent re-visiting of nodes.
            
            dfsCrawl(startUrl, writer, visitedUrls);
            
            System.out.println("Process complete 😊");
           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dfsCrawl(String startUrl, FileWriter writer, Set<String> visitedUrls) {
        Stack<String> stack = new Stack<>();
        stack.push(startUrl);

        while (!stack.isEmpty()) {
            String url = stack.pop();

            if (!visitedUrls.contains(url)) {
                visitedUrls.add(url); 
                parseUrl(url, writer); 

                try {
                	// A HTTP request is sent to the specified URL which retrieves the entire HTML content of the webpage as a Document object.
                    Document doc = Jsoup.connect(url).get();
                    
                    // selects all the anchor tags from the Document that have a href attribute.
                    for (Element link : doc.select("a[href]")) {
                        String linkHref = link.attr("href"); // extracts the value of href from all the anchor tags.

                        // Only add links that match the pattern and haven't been visited.
                        // Restricting only URLs such as https://en.wikipedia.org/wiki/IPhone_{} to enter the stack
                        if (linkHref.startsWith("/wiki/IPhone_") && !visitedUrls.contains(linkHref)) {
                            String fullUrl = "https://en.wikipedia.org" + linkHref;
                            stack.push(fullUrl);  // Push discovered links onto the stack(links that are to be visited in the future.).
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error discovering links on URL: " + url);
                }
            }
        }
    }

    public static void parseUrl(String url, FileWriter writer) {
        System.out.println("Crawling: " + url);
        try {
            Document doc = Jsoup.connect(url).get();
            
            // selecting the first instance of the table tag with class name infobox in the Document.
            Element infobox = doc.selectFirst("table.infobox");

            if (infobox != null) {
                writer.write("Extracted Data from: " + url + "\n");
                writer.write("=================================\n");

                for (Element row : infobox.select("tr")) { // tr represents a row in the table stored within the infobox.
                    Element header = row.selectFirst("th"); // th represents a row header in the table stored within the infobox.
                    Element value = row.selectFirst("td"); // td represents a row data in the table stored within the infobox.

                    if (header != null && value != null) {
                        writer.write(header.text().trim() + ": " + value.text().trim() + "\n");
                    }
                }
                writer.write("\n");
            } else {
                writer.write("No table found on this page: " + url + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error fetching or parsing URL: " + url);
        }
    }
}