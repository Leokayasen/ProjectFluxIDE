package com.bitwave.projectflux;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

public class ProjectFluxv3 extends Application {
    private TreeView<File> projectExplorer;
    private StyleClassedTextArea codeEditor;
    private File currentFile;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        //Top Menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem openProject = new MenuItem("Open Project");
        MenuItem saveFile = new MenuItem("Save");
        fileMenu.getItems().addAll(openProject, saveFile);
        menuBar.getMenus().addAll(fileMenu);

        //Project Explorer (left panel)
        projectExplorer = new TreeView<>();
        projectExplorer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                System.out.println("Opening" + currentFile);
                TreeItem<File> selectedItem = projectExplorer.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue().isFile()) {
                    openFile(selectedItem.getValue());
                }
            }
        });

        //Code Editor (center panel)
        codeEditor = new StyleClassedTextArea();
        codeEditor.setWrapText(true);
        applySyntaxHighlighting();

        //Layout
        root.setTop(menuBar);
        root.setLeft(projectExplorer);
        root.setCenter(codeEditor);
        root.setBottom(consoleOutput);

        //Menu Actions
        openProject.setOnAction(event -> openProjectFolder(primaryStage));
        saveFile.setOnAction(event -> saveCurrentFile());

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Project Flux IDE");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void openProjectFolder(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Folder");
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null && selectedDirectory.isDirectory()) {
            TreeItem<File> rootItem = new TreeItem<>(selectedDirectory);
            buildFileTree(rootItem, selectedDirectory);
            projectExplorer.setRoot(rootItem);

            projectExplorer.setCellFactory(tv -> new TreeCell<>() {
                @Override
                protected void updateItem(File file, boolean empty) {
                    super.updateItem(file, empty);
                    setText((file == null || empty) ? "" : file.getName());
                }
            });
        }
    }

    private void buildFileTree(TreeItem<File> parent, File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                TreeItem<File> item = new TreeItem<>(file);
                parent.getChildren().add(item);
                if (file.isDirectory()) {
                    buildFileTree(item, file);
                }
            }
        }
    }

    private TabPane tabPane = new TabPane();

    private void openFile(File file) {
        try {
            String content = Files.readString(file.toPath());

            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText().equals(file.getName())) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }

            StyleClassedTextArea editor = new StyleClassedTextArea();
            editor.replaceText(content);
            editor.setWrapText(true);

            Tab tab = new Tab(file.getName(), editor);
            tab.setOnClosed(e -> closeTab(tab, file));
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeTab(Tab tab, File file) {

    }

    private void saveCurrentFile() {
        if (currentFile != null) {
            try {
                Files.writeString(currentFile.toPath(), codeEditor.getText(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applySyntaxHighlighting() {
        codeEditor.textProperty().addListener((obs, oldText, newText) -> {
            codeEditor.clearStyle(0, newText.length());

            Pattern keywordPattern = Pattern.compile("\\b(class|public|static|void|int|new|if|else|return|double|float|boolean|char|String|long|short|byte)\\b");
            Pattern commentPattern = Pattern.compile("//.*|/\\*.*?\\*/", Pattern.DOTALL);
            Pattern stringPattern = Pattern.compile("\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"");
            Pattern numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
            Pattern operatorPattern = Pattern.compile("[=+\\-*/&|!><]=?");
            Pattern annotationPattern = Pattern.compile("@\\w+");

            keywordPattern.matcher(newText).results().forEach(match -> codeEditor.setStyleClass(match.start(), match.end(), "keyword"));
            commentPattern.matcher(newText).results().forEach(match -> codeEditor.setStyleClass(match.start(), match.end(), "comment"));
            stringPattern.matcher(newText).results().forEach(match -> codeEditor.setStyleClass(match.start(), match.end(), "string"));
            numberPattern.matcher(newText).results().forEach(match -> codeEditor.setStyleClass(match.start(), match.end(), "number"));
            operatorPattern.matcher(newText).results().forEach(match -> codeEditor.setStyleClass(match.start(), match.end(), "operator"));
            annotationPattern.matcher(newText).results().forEach(match -> codeEditor.setStyleClass(match.start(), match.end(), "annotation"));
        });
    }

    private void dragdrop(Scene scene) {
        scene.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
    }

    private TextArea consoleOutput = new TextArea();

    private void setConsoleOutput() {
        PrintStream consoleStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                consoleOutput.appendText(String.valueOf((char) b));
            }
        });
        System.setOut(consoleStream);
        System.setErr(consoleStream);
    }

    private void autoSave() {
        Timeline autosaveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> saveCurrentFile()));
        autosaveTimer.setCycleCount(Timeline.INDEFINITE);
        autosaveTimer.play();
    }

    private void trackChanges(Tab tab, StyleClassedTextArea editor) {
        editor.textProperty().addListener((obs, oldText, newText) -> {
            if (!tab.getText().endsWith("*")) {
                tab.setText(tab.getText() + "*");
            }
        });
    }

    private void autoComplete(StyleClassedTextArea editor) {
        ContextMenu autocompleteMenu = new ContextMenu();

        editor.textProperty().addListener((obs, oldText, newText) -> {
            autocompleteMenu.getItems().clear();

            if (newText.endsWith(".")) {
                MenuItem suggestion = new MenuItem("System.out.println()");
                suggestion.setOnAction(event -> {
                    editor.insertText(editor.getCaretPosition(), "System.out.println()");
                });
                autocompleteMenu.getItems().add(suggestion);
                autocompleteMenu.show(editor, Side.BOTTOM, 0, 0);
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }


}
