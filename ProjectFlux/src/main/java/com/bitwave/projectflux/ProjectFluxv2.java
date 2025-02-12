package com.bitwave.projectflux;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class ProjectFluxv2 extends Application {
    private TreeView<String> projectExplorer;
    private TabPane editorTabs;
    private Map<TreeItem<String>, Path> treeItemPathMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        // Initialize main components
        BorderPane root = new BorderPane();

        projectExplorer = new TreeView<>();
        editorTabs = new TabPane();

        // Create project management pane
        VBox projectPane = new VBox(new Label("Project Explorer"), projectExplorer);
        projectPane.setMinWidth(250);

        // Set up layout
        SplitPane splitPane = new SplitPane(projectPane, editorTabs);
        splitPane.setDividerPositions(0.2);
        root.setCenter(splitPane);

        // Set up menu bar
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);

        // Set up project explorer click handling
        setUpProjectExplorer();

        // Show stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("ProjectFlux IDE");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem openProject = new MenuItem("Open Project");
        openProject.setOnAction(e -> openProjectFolder(primaryStage));
        fileMenu.getItems().addAll(openProject);

        menuBar.getMenus().addAll(fileMenu);
        return menuBar;
    }

    private void openProjectFolder(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Folder");
        Path projectPath = directoryChooser.showDialog(primaryStage).toPath();

        if (projectPath != null && Files.isDirectory(projectPath)) {
            populateProjectExplorer(projectPath);
        }
    }

    private void populateProjectExplorer(Path rootPath) {
        TreeItem<String> rootItem = createTreeItem(rootPath);
        projectExplorer.setRoot(rootItem);
        rootItem.setExpanded(true);
    }

    private TreeItem<String> createTreeItem(Path path) {
        TreeItem<String> item = new TreeItem<>(path.getFileName().toString());
        treeItemPathMap.put(item, path);

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path subPath : stream) {
                    item.getChildren().add(createTreeItem(subPath));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return item;
    }

    private void setUpProjectExplorer() {
        projectExplorer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> selectedItem = projectExplorer.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Path filePath = treeItemPathMap.get(selectedItem);
                    if (filePath != null && Files.isRegularFile(filePath)) {
                        openFileInEditor(filePath);
                    } else {
                        System.out.println("Not a file or file path could not be resolved.");
                    }
                } else {
                    System.out.println("No item selected.");
                }
            }
        });
    }

    private void openFileInEditor(Path filePath) {
        try {
            String content = Files.readString(filePath);
            Tab tab = new Tab(filePath.getFileName().toString());
            TextArea textArea = new TextArea(content);
            tab.setContent(textArea);
            editorTabs.getTabs().add(tab);
            editorTabs.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
