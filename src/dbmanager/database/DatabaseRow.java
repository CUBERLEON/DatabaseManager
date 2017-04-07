package dbmanager.database;

import java.util.ArrayList;

public class DatabaseRow
{
    private ArrayList<DatabaseCell> cells;

    public DatabaseRow() {
        cells = new ArrayList<>();
    }

    public DatabaseRow(ArrayList<DatabaseCell> cells) {
        this.cells = cells;
    }

    public void addCell(DatabaseCell cell) {
        cells.add(cell);
    }

    public DatabaseCell getCell(String columnName) {
        for (DatabaseCell cell : cells) if (cell.getColumn().getName().equals(columnName)) return cell;
        return null;
    }

    public DatabaseCell getCell(int index) {
        return cells.get(index);
    }

    public String getCellValue(String columnName) {
        DatabaseCell cell = getCell(columnName);
        return cell == null ? "???" : cell.getValue();
    }

    public ArrayList<DatabaseCell> getCells() {
        return cells;
    }

    public String getAllNamesString() {
        String res = "";
        for (DatabaseCell cell : cells) res += (res.isEmpty() ? "" : ", ") + cell.getColumn().getName();
        return res;
    }

    public String getAllValuesString() {
        String res = "";
        for (DatabaseCell cell : cells) res += (res.isEmpty() ? "" : ", ") + "'" + cell.getValue() + "'";
        return res;
    }

    public String getPrimaryKeysString() {
        String res = "";
        for (DatabaseCell cell : cells)
            if (cell.getColumn().isPrimary())
                res += (res.isEmpty() ? "" : " AND ") + cell.getColumn().getName() + "='" + cell.getValue() + "'";
        return res;
    }

    public String getRegularColumnsString() {
        String res = "";
        for (DatabaseCell cell : cells)
            if (!cell.getColumn().isPrimary())
                res += (res.isEmpty() ? "" : ", ") + cell.getColumn().getName() + "='" + cell.getValue() + "'";
        return res;
    }
}
