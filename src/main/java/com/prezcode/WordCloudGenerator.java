package com.prezcode;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import com.kennycason.kumo.WordCloud;

public class WordCloudGenerator {

    private static final String WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/Java_(programming_language)";
    private static final String TOKENIZER_MODEL_PATH = "en-token.bin"; // Ensure this file is in src/main/resources
    private static final String STOP_WORDS_PATH = "stopwords.txt"; // Optional: if you have a stop words file

    public static void main(String[] args) {
        try {
            // 1. Scrape text from Wikipedia
            String text = scrapeWikipediaArticle(WIKIPEDIA_URL);
            System.out.println("Scraped text length: " + text.length());

            // 2. Process the text
            List<String> processedWords = processText(text);
            System.out.println("Number of words after processing: " + processedWords.size());

            // 3. Generate word frequencies
            Map<String, Integer> wordFrequencies = getWordFrequencies(processedWords);

            // 4. Generate word cloud
            generateWordCloud(wordFrequencies, "wordcloud.png");
            System.out.println("Word cloud generated successfully as 'wordcloud.png'.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Scrapes the main content text from a Wikipedia article.
     *
     * @param url The URL of the Wikipedia article.
     * @return The extracted text content.
     * @throws IOException If an I/O error occurs.
     */
    private static String scrapeWikipediaArticle(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        // Select the content text within the article body
        // Wikipedia's main content is within the <div id="bodyContent">
        String content = doc.select("#bodyContent").text();
        return content;
    }

    /**
     * Processes the input text by tokenizing, converting to lowercase, and removing stop words.
     *
     * @param text The raw text to process.
     * @return A list of processed words.
     * @throws IOException If an I/O error occurs while loading stop words.
     */
    private static List<String> processText(String text) throws IOException {
        // Initialize tokenizer
        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

        // Tokenize the text
        String[] tokens = tokenizer.tokenize(text);

        // Load stop words
        Set<String> stopWords = loadStopWords();

        // Process tokens: lowercase and remove stop words and non-alphabetic tokens
        List<String> processed = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .filter(token -> token.matches("[a-zA-Z]+")) // Keep only alphabetic words
                .filter(token -> !stopWords.contains(token))
                .collect(Collectors.toList());

        return processed;
    }

    /**
     * Loads stop words from a predefined list.
     *
     * @return A set of stop words.
     * @throws IOException If an I/O error occurs while reading the stop words file.
     */
    private static Set<String> loadStopWords() throws IOException {
        // You can either hardcode stop words or load from a file
        // Here, we'll hardcode a simple list for demonstration

        String[] stopWordsArray = {
                "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
                "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being",
                "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't",
                "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during",
                "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't",
                "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
                "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
                "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it",
                "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
                "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
                "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
                "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so",
                "some", "such", "than", "that", "that's", "the", "their", "theirs", "them",
                "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll",
                "they're", "they've", "this", "those", "through", "to", "too", "under", "until",
                "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've",
                "were", "weren't", "what", "what's", "when", "when's", "where", "where's",
                "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't",
                "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your",
                "yours", "yourself", "yourselves"
        };

        return new HashSet<>(Arrays.asList(stopWordsArray));
    }

    /**
     * Calculates the frequency of each word in the list.
     *
     * @param words The list of words.
     * @return A map of word frequencies.
     */
    private static Map<String, Integer> getWordFrequencies(List<String> words) {
        Map<String, Integer> frequencies = new HashMap<>();

        for (String word : words) {
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
        }

        return frequencies;
    }

    /**
     * Generates a word cloud image from the word frequencies.
     *
     * @param wordFrequencies The map of word frequencies.
     * @param outputFileName  The name of the output image file.
     * @throws IOException If an I/O error occurs while writing the image.
     */
    private static void generateWordCloud(Map<String, Integer> wordFrequencies, String outputFileName) throws IOException {
        // Convert the word frequencies map to a list of WordFrequency objects
        List<WordFrequency> wordFrequencyList = wordFrequencies.entrySet().stream()
                .map(entry -> new WordFrequency(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // Configure the word cloud
        Dimension dimension = new Dimension(800, 600);
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(new ColorPalette(
                new Color(0x4055F1),
                new Color(0x408DF1),
                new Color(0x40AAF1),
                new Color(0x40C5F1),
                new Color(0x40D3F1),
                new Color(0xFFFFFF)
        ));
        wordCloud.setFontScalar(new LinearFontScalar(10, 40));
        wordCloud.build(wordFrequencyList);

        // Write the word cloud to a file
        wordCloud.writeToFile(outputFileName);
    }
}
