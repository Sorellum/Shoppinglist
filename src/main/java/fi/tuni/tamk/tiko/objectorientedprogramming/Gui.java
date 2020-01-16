package fi.tuni.tamk.tiko.objectorientedprogramming;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import fi.tuni.tamk.tiko.objectorientedprogramming.JSONObject;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.BufferedReader;
/**
 * Class holding all methods related to shopping list gui operation. 
 * 
 * @author Lassi Markkinen
 * @version 2019.1216
 */
public class Gui extends Application {
    /**
     * Method which is called when the application is starting. Creates a scene with specific size and content.
     * Adjusts the gui window to be centered on the user screen and sets a style. Finally the method shows the application window to
     * the user.
     * 
     * @param window The application window.
     */
    @Override
    public void start(Stage window) {
        window.setTitle("Shopping list");
        Scene content = new Scene(generateContent(window), 640, 480);
        window.initStyle(StageStyle.DECORATED);
        window.setScene(content);
        window.centerOnScreen();
        window.show();
    }

    /**
     * Main method which calls Application's launch() -method .
     * @param args Not used.
     */
    public static void main(String[] args) {
        launch(args);
    }
    /**
     * The generateContent method creates a BorderPane object and calls several other methods which create elements to populate the 
     * BorderPane with. 
     * 
     * @param window The application window.
     * @return The created BorderPane, populated by elements generated by other methods.
     */
    public BorderPane generateContent(Stage window) {
        BorderPane root = new BorderPane();
        TableView tableView = createTable();
        GridPane bottomGrid = createBottomPanel(tableView);
        MenuBar menuBar = createMenu(tableView, window);

        root.setCenter(tableView);
        root.setBottom(bottomGrid);
        root.setTop(menuBar);
        return root;
    }
    /**
     * Method which writes a given String variable into a given file.
     * 
     * @param content String variable to write.
     * @param file File to write into.
     */
    private void saveShoppingListToFile(String content, File file) {
        try (PrintWriter writer = new PrintWriter(file)) {

            writer.println(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method which creates a TableView instance and returns it. The TableView will contain two TableColumns of String and JSONObject type.
     * The TableColumns use getKey() and getValue() -methods from JSONObjects added into the table. This is done with PropertyValueFactory.
     * 
     * @return The generated TableView.
     */
    private TableView createTable() {

        TableView tableView = new TableView();

        TableColumn<String, JSONObject> keyColumn = new TableColumn<>("Item");
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<String, JSONObject> valueColumn = new TableColumn<>("Amount");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        tableView.getColumns().add(keyColumn);
        tableView.getColumns().add(valueColumn);

        return tableView;
    }
    /**
     * Method which generates a MenuBar for the gui application. The Menu contains two MenuItems for saving and loading files in the application.
     * FileChooser is used to create actions for these MenuItems. For saving a shoppinglist, JsonParser is used to parse the JSONObjects inside 
     * the TableView into a String with json formatting. saveShoppingListToFile is then called for the file writing process.
     * 
     * For file loading, FileReader is used to read the file the user has chosen through FileChooser. StringBuffer is used to collect the contents 
     * of the text file. JsonParser parseShoppingList is then used to create an array of JSONObjects to add into the table.
     * 
     * @param table TableView holding JSONObjects.
     * @param window The application window.
     * @return The generated MenuBar.
     */
    @SuppressWarnings("unchecked")
    private MenuBar createMenu(TableView table, Stage window) {

        MenuBar menuBar = new MenuBar();
        Menu file = new Menu("File");
        MenuItem save = new MenuItem("Save as...");

        save.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();

            List<JSONObject> jsonObjects = table.getItems();

            String saveableString = JsonParser.parseMultipleIntoJsonArray(jsonObjects);

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);

            File saveFile = fileChooser.showSaveDialog(window);

            if (saveFile != null) {
                saveShoppingListToFile(saveableString, saveFile);
            }

        });
        MenuItem load = new MenuItem("Open file...");

        load.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);

