package com.bitwave.projectflux;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;


public class syntax {
    private static final String[] KEYWORDS = {
            "public", "private", "static", "final", "class", "void", "if", "else", "while", "for", "return", "import"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String COMMENT_PATTERN = "//[^\n]*";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
    );

    public static List<StyleSpan<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        List<StyleSpan<String>> spans = new ArrayList<>();

        int lastEnd = 0;
        while (matcher.find()) {
            String styleClass = null;
            if (matcher.group("KEYWORD") != null) {
                styleClass = "keyword";
            } else if (matcher.group("COMMENT") != null) {
                styleClass = "comment";
            } else if (matcher.group("STRING") != null) {
                styleClass = "string";
            }

            if (styleClass != null) {
                spans.add(new StyleSpan<>("", matcher.start() - lastEnd));
                spans.add(new StyleSpan<>(styleClass, matcher.end() - lastEnd));
                lastEnd = matcher.end();
            }
        }

        spans.add(new StyleSpan<>("", text.length() - lastEnd));
        return spans;
    }
































}
