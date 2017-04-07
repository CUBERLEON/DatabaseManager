package dbmanager.controllers;

import com.typesafe.config.ConfigValueFactory;
import dbmanager.Manager;
import dbmanager.Viewer;
import dbmanager.database.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManagerController
{
    @FXML private BorderPane root;
    @FXML private ResourceBundle resources;

    //menu
    @FXML private MenuItem selectTableItem;
    @FXML private MenuItem requestItem;
    @FXML private MenuItem chartItem;

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
        selectTableItem.setOnAction(e -> {
            //TODO
        });
        requestItem.setOnAction(e -> {
            Optional<String> result = showQueryDialog();
            result.ifPresent(this::show);
        });
        chartItem.setOnAction(e -> {
            BorderPane mainPane = new BorderPane();

            LineChart<Number,Number> lineChart = new LineChart<>(new NumberAxis(),new NumberAxis());
            lineChart.getXAxis().setLabel(Manager.getString("chart", "month"));
            lineChart.getYAxis().setLabel(Manager.getString("chart", "revenue"));
            lineChart.setTitle(null);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            lineChart.getData().add(series);
            for (int i = 0; i < 12; ++i) series.getData().add(new XYChart.Data<>(i + 1, 0));
            mainPane.setCenter(lineChart);

            TextField yearField = new TextField();
            yearField.setPromptText(Manager.getString("chart", "year"));
            yearField.textProperty().addListener(newYear -> {
                DatabaseResult result = Manager.getDatabase().query("SELECT extract(month from buy_rel.date) id, SUM(buy_rel.price) height FROM buy_rel WHERE extract(year from buy_rel.date)='" + yearField.getText() + "' GROUP BY extract(month from buy_rel.date) ORDER BY extract(month from buy_rel.date)");
                series.setName(yearField.getText());
                for (int i = 0, j = 0; i < 12; ++i) {
                    if (j < result.getRows().size() && Integer.parseInt(result.getRows().get(j).getCellValue("id"))-1 == i) {
                        series.getData().get(i).setYValue(Integer.parseInt(result.getRows().get(j).getCellValue("height")));
                        ++j;
                    } else {
                        series.getData().get(i).setYValue(0);
                    }
                }
            });
            yearField.setText("2016");
            mainPane.setTop(yearField);

            showAlert(Alert.AlertType.INFORMATION, Manager.getString("dialog", "chart"), null, mainPane, false);
        });

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

        //---table---
        tableView.setColumnResizePolicy(p -> true);
    }

    private void changeLang(String lang) {
        if (!lang.equals(Manager.getConfig().getString("language"))) {
            Manager.setConfigValue("language", ConfigValueFactory.fromAnyRef(lang));
            new Viewer((Stage) root.getScene().getWindow()).setScene("manager.fxml").setMaximized(Manager.getConfig().getBoolean("maximized")).show();
        }
    }

    @FXML
    private void close() {
        System.exit(0);
    }

    @FXML
    private void showTable(String tableName) {
        tableView.getColumns().clear();
        if (tableName != null) show("SELECT * FROM " + tableName);
    }

    private void show(String sqlQuery) {
        DatabaseResult result = Manager.getDatabase().query(sqlQuery);
        ArrayList<TableColumn<DatabaseRow, String>> columns = new ArrayList<>();

        for (DatabaseColumn databaseColumn : result.getColumns()){
            TableColumn<DatabaseRow, String> column = new TableColumn<>(databaseColumn.getName());
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCellValue(databaseColumn.getName())));
            column.setText(databaseColumn.getDisplayName());
            columns.add(column);
        }

        tableView.getColumns().setAll(columns);
        tableView.setItems(FXCollections.observableList(result.getRows()));
    }

    private void showToolbar(boolean enabled) {
        Manager.setConfigValue("show_toolbar", ConfigValueFactory.fromAnyRef(enabled));
        toolbar.setVisible(enabled);
        toolbar.setManaged(enabled);
    }

    @FXML
    private void addRow() {
        String tableName = tableSelect.getSelectionModel().getSelectedItem();
        Optional<DatabaseRow> optional = showDatabaseDialog(Manager.getString("dialog", "add_row"), Manager.getString("dialog", "add"), tableName, DatabaseDialogType.ADD);
        optional.ifPresent(row -> {
            if (!Manager.getDatabase().update("INSERT INTO " + tableName + " (" + row.getAllNamesString() + ") VALUES (" + row.getAllValuesString() + ")"))
                showAlert(Alert.AlertType.ERROR, Manager.getString("alert", "error"), Manager.getString("alert", "insert_failed"));
            else
                showTable(tableName);
        });
    }

    @FXML
    private void modifyRow() {
        String tableName = tableSelect.getSelectionModel().getSelectedItem();
        Optional<DatabaseRow> optional = showDatabaseDialog(Manager.getString("dialog", "modify_row"), Manager.getString("dialog", "save"), tableName, DatabaseDialogType.MODIFY);
        optional.ifPresent(row -> {
            if (!Manager.getDatabase().update("UPDATE " + tableName + " SET " + row.getRegularColumnsString() + " WHERE " + row.getPrimaryKeysString()))
                showAlert(Alert.AlertType.ERROR, Manager.getString("alert", "error"), Manager.getString("alert", "update_failed"));
            else
                showTable(tableName);
        });
    }

    @FXML
    private void removeRow() {
        String tableName = tableSelect.getSelectionModel().getSelectedItem();
        Optional<DatabaseRow> optional = showDatabaseDialog(Manager.getString("dialog", "remove_row"), Manager.getString("dialog", "remove"), tableName, DatabaseDialogType.REMOVE);
        optional.ifPresent(row -> {
            if (!Manager.getDatabase().update("DELETE FROM " + tableName + " WHERE " + row.getPrimaryKeysString()))
                showAlert(Alert.AlertType.ERROR, Manager.getString("alert", "error"), Manager.getString("alert", "remove_failed"));
            else
                showTable(tableName);
        });
    }

    private enum DatabaseDialogType {
        ADD,
        MODIFY,
        REMOVE
    }

    private Optional<DatabaseRow> showDatabaseDialog(String title, String okButtonText, String tableName, DatabaseDialogType type) {
        if (tableName == null) {
            showAlert(Alert.AlertType.ERROR, Manager.getString("alert", "error"), Manager.getString("alert", "no_selected_table"));
            return Optional.empty();
        }

        Dialog<DatabaseRow> dialog = createDialog(title, okButtonText);

        DatabaseResult allResult = Manager.getDatabase().query("SELECT * FROM " + tableName);
        ArrayList<SimpleStringProperty> values = new ArrayList<>();

        //panes
        BorderPane mainPane = new BorderPane();
        dialog.getDialogPane().setContent(mainPane);

        VBox topPane = new VBox();
        VBox bottomPane = new VBox();

        if (type == DatabaseDialogType.MODIFY || type == DatabaseDialogType.REMOVE)
            mainPane.setTop(topPane);
        if (type == DatabaseDialogType.ADD || type == DatabaseDialogType.MODIFY || type == DatabaseDialogType.REMOVE)
            mainPane.setBottom(bottomPane);

        //row select
        ComboBox<DatabaseRow> rowSelect = new ComboBox<>();
        rowSelect.setItems(FXCollections.observableList(allResult.getRows()));
        topPane.getChildren().addAll(rowSelect);
        rowSelect.setConverter(new StringConverter<DatabaseRow>() {
            @Override public String toString(DatabaseRow s)   { return s.getCells().stream().map(DatabaseCell::getValue).collect(Collectors.joining(" | ")); }
            @Override public DatabaseRow fromString(String s) { return null; }
        });
        rowSelect.valueProperty().addListener(e -> {
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
                list.setPromptText(column.getDisplayName());
                VBox.setMargin(list, new Insets(5, 5, 5, 5));
                list.setMaxWidth(Double.MAX_VALUE);

                DatabaseResult columnResult = Manager.getDatabase().query("SELECT " + column.getReference().getColumnName() + " FROM " + column.getReference().getTableName());
                list.getItems().addAll(columnResult.getRows().stream().map(t -> t.getCellValue(column.getReference().getColumnName())).collect(Collectors.toList()));

                value.bindBidirectional(list.valueProperty());

                if (type == DatabaseDialogType.REMOVE) list.setDisable(true);

                bottomPane.getChildren().add(list);
            } else {
                TextField field = new TextField();
                field.setPromptText(column.getDisplayName());
                VBox.setMargin(field, new Insets(5, 5, 5, 5));

                value.bindBidirectional(field.textProperty());

                if ((column.isPrimary() && type == DatabaseDialogType.MODIFY) || type == DatabaseDialogType.REMOVE) field.setDisable(true);

                bottomPane.getChildren().add(field);
            }

            values.add(value);
        }

        //auto select
        if (type == DatabaseDialogType.MODIFY || type == DatabaseDialogType.REMOVE)
            rowSelect.getSelectionModel().select(tableView.getSelectionModel().getSelectedItem());

        dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0)).addEventFilter(ActionEvent.ACTION, event -> {
            for (int i = 0; i < allResult.getColumns().size(); ++i) {
                DatabaseCell cell = new DatabaseCell(allResult.getColumns().get(i), values.get(i).getValue());
                if (!cell.isValid()) {
                    showAlert(Alert.AlertType.ERROR, Manager.getString("alert", "error"), cell.getErrorMessage());
                    event.consume();
                    break;
                }
            }
        });

        //result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                DatabaseRow row = new DatabaseRow();
                for (int i = 0; i < allResult.getColumns().size(); ++i) row.addCell(new DatabaseCell(allResult.getColumns().get(i), values.get(i).getValue()));
                return row;
            } else {
                return null;
            }
        });

        return dialog.showAndWait();
    }

    private Optional<String> showQueryDialog() {
        Dialog<String> dialog = createDialog(Manager.getString("dialog", "query"), Manager.getString("dialog", "ok"));

        BorderPane mainPane = new BorderPane();
        dialog.getDialogPane().setContent(mainPane);

        ArrayList<SqlQuery> queries = new ArrayList<>();
        queries.addAll(Arrays.asList(new SqlQuery("SELECT * FROM book WHERE book.title~ALL('{$pattern}')").setDisplayName(Manager.getString("query", "search_by_column")),
                                     new SqlQuery("SELECT * FROM $a").setDisplayName("Select all columns from table $a"),
                                     new SqlQuery("SELECT $a FROM $b").setDisplayName("Select column $a from table $b")));

        ComboBox<SqlQuery> queriesList = new ComboBox<>();
        queriesList.setPromptText(Manager.getString("dialog", "query_prompt_text"));
        BorderPane.setMargin(queriesList, new Insets(5, 5, 10, 5));
        queriesList.setMaxWidth(Double.MAX_VALUE);
        queriesList.getItems().setAll(queries);
        queriesList.setConverter(new StringConverter<SqlQuery>() {
            @Override public String toString(SqlQuery s)   { return s.getDisplayName(); }
            @Override public SqlQuery fromString(String s) { return null; }
        });
        mainPane.setTop(queriesList);

        VBox parametersPane = new VBox();
        mainPane.setCenter(parametersPane);

        queriesList.valueProperty().addListener(e -> {
            parametersPane.getChildren().clear();
            SqlQuery query = queriesList.getSelectionModel().getSelectedItem();
            if (query != null) {
                for (SqlQueryParameter parameter : query.getParameters()) {
                    TextField field = new TextField();
                    field.setPromptText(parameter.getName());
                    VBox.setMargin(field, new Insets(5, 5, 5, 5));
                    parameter.getValueProperty().bindBidirectional(field.textProperty());
                    parametersPane.getChildren().add(field);
                }
            }
            dialog.getDialogPane().getScene().getWindow().sizeToScene();
        });

        dialog.setResultConverter(dialogButton -> {
            SqlQuery query = queriesList.getSelectionModel().getSelectedItem();
            if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE && query != null) {
                return query.build();
            } else {
                return null;
            }
        });

        return dialog.showAndWait();
    }

    private <T> Dialog<T> createDialog(String title, String okButtonText) {
        Dialog<T> dialog = new Dialog<>();

        ButtonType okButtonType = new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(Manager.getString("dialog", "cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);
        dialog.setTitle(title);
        dialog.initOwner(root.getScene().getWindow());

        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String hearder) {
        showAlert(type, title, hearder, null, true);
    }

    private void showAlert(Alert.AlertType type, String title, String header, Node content, boolean displayGraphic) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.initOwner(root.getScene().getWindow());
        alert.getDialogPane().setContent(content);
        if (!displayGraphic)
            alert.setGraphic(null);
        alert.showAndWait();
    }
}
