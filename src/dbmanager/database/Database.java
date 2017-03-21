package dbmanager.database;

import java.sql.*;
import java.util.ArrayList;

public class Database
{
    private String url;
    private String user;
    private String password;

    private Connection connection;

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getTables() {
        ArrayList<String> tables = new ArrayList<>();
        try {
            ResultSet result = connection.createStatement().executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public'");
            while (result.next()) tables.add(result.getString("table_name"));
         } catch (Exception ignored) {}
        return tables;
    }

//    public ArrayList<DatabaseColumn> getTableColumns(String tableName) {
//        ArrayList<DatabaseColumn> columns = new ArrayList<>();
//        try {
//            ResultSetMetaData data = connection.createStatement().executeQuery("SELECT * FROM " + tableName).getMetaData();
//            for (int i = 1; i <= data.getColumnCount(); ++i) {
//                String colName = data.getColumnName(i), colTableName = data.getTableName(i);
//                DatabaseColumn column = new DatabaseColumn(data.getColumnTypeName(i), colName, colTableName, findReference(colTableName, colName), isPrimaryKey(colTableName, colName));
//                columns.add(column);
//            }
//        } catch (Exception ignored) {}
//        return columns;
//    }

    public DatabaseResult query(String sqlQuery) {
        ArrayList<DatabaseRow> rows = new ArrayList<>();
        ArrayList<DatabaseColumn> columns = new ArrayList<>();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sqlQuery);
            ResultSetMetaData data = resultSet.getMetaData();

            for (int i = 1; i <= data.getColumnCount(); ++i) {
                String colName = data.getColumnName(i), colTableName = data.getTableName(i);
                DatabaseColumn column = new DatabaseColumn(data.getColumnTypeName(i), colName, colTableName, findReference(colTableName, colName), isPrimaryKey(colTableName, colName));
                columns.add(column);
            }

            while (resultSet.next()) {
                DatabaseRow row = new DatabaseRow();
                for (int i = 1; i <= data.getColumnCount(); ++i)
                    row.addCell(new DatabaseCell(columns.get(i-1), resultSet.getString(data.getColumnName(i))));
                rows.add(row);
            }
        } catch (Exception ignored) {}
        return new DatabaseResult(columns, rows);
    }

    public DatabaseReference findReference(String tableName, String columnName) {
        DatabaseReference reference = null;
        try {
            ResultSet resultSet = connection.getMetaData().getImportedKeys(null, null, tableName);
            while (resultSet.next()) {
                if (resultSet.getString("fkcolumn_name").equals(columnName))
                    reference = new DatabaseReference(resultSet.getString("pktable_name"), resultSet.getString("pkcolumn_name"));
            }
        } catch (Exception ignored) {}
        return reference;
    }

    public boolean isPrimaryKey(String tableName, String columnName) {
        try {
            ResultSet resultSet = connection.getMetaData().getPrimaryKeys(null, null, tableName);
            while (resultSet.next()) {
                if (resultSet.getString("column_name").equals(columnName))
                    return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