            File loadFile = fileChooser.showOpenDialog(window);
                
                StringBuilder stringBuffer = new StringBuilder();
 
                if (loadFile != null) {
                    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(loadFile))) {
                        String text;
                        while((text = bufferedReader.readLine()) != null) {
                            stringBuffer.append(text);
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    table.getItems().removeAll(table.getItems());
                    table.getItems().addAll(JsonParser.parseShoppingList(stringBuffer.toString()));
                } 
        });

        file.getItems().addAll(save, load);
        menuBar.getMenus().add(file);

        return menuBar;
    }
    /**
     * Method which instanciates a GridPane which holds the Buttons, TextField and ComboBox of the bottom part of the gui.
     * The ComboBox is given a range of 0-10 which can be used to choose the amount of particular items on the shoppinglist.
     * The Button actions utilize the TableView's selectionModel to remove or replace specific items from the table.
     * 
     * Finally, the buttons are added to the GridPane and column constraints are created as well as buttons widths adjusted to provide
     * better visuals for the user.
     * 
     * @param table TableView holding JSONObjects.
     * @return The generated GridPane.
     */
    @SuppressWarnings("unchecked")
    private GridPane createBottomPanel(TableView table) {
        GridPane bottomGrid = new GridPane();
        TextField keyField = new TextField();
        
        ComboBox<Integer> valueBox = new ComboBox<>(FXCollections.observableArrayList(0,1,2,3,4,5,6,7,8,9,10));
        valueBox.getSelectionModel().selectFirst();
        
        Button addToTableButton = new Button("Add item");
        addToTableButton.setPadding(new Insets(5, 5, 5, 5));

        addToTableButton.setOnAction((event) -> {
            if(keyField.getCharacters().length() > 0 && valueBox.getValue() > 0) {
                table.getItems().add(new JSONObject(keyField.getCharacters().toString(), (Integer)valueBox.getValue()));
            }  
        });

        Button removeFromTableButton = new Button("Remove item");
        removeFromTableButton.setPadding(new Insets(5, 5, 5, 5));
        removeFromTableButton.setOnAction((event) -> {
            if(table.getSelectionModel().getSelectedItem() != null) {
                table.getItems().remove(table.getSelectionModel().getSelectedItem());
            }       
        });

        Button replaceTableButton = new Button("Replace item");
        replaceTableButton.setPadding(new Insets(5, 5, 5, 5));
        replaceTableButton.setOnAction((event) -> {
            if(table.getSelectionModel().getSelectedItem() != null && valueBox.getValue() > 0) {
                int index = table.getSelectionModel().getSelectedIndex();
                table.getItems().remove(table.getSelectionModel().getSelectedItem());
                table.getItems().add(index, new JSONObject(keyField.getCharacters().toString(), (Integer)valueBox.getValue()));
            }     
        });

        for(int i = 0; i < 7; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            if(i == 0) {
                colConst.setPercentWidth(100 / 3f);
            }
            colConst.setPercentWidth(100 / 4f);
            bottomGrid.getColumnConstraints().add(colConst);
        }
        bottomGrid.add(keyField, 0, 0);
        bottomGrid.add(valueBox, 1, 0);
        bottomGrid.add(addToTableButton, 3, 0);
        bottomGrid.add(replaceTableButton, 4, 0);
        bottomGrid.add(removeFromTableButton, 5, 0);
        bottomGrid.setPadding(new Insets(10, 10, 10, 10));

        GridPane.setFillWidth(addToTableButton, true);
        addToTableButton.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(replaceTableButton, true);
        replaceTableButton.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(removeFromTableButton, true);
        removeFromTableButton.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(keyField, true);
        keyField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setFillWidth(valueBox, true);
        valueBox.setMaxWidth(Double.MAX_VALUE);

        return bottomGrid;
    }
}