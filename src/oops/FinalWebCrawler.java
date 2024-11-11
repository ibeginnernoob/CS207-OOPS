package oops;

import org.jsoup.*;
import org.jsoup.nodes.*;
//import org.jsoup.select.Elements;
import java.io.*;
import java.util.*;

public class FinalWebCrawler {

    public static void main(String[] args) {
        String startUrl = "https://en.wikipedia.org/wiki/IPhone_16"; 
        String outputFile = "Output_DFS.txt"; 

        try (FileWriter writer = new FileWriter(outputFile)) {
            // Start DFS crawl from the given start URL
            Set<String> visitedUrls = new HashSet<>();
            dfsCrawl(startUrl, writer, visitedUrls);
            System.out.println("Process complete 😊");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dfsCrawl(String url, FileWriter writer, Set<String> visitedUrls) {
        if (!visitedUrls.contains(url)) {
            visitedUrls.add(url); 
            parseUrl(url, writer); 

            discoverLinks(url, writer, visitedUrls);
        }
    }

    public static void parseUrl(String url, FileWriter writer) {
        System.out.println("Crawling: " + url);
        try {
            Document doc = Jsoup.connect(url).get();
            Element infobox = doc.selectFirst("table.infobox");

            if (infobox != null) {
                writer.write("Extracted Data from: " + url + "\n");
                writer.write("=================================\n");

                for (Element row : infobox.select("tr")) {
                    Element header = row.selectFirst("th");
                    Element value = row.selectFirst("td");

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

    public static void discoverLinks(String url, FileWriter writer, Set<String> visitedUrls) {
        try {
            Document doc = Jsoup.connect(url).get();
            for (Element link : doc.select("a[href]")) {
                String linkHref = link.attr("href");

                if (linkHref.startsWith("/wiki/IPhone_") && !visitedUrls.contains(linkHref)) {
                    String fullUrl = "https://en.wikipedia.org" + linkHref;
                    dfsCrawl(fullUrl, writer, visitedUrls); 
                }
            }
        } catch (IOException e) {
            System.out.println("Error discovering links on URL: " + url);
        }
    }
}