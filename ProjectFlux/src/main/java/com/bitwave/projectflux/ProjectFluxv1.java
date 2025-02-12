package com.bitwave.projectflux;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.*;
import java.io.IOException;
import java.nio.file.*;

public class ProjectFluxv1 extends Application {

    private TabPane tabPane;
    private TreeView<String> projectExplorer;
    private Path currentProjectPath;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ProjectFlux IDE - v0.2");

        // Layout Setup
        BorderPane root = new BorderPane();
        tabPane = new TabPane();
        projectExplorer = new TreeView<>();
        setupProjectExplorer();


        VBox explorerBox = new VBox(new Label("Project Explorer"), projectExplorer);
        root.setLeft(explorerBox);
        root.setCenter(tabPane);
        root.setTop(createMenuBar(primaryStage));


        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newProject = new MenuItem("New Project");
        MenuItem openProject = new MenuItem("Open Project");
        MenuItem saveProject = new MenuItem("Save Project");

        MenuItem newFile = new MenuItem("New File");
        MenuItem openFile = new MenuItem("Open File");
        MenuItem saveFile = new MenuItem("Save File");
        MenuItem saveAsFile = new MenuItem("Save As");

        MenuItem exit = new MenuItem("Exit");

        newProject.setOnAction(event -> createNewProject(primaryStage));
        openProject.setOnAction(actionEvent -> openProject(primaryStage));
        saveProject.setOnAction(actionEvent -> saveProject());

        newFile.setOnAction(e -> createNewTab());
        openFile.setOnAction(e -> openFile(primaryStage));
        saveFile.setOnAction(e -> saveFile());
        saveAsFile.setOnAction(e -> saveAsFile(primaryStage));

        exit.setOnAction(e -> primaryStage.close());

        fileMenu.getItems().addAll(newProject, openProject, saveProject, new SeparatorMenuItem(), newFile, openFile, saveFile, saveAsFile, new SeparatorMenuItem(), exit);

        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void createNewProject(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select New Project Directory");
        File projectDir = directoryChooser.showDialog(primaryStage);

        if (projectDir != null) {
            currentProjectPath = projectDir.toPath();
            projectExplorer.setRoot(createDirectoryTree(currentProjectPath));
            createNewTab();
        }
    }

    private void setupProjectExplorer() {
        projectExplorer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                System.out.println("Double click detected");
                TreeItem<String> selectedItem = projectExplorer.getSelectionModel().getSelectedItem();

                if (selectedItem != null) {
                    System.out.println("Selected Item: "+selectedItem.getValue());
                    Path filePath = resolvePathFromTreeItem(selectedItem);

                    if (filePath != null && Files.isRegularFile(filePath)) {
                        System.out.println("File Path: "+filePath);
                        openFileInEditor(filePath);
                    } else {
                        System.out.println("Not a file or file path could not be resolved");
                    }
                } else {
                    System.out.println("No file selected");
                }
            }
        });
    }

    private Path resolvePathFromTreeItem(TreeItem<String> item) {
        if (currentProjectPath == null) {
            System.out.println("Current project path is null");
            return null;
        }

        StringBuilder relativePath = new StringBuilder();
        TreeItem<String> current = item;

        while (current != null && current.getParent() != null) {
            relativePath.insert(0, current.getValue()).insert(0, File.separator);
            current = current.getParent();
        }

        Path resolvedPath = currentProjectPath.resolve(relativePath.toString());
        System.out.println("Resolved Path: "+resolvedPath);
        return resolvedPath;
    }

    private void openFileInEditor(Path filePath) {
        try {
            String content = Files.readString(filePath);
            System.out.println("File content loaded: "+filePath);
            Tab tab = new Tab(filePath.getFileName().toString());
            TextArea textArea = new TextArea(content);

            tab.setContent(textArea);
            tab.setUserData(filePath);
            tab.setOnCloseRequest(e -> {
                if (!confirmClose(tab)) {
                    e.consume();
                }
            });

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            showAlert("Error", "Failed to open file: "+filePath, Alert.AlertType.ERROR);
        }
    }

    private void openProject(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Directory");
        File projectDir = directoryChooser.showDialog(primaryStage);

        if (projectDir != null) {
            currentProjectPath = projectDir.toPath();
            projectExplorer.setRoot(createDirectoryTree(currentProjectPath));
        }
    }

    private void saveProject() {
        if (currentProjectPath != null) {
            for (Tab tab : tabPane.getTabs()) {
                saveFile();
            }
        } else {
            showAlert("No Project", "Please create or open a project first", Alert.AlertType.WARNING);
        }
    }

    private TreeItem<String> createDirectoryTree(Path path) {
        TreeItem<String> root = new TreeItem<>(path.getFileName().toString());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    root.getChildren().add(createDirectoryTree(entry));
                } else {
                    TreeItem<String> fileItem = new TreeItem<>(entry.getFileName().toString());
                    fileItem.setGraphic(new Label("[File]"));
                    root.getChildren().add(fileItem);
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load project structure.", Alert.AlertType.ERROR);
        }
        root.setExpanded(true);
        return root;
    }


    private void createNewTab() {
        Tab tab = new Tab("Untitled");
        TextArea textArea = new TextArea();
        tab.setContent(textArea);
        tab.setOnCloseRequest(e -> {
            if (!confirmClose(tab)) {
                e.consume();
            }
        });
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void openFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                Tab tab = new Tab(file.getName());
                TextArea textArea = new TextArea(content);
                tab.setContent(textArea);
                tab.setUserData(file.toPath());
                tab.setOnCloseRequest(e -> {
                    if (!confirmClose(tab)) {
                        e.consume();
                    }
                });
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().select(tab);
            } catch (IOException e) {
                showAlert("Error", "Failed to open file.", Alert.AlertType.ERROR);
            }
        }
    }

    private void saveFile() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            Path filePath = (Path) selectedTab.getUserData();
            if (filePath != null) {
                writeFile(filePath, getTextFromTab(selectedTab));
                selectedTab.setText(selectedTab.getText().replace("*", ""));
            }
        }
    }

    private void saveAsFile(Stage primaryStage) {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file != null) {
                Path filePath = file.toPath();
                writeFile(filePath, getTextFromTab(selectedTab));
                selectedTab.setUserData(filePath);
                selectedTab.setText(file.getName());
            }
        }
    }

    private boolean confirmClose(Tab tab) {
        if (tab.getText().endsWith("*")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes.");
            alert.setContentText("Do you want to save before closing?");

            ButtonType save = new ButtonType("Save");
            ButtonType dontSave = new ButtonType("Don't Save");
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(save, dontSave, cancel);

            ButtonType result = alert.showAndWait().orElse(cancel);
            if (result == save) {
                saveFile();
                return true;
            } else return result == dontSave;
        }
        return true;
    }

    private String getTextFromTab(Tab tab) {
        TextArea textArea = (TextArea) tab.getContent();
        return textArea.getText();
    }

    private void writeFile(Path filePath, String content) {
        try {
            Files.writeString(filePath, content);
        } catch (IOException e) {
            showAlert("Error", "Failed to save file.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
