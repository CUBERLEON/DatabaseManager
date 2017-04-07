package dbmanager.database;

import javafx.beans.property.SimpleStringProperty;

public class SqlQueryParameter
{
    private String name;
    private SimpleStringProperty value;

    public SqlQueryParameter(String name) {
        this.name = name;
        this.value = new SimpleStringProperty();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value.getValue();
    }

    public SimpleStringProperty getValueProperty() {
        return value;
    }
}
