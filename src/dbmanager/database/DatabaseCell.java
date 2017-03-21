package dbmanager.database;

public class DatabaseCell
{
    private DatabaseColumn column;
    private String value;

    private String errorMessage;

    public DatabaseCell(DatabaseColumn col, String value) {
        this.column = col;
        this.value = value;
        this.errorMessage = "";
    }

    public DatabaseColumn getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isValid() {
        errorMessage = "invalid data";
        return true; //TODO implement isValid method
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
