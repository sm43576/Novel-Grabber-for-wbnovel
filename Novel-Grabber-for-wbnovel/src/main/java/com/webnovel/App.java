package com.webnovel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import com.itextpdf.html2pdf.HtmlConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jsoup.Connection;
 
 public class App {

    private static JTextArea statusArea; // global
    private static String cookies; // global
    public static Map<String, Object> bookDict = new HashMap<>();
    public static final Map<String, String> headers = new HashMap<>();


    static {
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        headers.put("Accept-Encoding", "gzip, deflate, br, zstd");
        headers.put("Accept-Language", "en-US,en;q=0.9,en-NZ;q=0.8");
        headers.put("Cache-Control", "no-cache");
        headers.put("Origin", "https://www.webnovel.com");
    }

    public static void main(String[] args) {
        // Launch the UI
        SwingUtilities.invokeLater(() -> showInputUI());
        //runNovelProcessing("https://en.webnovel.com/book/32843307508686305");
    }

    private static void showInputUI() {
        JFrame frame = new JFrame("Novel URL Input");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250); // smaller overall frame
        frame.setLayout(new BorderLayout(10, 10));

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JPanel labelWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JLabel label = new JLabel("Website:");
        labelWrapper.add(label);

        JPanel textFieldWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(200, 25)); // smaller text field width
        textFieldWrapper.add(textField);

        inputPanel.add(labelWrapper, BorderLayout.WEST);
        inputPanel.add(textFieldWrapper, BorderLayout.CENTER);
        //////////////////////////////////////
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setEditable(false);   // already prevents editing
        statusArea.setHighlighter(null); // disables text selection highlighting
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setPreferredSize(new Dimension(300, 110)); // fixed size
        scrollPane.setMinimumSize(new Dimension(300, 110));
        

        // Center Panel to hold input + status
        JPanel centerPanel = new JPanel();
       // centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(inputPanel);
        centerPanel.add(scrollPane);

        // Submit Button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String novelLink = textField.getText().trim();
            clearStatus();
            if (novelLink.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a URL!");
            } else {
                new Thread(() -> {
                    runNovelProcessing(novelLink); 
                }).start();
            }
        });

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(submitButton, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void appendStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusArea.append(msg + "\n"));
        System.out.println(msg);
    }

    public static void clearStatus() {
        SwingUtilities.invokeLater(() -> statusArea.setText(""));
    }

    public static void runNovelProcessing(String novelLink2) {
        //String novelLink = "https://www.webnovel.com/book/33412005808141105";
        String novelLink = novelLink2;
        cookies = handleCookies();
        
        try {
            Connection.Response response = Jsoup.connect(novelLink)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0") // User-Agent from your browser
                .headers(headers)
                .header("Cookie", cookies)
                .timeout(10000) // Timeout to avoid long waits
                .execute(); 

            if (response.statusCode() == 200) {
                Document toc = response.parse();
                Elements chapterLinks = toc.select(".volume-item a:not(:has(svg))");
                
                if(chapterLinks.size()==0){
                    //System.out.println("triggered no size");
                    Elements chapterContainer = toc.select("ol[class^=ChapterList_chapterList]").select("a:not(:has(svg))");;
                    chapterLinks = chapterContainer;
                }
                List<String> chapterHtmls = new ArrayList<>();
                
                
                bookDict.put("Title", toc.title());
                bookDict.put("Synopsis", toc.selectFirst(".j_synopsis"));
                bookDict.put("Chapter Number",chapterLinks.size());
                appendStatus("Book has been gotten "+ bookDict.get("Title"));

                chapterHtmls = initTitlePage(chapterHtmls,chapterLinks);

            
                new File("novels").mkdirs(); // ensure /novel exists
                String title ="novels/" + bookDict.get("Title").toString().trim().replaceAll("[\\\\/:*?\"<>|]", "_") + ".pdf";
                FileOutputStream fos = new FileOutputStream(title);
                String fullHtml = String.join("<hr>", chapterHtmls);
                HtmlConverter.convertToPdf(fullHtml, fos);
                System.out.println("PDF created successfully!");
            }
        }
             catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error Details: " + e.getMessage());
            }
    }
    
    public static String handleCookies(){
        String cookies = "";
        try {
            Path cookiesPath = Path.of(System.getProperty("user.dir"), "cookies.txt");
            cookies = Files.readString(cookiesPath).trim();
        } catch (IOException e) {
            System.err.println("Failed to read cookies.txt: " + e.getMessage());
            e.printStackTrace();
        }
        
        StringBuilder cookieString = new StringBuilder();
        for (String line : cookies.split("\\R")) { // Split by any line break
            if (line.trim().isEmpty()) continue;
        
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = parts[1].trim();
        
                // Encode only if it contains illegal HTTP header chars
                if (value.contains("{") || value.contains("}") || value.contains("\"") || value.contains(",")) {
                    value = URLEncoder.encode(value, StandardCharsets.UTF_8);
                }
        
                if (cookieString.length() > 0) cookieString.append("; ");
                cookieString.append(name).append("=").append(value);
            }
        }
        
        String finalCookies = cookieString.toString();
        return finalCookies;        
    }

    public static List<String> initTitlePage(List<String> chapterHtmls, Elements chapterLinks){
        // Add title page//
        chapterHtmls.add("<div style='text-align:center; page-break-after: always;'>"
        + "<h1>" + bookDict.get("Title") + "</h1>"
        + "<p><em>" + bookDict.get("Synopsis") + "</em></p>"
        + "</div>");

        StringBuilder toc = new StringBuilder("<div style='page-break-after: always;'>");
        toc.append("<h2>Table of Contents</h2><ul>");
        for (Element chapter : chapterLinks) {
            String title = chapter.attr("title");
            if (!title.startsWith("Chapter")) {
                title = "Chapter: " + title;
            }
            toc.append("<li>").append(title).append("</li>");
        }
        toc.append("</ul></div>");
        chapterHtmls.add(toc.toString());

        try{
            for (Element chapter : chapterLinks) {
                    //System.out.println(chapter);
                    String url = chapter.attr("abs:href");
                    //System.out.println(url);
                    Document chapterDoc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36 Edg/139.0.0.0") // User-Agent from your browser
                        .headers(headers)
                        .header("Cookie", cookies)
                        .timeout(10000) 
                        .execute() 
                        .parse();

                    Element content = chapterDoc.selectFirst(".cha-words");
                    //System.out.println(content);
                    if (content != null) {
                        String title = chapter.attr("title");
                        
                        if (!title.startsWith("Chapter")) {
                            title = "Chapter: " + title;
                        }
                        if (!chapterHtmls.isEmpty()) {
                            chapterHtmls.add("<div style='page-break-before: always;'></div>");
                        }
                    
                        chapterHtmls.add("<h2>" + title + "</h2>" + content.html());
                        appendStatus(title);
                        System.out.println(title);
                    }
                }
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error Details: " + e.getMessage());
        }


        return chapterHtmls;
    
    }

}
 
