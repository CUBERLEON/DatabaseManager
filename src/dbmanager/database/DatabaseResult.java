package dbmanager.database;

import java.util.ArrayList;

public class DatabaseResult
{
    private ArrayList<DatabaseColumn> columns;
    private ArrayList<DatabaseRow> rows;

    public DatabaseResult(ArrayList<DatabaseColumn> cols, ArrayList<DatabaseRow> rows) {
        this.columns = cols;
        this.rows = rows;
    }

    public ArrayList<DatabaseColumn> getColumns() {
        return columns;
    }

    public ArrayList<DatabaseRow> getRows() {
        return rows;
    }


}
