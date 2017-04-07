package dbmanager.database;

import dbmanager.Manager;

public class DatabaseColumn
{
    private String type;
    private String name;

    private String tableName;

    private DatabaseReference reference;
    private boolean primary;

    public DatabaseColumn(String type, String label, String tableName, DatabaseReference reference, boolean primary) {
        this.type = type;
        this.name = label;

        this.tableName = tableName;
        this.reference = reference;
        this.primary = primary;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        String pry = primary ? "$" : "";
        String ref = reference == null ? "" : " @ " + Manager.getString("table", reference.getTableName());
        return pry + Manager.getString("column", name) + ref;
    }

    public String getTableName() {
        return tableName;
    }

    public DatabaseReference getReference() {
        return reference;
    }

    public boolean isPrimary() {
        return primary;
    }
}
