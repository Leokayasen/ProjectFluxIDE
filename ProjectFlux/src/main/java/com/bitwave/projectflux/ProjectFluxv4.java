package com.bitwave.projectflux;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectFluxv4 extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        CodeEditor codeEditor = new CodeEditor();

        FileExplorer fileExplorer = new FileExplorer("C:/");

        Terminal terminal = new Terminal();

        BuildRunPanel buildRunPanel = new BuildRunPanel(codeEditor, terminal);

        root.setLeft(fileExplorer);
        root.setCenter(codeEditor);
        root.setBottom(terminal);
        root.setRight(buildRunPanel);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("ProjectFlux IDE v4");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class CodeEditor extends CodeArea {
    private static final String[] KEYWORDS = new String[]{
            "public", "class", "void", "static", "int", "String", "return"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final Pattern PATTERN = Pattern.compile("(?<KEYWORD>" + KEYWORD_PATTERN + ")");

    public CodeEditor() {
        setParagraphGraphicFactory(LineNumberFactory.get(this)); // Add line numbers
        textProperty().addListener((obs, oldText, newText) -> setStyleSpans(0, computeHighlighting(newText)));
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword" : null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

}

class FileExplorer extends TreeView<String> {
    public FileExplorer(String rootPath) {
        setRoot(createTree(new File(rootPath)));
    }

    private TreeItem<String> createTree(File file) {
        TreeItem<String> item = new TreeItem<>(file.getName());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    item.getChildren().add(createTree(child));
                }
            }
        }
        return item;
    }
}

class Terminal extends TextArea {
    public Terminal() {
        setEditable(false);
    }

    public void executeCommand(String command) {
        try {
            Process process = new ProcessBuilder(command.split(" ")).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                appendText(line + "\n");
            }
        } catch (IOException e) {
            appendText("Error while executing command: " + e.getMessage() + "\n");
        }
    }
}

class BuildRunPanel extends VBox {
    private final CodeEditor codeEditor;
    private final Terminal terminal;

    public BuildRunPanel(CodeEditor codeEditor, Terminal terminal) {
        this.codeEditor = codeEditor;
        this.terminal = terminal;

        Button buildButton = new Button("Build");
        buildButton.setOnAction(event -> buildProject());

        Button runButton = new Button("Run");
        runButton.setOnAction(event -> runProject());

        getChildren().addAll(buildButton, runButton);
    }

    private void buildProject() {
        String code = codeEditor.getText();
        terminal.executeCommand("javac YourFileName.java");
    }

    private void runProject() {
        terminal.executeCommand("java YourFileName");
    }
}


















