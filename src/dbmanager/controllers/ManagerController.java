package dbmanager.controllers;

import com.typesafe.config.ConfigValueFactory;
import dbmanager.Manager;
import dbmanager.Viewer;
import dbmanager.database.DatabaseCell;
import dbmanager.database.DatabaseColumn;
import dbmanager.database.DatabaseResult;
import dbmanager.database.DatabaseRow;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManagerController
{
    @FXML private BorderPane root;
    @FXML private ResourceBundle resources;

    //menu
    @FXML private RadioMenuItem tableModeItem;
    @FXML private RadioMenuItem requestModeItem;

    @FXML private RadioMenuItem enLanguageItem;
    @FXML private RadioMenuItem ukLanguageItem;

    @FXML private CheckMenuItem toolbarCheckItem;

    @FXML private MenuItem aboutItem;

    //toolbar
    @FXML private BorderPane toolbar;
    @FXML private ComboBox<String> tableSelect;

    //table
    @FXML private TableView<DatabaseRow> tableView;

    public ManagerController() {
    }

    @FXML
    private void initialize() {
        //---table query ComboBox---
        tableSelect.setConverter(new StringConverter<String>() {
            @Override public String toString(String s)   { return Manager.getString("table", s); }
            @Override public String fromString(String s) { return null; }
        });
        tableSelect.setItems(FXCollections.observableList(Manager.getDatabase().getTables()));
        tableSelect.getSelectionModel().selectedItemProperty().addListener((observableValue, o, n) -> showTable((String)n) );

        //---File menu items---
        tableModeItem.setOnAction(e -> changeMode("table"));
        requestModeItem.setOnAction(e -> changeMode("request"));
        switch (Manager.getConfig().getString("mode")) {
            case "table"   : tableModeItem.setSelected(true); break;
            case "request" : requestModeItem.setSelected(true); break;
        }

        //---Edit menu items---

        //---View menu items---
        enLanguageItem.setOnAction(e -> changeLang("en"));
        ukLanguageItem.setOnAction(e -> changeLang("uk"));
        switch (Manager.getConfig().getString("language")) {
            case "en" : enLanguageItem.setSelected(true); break;
            case "uk" : ukLanguageItem.setSelected(true); break;
        }

        toolbarCheckItem.setOnAction(e -> showToolbar(toolbarCheckItem.isSelected()));
        toolbarCheckItem.setSelected(Manager.getConfig().getBoolean("show_toolbar"));
        showToolbar(toolbarCheckItem.isSelected());

        //---Help menu items---
        aboutItem.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION, Manager.getString("about", "title"), Manager.getString("about", "text")));

        //------
        tableView.setColumnResizePolicy(p -> true);
    }

    private void changeLang(String lang) {
        if (!lang.equals(Manager.getConfig().getString("language"))) {
            Manager.setConfigValue("language", ConfigValueFactory.fromAnyRef(lang));
            new Viewer((Stage) root.getScene().getWindow()).setScene("manager.fxml").setMaximized(Manager.getConfig().getBoolean("maximized")).show();
        }
    }

    private void changeMode(String mode) {
        if (!mode.equals(Manager.getConfig().getString("mode"))) {
            Manager.setConfigValue("mode", ConfigValueFactory.fromAnyRef(mode));
//            new Viewer((Stage) root.getScene().getWindow()).setScene("request.fxml").setMaximized(Manager.getConfig().getBoolean("maximized")).show();
        }
    }

    @FXML
    private void close() {
        System.exit(0);
    }

    @FXML
    private void showTable(String tableName) {
        tableView.getColumns().clear();

        if (tableName != null) {
            DatabaseResult result = Manager.getDatabase().query("SELECT * FROM " + tableName);
            ArrayList<TableColumn<DatabaseRow, String>> columns = new ArrayList<>();

            for (DatabaseColumn databaseColumn : result.getColumns()){
                TableColumn<DatabaseRow, String> column = new TableColumn<>(databaseColumn.getName());
                column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCellValue(databaseColumn.getName())));

                String primary = databaseColumn.isPrimary() ? "$" : "";
                String reference = databaseColumn.getReference() == null ? "" : " @ " + Manager.getString("table", databaseColumn.getReference().getTableName());
                column.setText(primary + Manager.getString("column", databaseColumn.getName()) + reference);

                columns.add(column);
            }

            tableView.getColumns().addAll(columns);
            tableView.setItems(FXCollections.observableList(result.getRows()));
        }
    }

    private void showToolbar(boolean enabled) {
        Manager.setConfigValue("show_toolbar", ConfigValueFactory.fromAnyRef(enabled));
        toolbar.setVisible(enabled);
        toolbar.setManaged(enabled);
    }

    @FXML
    private void addRow() {
        Optional<DatabaseRow> optional = showDatabaseDialog(Manager.getString("dialog", "add_row"), Manager.getString("dialog", "add"), tableSelect.getSelectionModel().getSelectedItem(), DatabaseDialogType.ADD);
        optional.ifPresent(row -> {
            System.out.println("1111");
            for (DatabaseCell cell : row.getCells()) {
                System.out.println(cell.getValue());
            }
        });
    }

    @FXML
    private void modifyRow() {
        Optional<DatabaseRow> optional = showDatabaseDialog(Manager.getString("dialog", "modify_row"), Manager.getString("dialog", "save"), tableSelect.getSelectionModel().getSelectedItem(), DatabaseDialogType.MODIFY);
        optional.ifPresent(row -> {
            System.out.println("2222");
            for (DatabaseCell cell : row.getCells()) {
                System.out.println(cell.getValue());
            }
        });
    }

    @FXML
    private void removeRow() {
        Optional<DatabaseRow> optional = showDatabaseDialog(Manager.getString("dialog", "remove_row"), Manager.getString("dialog", "remove"), tableSelect.getSelectionModel().getSelectedItem(), DatabaseDialogType.REMOVE);
        optional.ifPresent(row -> {
            System.out.println("3333");
            for (DatabaseCell cell : row.getCells()) {
                System.out.println(cell.getValue());
            }
        });
    }

    private enum DatabaseDialogType {
        ADD,
        MODIFY,
        REMOVE
    }

    private Optional<DatabaseRow> showDatabaseDialog(String title, String okButtonText, String tableName, DatabaseDialogType type) {
        if (tableName == null) {
            showAlert(Alert.AlertType.ERROR, Manager.getString("error", "title"), Manager.getString("error", "select_table_message"));
            return Optional.empty();
        }

        Dialog<DatabaseRow> dialog = new Dialog<>();

        ButtonType okButtonType = new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(Manager.getString("dialog", "cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);
        dialog.setTitle(title);
        dialog.initOwner(root.getScene().getWindow());

        DatabaseResult allResult = Manager.getDatabase().query("SELECT * FROM " + tableName);
        ArrayList<SimpleStringProperty> values = new ArrayList<>();

        //panes
        BorderPane mainPane = new BorderPane();
        dialog.getDialogPane().setContent(mainPane);

        VBox topPane = new VBox();
        VBox bottomPane = new VBox();

        if (type == DatabaseDialogType.MODIFY || type == DatabaseDialogType.REMOVE)
            mainPane.setTop(topPane);
        if (type == DatabaseDialogType.ADD || type == DatabaseDialogType.MODIFY)
            mainPane.setBottom(bottomPane);

        //row select
        ComboBox<DatabaseRow> rowSelect = new ComboBox<>();
        rowSelect.setItems(FXCollections.observableList(allResult.getRows()));
        topPane.getChildren().addAll(rowSelect);
        rowSelect.setConverter(new StringConverter<DatabaseRow>() {
            @Override public String toString(DatabaseRow s)   { return s.getCell(0).getValue(); }
            @Override public DatabaseRow fromString(String s) { return null; }
        });
        rowSelect.setOnAction(e -> {
            DatabaseRow row = rowSelect.getSelectionModel().getSelectedItem();
            for (int i = 0; i < allResult.getColumns().size(); ++i) {
                values.get(i).setValue(row == null ? "" : row.getCellValue(allResult.getColumns().get(i).getName()));
            }
        });
        VBox.setMargin(rowSelect, new Insets(5, 5, 15, 5));
        rowSelect.setMaxWidth(Double.MAX_VALUE);
        rowSelect.setPromptText(Manager.getString("dialog", "row_select"));

        //fields
        for (DatabaseColumn column : allResult.getColumns()) {
            SimpleStringProperty value = new SimpleStringProperty();
            if (column.getReference() != null) {
                ComboBox<String> list = new ComboBox<>();
                list.setPromptText(Manager.getString("column", column.getName()));
                VBox.setMargin(list, new Insets(5, 5, 5, 5));
                list.setMaxWidth(Double.MAX_VALUE);

                DatabaseResult columnResult = Manager.getDatabase().query("SELECT " + column.getReference().getColumnName() + " FROM " + column.getReference().getTableName());
                list.getItems().addAll(columnResult.getRows().stream().map(t -> t.getCellValue(column.getReference().getColumnName())).collect(Collectors.toList()));

                value.bindBidirectional(list.valueProperty());

                bottomPane.getChildren().add(list);
            } else {
                TextField field = new TextField();
                field.setPromptText(Manager.getString("column", column.getName()));
                VBox.setMargin(field, new Insets(5, 5, 5, 5));

                value.bindBidirectional(field.textProperty());

                if (column.isPrimary() && type == DatabaseDialogType.MODIFY) {
                    field.setDisable(true);
                }

                bottomPane.getChildren().add(field);
            }
            values.add(value);
        }

        //result
        dialog.setOnCloseRequest((DialogEvent e) -> {
//            System.out.println(e.getTarget().getClass());
//            if (e.)
//                e.consume();
        });
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                DatabaseRow row = new DatabaseRow();
                for (int i = 0; i < allResult.getColumns().size(); ++i) {
                    DatabaseCell cell = new DatabaseCell(allResult.getColumns().get(i), values.get(i).getValue());
                    if (!cell.isValid()) {
                        showAlert(Alert.AlertType.ERROR, Manager.getString("error", "title"), cell.getErrorMessage());
                        return null;
                    }
                    row.addCell(cell);
                }
                return row;
            } else {
                return null;
            }
        });

        return dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.initOwner(root.getScene().getWindow());
        alert.showAndWait();
    }
}
