//package oops;
//
//import org.jsoup.Connection;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//
//public class WebCrawler {
//
//    private static final int MAX_DEPTH = 2;
//    private static final Set<String> visitedUrls = new HashSet<>(); 
//
//    public static void main(String[] args) {
//        String startUrl = "https://www.reddit.com/";
//        String outputFile = "output.txt";
//        
//        try (FileWriter writer = new FileWriter(outputFile)) {
//            crawl(startUrl, 0, writer);
//            System.out.println("Crawling complete. Data saved to " + outputFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void crawl(String url, int depth, FileWriter writer) {
//        if (depth > MAX_DEPTH || visitedUrls.contains(url)) {
//            return;
//        }
//
//        try {
//            visitedUrls.add(url);
//            System.out.println("Crawling: " + url);
//
//            // Fetch and parse the HTML content
//            Document doc = ((Connection) Jsoup.connect(url)).get();
//            String text = doc.text();
//
//            // Write the URL and content to the file
//            writer.write("URL: " + url + "\n" + text + "\n\n");
//
//            // Extract and visit each link found on the page
//            Elements links = doc.select("a[href]");
//            for (Element link : links) {
//                String nextUrl = link.absUrl("href");
//                if (!visitedUrls.contains(nextUrl)) {
//                    crawl(nextUrl, depth + 1, writer);
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Failed to retrieve content from: " + url);
//        }
//    }
//}