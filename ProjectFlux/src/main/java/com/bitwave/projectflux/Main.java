//package com.bitwave.projectflux;
//
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.input.KeyCode;
//import javafx.stage.*;
//import org.fxmisc.richtext.CodeArea;
//import org.fxmisc.richtext.model.StyleSpans;
//import org.fxmisc.richtext.model.StyleSpansBuilder;
//import javafx.scene.text.Text;
//import javafx.scene.text.TextFlow;
//
//import java.io.*;
//import java.nio.file.*;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.regex.*;
//
//public class Main extends Application {
//
//    private TabPane tabPane;
//    private File currentFile;
//    private TreeView<String> fileTree;
//    private CodeArea currentEditor;
//
//    @Override
//    public void start(Stage primaryStage) {
//        BorderPane layout = new BorderPane();
//
//        // Create MenuBar and ToolBar
//        MenuBar menuBar = createMenuBar(primaryStage);
//        HBox toolBar = createToolBar();
//
//        // Create Project Explorer (File Tree)
//        fileTree = createFileTree(primaryStage);
//
//        // Editor setup (using CodeArea from RichTextFX)
//        tabPane = new TabPane();
//        layout.setCenter(tabPane);
//
//        // Put MenuBar and Toolbar in separate regions
//        VBox topLayout = new VBox(menuBar, toolBar);
//        layout.setTop(topLayout);
//
//        // Project Explorer on the left
//        layout.setLeft(fileTree);
//
//        // Status Bar at the bottom
//        Label statusLabel = new Label("Ready");
//        layout.setBottom(statusLabel);
//
//        // Create the scene and show the stage
//        Scene scene = new Scene(layout, 800, 600);
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("ProjectFlux IDE");
//        primaryStage.show();
//    }
//
//    private MenuBar createMenuBar(Stage primaryStage) {
//        Menu fileMenu = new Menu("File");
//        MenuItem newItem = new MenuItem("New");
//        MenuItem openItem = new MenuItem("Open");
//        MenuItem saveItem = new MenuItem("Save");
//        MenuItem saveAsItem = new MenuItem("Save As");
//        MenuItem closeItem = new MenuItem("Close");
//        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, closeItem);
//
//        // Action for New File
//        newItem.setOnAction(e -> createNewFile());
//
//        // Action for Open File
//        openItem.setOnAction(e -> openFile(primaryStage));
//
//        // Action for Save File
//        saveItem.setOnAction(e -> saveFile(false));
//
//        // Action for Save As File
//        saveAsItem.setOnAction(e -> saveFile(true));
//
//        // Action for Close File
//        closeItem.setOnAction(e -> closeFile());
//
//        MenuBar menuBar = new MenuBar();
//        menuBar.getMenus().addAll(fileMenu);
//        return menuBar;
//    }
//
//    private HBox createToolBar() {
//        HBox toolBar = new HBox(10);
//
//        Button undoButton = new Button("Undo");
//        Button redoButton = new Button("Redo");
//
//        // Set undo/redo actions on toolbar
//        undoButton.setOnAction(e -> undoAction());
//        redoButton.setOnAction(e -> redoAction());
//
//        toolBar.getChildren().addAll(undoButton, redoButton);
//        return toolBar;
//    }
//
//    private TreeView<String> createFileTree(Stage primaryStage) {
//        // Creating a simple file explorer using TreeView
//        TreeItem<String> rootItem = new TreeItem<>("Project");
//        rootItem.setExpanded(true);
//
//        // Initially loading the root directory (can be enhanced later to load real projects)
//        TreeItem<String> folder1 = new TreeItem<>("src");
//        folder1.getChildren().add(new TreeItem<>("Main.java"));
//        rootItem.getChildren().add(folder1);
//
//        fileTree = new TreeView<>(rootItem);
//        fileTree.setShowRoot(true);
//        fileTree.setOnMouseClicked(e -> {
//            if (e.getClickCount() == 2) {
//                TreeItem<String> item = fileTree.getSelectionModel().getSelectedItem();
//                if (item != null && item.isLeaf()) {
//                    openFileFromTree(item.getValue());
//                }
//            }
//        });
//
//        return fileTree;
//    }
//
//    private void createNewFile() {
//        Tab newTab = new Tab("Untitled");
//        currentEditor = new CodeArea();
//        currentEditor.setStyleSpans(0, computeHighlighting(currentEditor.getText()));
//        currentEditor.textProperty().addListener((observable, oldValue, newValue) -> {
//            currentEditor.setStyleSpans(0, computeHighlighting(newValue));
//        });
//        newTab.setContent(currentEditor);
//        tabPane.getTabs().add(newTab);
//        currentFile = null; // No file associated yet
//
//    }
//
//    private void openFile(Stage primaryStage) throws IOException {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.java"));
//        File selectedFile = fileChooser.showOpenDialog(primaryStage);
//
//        if (selectedFile != null) {
//            Tab openTab = new Tab(selectedFile.getName());
//            currentEditor = new CodeArea();
//            try {
//                currentEditor.replaceText(0, 0, new String(Files.readAllBytes(selectedFile.toPath())));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            openTab.setContent(currentEditor);
//            tabPane.getTabs().add(openTab);
//            currentFile = selectedFile;
//        }
//
//        currentEditor.replaceText(0, 0, new String(Files.readAllBytes(selectedFile.toPath())));
//        currentEditor.setStyleSpans(0, computeHighlighting(currentEditor.getText()));
//
//
//    }
//
//    private void saveFile(boolean saveAs) {
//        if (currentFile == null || saveAs) {
//            FileChooser fileChooser = new FileChooser();
//            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.java"));
//            File file = fileChooser.showSaveDialog(null);
//            if (file != null) {
//                currentFile = file;
//                try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
//                    writer.write(currentEditor.getText());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            // Save to the current file directly
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
//                writer.write(currentEditor.getText());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void closeFile() {
//        if (!tabPane.getTabs().isEmpty()) {
//            tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedIndex());
//            currentFile = null; // Reset the current file reference
//        }
//    }
//
//    private void openFileFromTree(String filePath) {
//        File file = new File(filePath);
//        if (file.exists()) {
//            Tab openTab = new Tab(file.getName());
//            currentEditor = new CodeArea();
//            try {
//                currentEditor.replaceText(0, 0, new String(Files.readAllBytes(file.toPath())));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            openTab.setContent(currentEditor);
//            tabPane.getTabs().add(openTab);
//            currentFile = file;
//        }
//    }
//
//    private void undoAction() {
//        if (currentEditor != null) {
//            currentEditor.undo();
//        }
//    }
//
//    private void redoAction() {
//        if (currentEditor != null) {
//            currentEditor.redo();
//        }
//    }
//
//    private static final String KEYWORDS = "\\b(?:if|else|for|while|int|double|public|private|class|static|void)\\b";
//    private StyleSpans<Collection<String>> computeHighlighting(String text) {
//        Matcher matcher = Pattern.compile(KEYWORDS).matcher(text);
//        StyleSpansBuilder<Collection<String>> styleSpansBuilder = new StyleSpansBuilder<>();
//
//        int lastKwEnd = 0;
//        while (matcher.find()) {
//            styleSpansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
//            styleSpansBuilder.add(Collections.singleton("keyword"), matcher.end() - matcher.start());
//            lastKwEnd = matcher.end();
//        }
//        styleSpansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
//        return styleSpansBuilder.create();
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}
