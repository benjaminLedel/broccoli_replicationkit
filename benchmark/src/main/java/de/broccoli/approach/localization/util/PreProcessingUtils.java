package de.broccoli.approach.localization.util;

import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PreProcessingUtils {

    private static final String CAMEL_CASE_PATTERN = "([a-z0-9]+[A-Z0-9]+\\w+)+";
    private static final String PACKAGE_NAME_REGEX = "^(?:\\w+|\\w+\\.\\w+)+$";
    private static List<String> stopwords;

    public static PreProcessingUtils instance = new PreProcessingUtils();
    private Logger logger = LoggerFactory.getLogger(PreProcessingUtils.class.getName());

    public PreProcessingUtils()
    {
        try {
            stopwords = Files.readAllLines(Paths.get("src/main/resources/stopwords.txt"));
        } catch (IOException e) {
            logger.warn("Stopwords could not read from file");
            stopwords = new ArrayList<>();
        }
    }
    /**
     * Return as list of strings, that can be matched against other natural language text. Java, File Names will be destoryed! Use other methods for that
     * @param text
     * @return
     */
    public List<String> preProcessNaturalLanguage(String text)
    {
        String texts = mapLineBreaksToSpace(text).replaceAll("\\.", " ");
        return Arrays.stream(texts.split(" ")).map(String::toLowerCase)
                .map(filename -> filename.replaceAll("[^a-zA-Z0-9]", ""))
                .filter(filename -> filename.length() > 2)
                .filter(s -> !stopwords.contains(s))
                .filter(s -> !Arrays.asList(JavaLanguageConst.OTHER_STOPWORDS).contains(s))
                .distinct()
                .collect(Collectors.toCollection(ArrayList<String>::new));
    }

    public List<String> preProcessJavaLangaugeName(String text)
    {
        return Collections.singletonList(text);
//        text = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, text);
//        List<String> result = Arrays.asList(text.split("_"));
//        result = result.stream().
//                filter(s -> !Arrays.asList(JavaLanguageConst.JAVA_LANGUAGE_KEYS).contains(s)).
//                filter(s -> !Arrays.asList(JavaLanguageConst.OTHER_STOPWORDS).contains(s))
//                .collect(Collectors.toList());
//        return result;
    }

    public List<String> getStopwords() {
        return stopwords;
    }

    public List<String> findJavaMethods(String text) {
        List<String> result = new ArrayList<>(findCamelCaseWords(text));
        result.addAll(findWordsWithDots(text));
        return result.stream().distinct().collect(Collectors.toList());
    }

    public List<String> findFileNames(String text) {
        return Arrays.stream(mapLineBreaksToSpace(text).split(" ")).filter(s -> s.matches(".+\\..+") || s.matches("\\..+")).collect(Collectors.toList());
    }

    public List<String> findCamelCaseWords(String text) {
        if(text == null)
            return Collections.emptyList();
        return Arrays.stream(mapLineBreaksToSpace(text)
                .replaceAll("\\.", " ")
                .replaceAll("\\(", " ")
                .replaceAll("\\)", " ")
                .split(" "))
                .filter(s -> !s.contains(";"))
                .filter(s -> s.length() < 200) // otherwise infinited loop
                .filter(s -> WordUtils.uncapitalize(s).matches(CAMEL_CASE_PATTERN))
                .collect(Collectors.toList());
    }

    public List<String> findWordsWithDots(String text) {
        if(text == null)
            return Collections.emptyList();
        String[] lines = mapLineBreaksToSpace(text).split(" ");
        return Arrays.stream(lines)
                .map(s -> s.replaceAll("[^A-Za-z0-9.]", ""))
                .filter(s -> s.contains("."))
                // .filter(s -> s.matches(PACKAGE_NAME_REGEX))
                .collect(Collectors.toList());
    }

    private String mapLineBreaksToSpace(String text)
    {
        return text.replaceAll("\n", " ").replaceAll("\r\n", " ");
    }
}
