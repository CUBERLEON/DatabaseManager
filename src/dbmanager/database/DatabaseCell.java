package dbmanager.database;

import dbmanager.Manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
        boolean valid = true;
        errorMessage = "";

        System.out.println(column.getType());

        String type = column.getType();
        if (value.isEmpty()) {
            valid = false;
            errorMessage = "empty_field";
        } else if (type.equals("text")) {

        } else if (type.contains("int")) {
            if (!value.matches("\\d+")) {
                valid = false;
                errorMessage = "not_integer";
            }
        } else if (type.equals("date")) {
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                df.setLenient(false);
                df.parse(value);
            } catch (ParseException e) {
                valid = false;
                errorMessage = "invalid_date";
            }
        }

        errorMessage = column.getDisplayName() + ": " + Manager.getString("alert", errorMessage);

        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
