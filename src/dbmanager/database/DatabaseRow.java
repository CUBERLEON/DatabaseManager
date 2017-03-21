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
}
