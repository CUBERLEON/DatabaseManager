package dbmanager.database;

public class DatabaseReference
{
    private String tableName;
    private String columnName;

    public DatabaseReference(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }
}
