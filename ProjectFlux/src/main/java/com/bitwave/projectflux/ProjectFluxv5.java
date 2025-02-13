package com.bitwave.projectflux;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectFluxv5 extends Application {

    private CodeEditor codeEditor;
    private Terminal terminal;
    private File currentFile;

    @Override
    public void start(Stage primaryStage) {
        // Create the main layout
        BorderPane root = new BorderPane();

        // Code Editor
        codeEditor = new CodeEditor();

        // File Explorer
        FileExplorer fileExplorer = new FileExplorer("C:/"); // Change to your desired root path

        // Terminal/Console
        terminal = new Terminal();

        // Build and Run Panel
        BuildRunPanel buildRunPanel = new BuildRunPanel(codeEditor, terminal);

        // Menu Bar
        MenuBar menuBar = createMenuBar(primaryStage);

        // Add components to the layout
        root.setTop(menuBar);
        root.setLeft(fileExplorer);
        root.setCenter(codeEditor);
        root.setBottom(terminal);
        root.setRight(buildRunPanel);

        // Set up the scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Custom JavaFX IDE");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newMenuItem = new MenuItem("New");
        newMenuItem.setOnAction(e -> newFile());
        MenuItem openMenuItem = new MenuItem("Open");
        openMenuItem.setOnAction(e -> openFile(primaryStage));
        MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(e -> saveFile(primaryStage));
        MenuItem saveAsMenuItem = new MenuItem("Save As");
        saveAsMenuItem.setOnAction(e -> saveFileAs(primaryStage));
        fileMenu.getItems().addAll(newMenuItem, openMenuItem, saveMenuItem, saveAsMenuItem);

        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void newFile() {
        codeEditor.clear();
        currentFile = null;
    }

    private void openFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                codeEditor.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    codeEditor.appendText(line + "\n");
                }
                currentFile = file;
            } catch (IOException e) {
                terminal.appendText("Error opening file: " + e.getMessage() + "\n");
            }
        }
    }

    private void saveFile(Stage primaryStage) {
        if (currentFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                writer.write(codeEditor.getText());
            } catch (IOException e) {
                terminal.appendText("Error saving file: " + e.getMessage() + "\n");
            }
        } else {
            saveFileAs(primaryStage);
        }
    }

    private void saveFileAs(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(codeEditor.getText());
                currentFile = file;
            } catch (IOException e) {
                terminal.appendText("Error saving file: " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Code Editor with Syntax Highlighting
class CodeEditor extends CodeArea {

    private static final String[] KEYWORDS = new String[]{"public", "class", "void", "static", "int", "String", "return"};

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

// File Explorer
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

// Terminal/Console
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
            appendText("Error executing command: " + e.getMessage() + "\n");
        }
    }
}

// Build and Run Panel
class BuildRunPanel extends VBox {

    private final CodeEditor codeEditor;
    private final Terminal terminal;

    public BuildRunPanel(CodeEditor codeEditor, Terminal terminal) {
        this.codeEditor = codeEditor;
        this.terminal = terminal;

        Button buildButton = new Button("Build");
        buildButton.setOnAction(e -> buildProject());

        Button runButton = new Button("Run");
        runButton.setOnAction(e -> runProject());

        getChildren().addAll(buildButton, runButton);
    }

    private void buildProject() {
        // Save code to a temporary file and compile it
        String code = codeEditor.getText();
        // Implement file saving and compilation logic here
        terminal.executeCommand("javac YourFileName.java");
    }

    private void runProject() {
        // Execute the compiled Java program
        terminal.executeCommand("java YourFileName");
    }
}

// Note: Replace "YourFileName" with the actual name of the file you are compiling and running.
