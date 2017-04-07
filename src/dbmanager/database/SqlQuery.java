package dbmanager.database;

import org.mentaregex.Regex;

import java.util.ArrayList;
import java.util.Arrays;

public class SqlQuery
{
    private String displayName;
    private String query;
    private ArrayList<SqlQueryParameter> parameters;

    public SqlQuery(String query) {
        this.query = query;
        this.displayName = query;
        this.parameters = new ArrayList<>();

        String[] m = Regex.match(query, "/\\$([a-z]+[a-z0-9_]*)/ig");
        if (m != null) {
            for (int i = 0; i < m.length; ++i) {
                final String q = m[i];
                if (parameters.stream().filter(e -> e.getName().equals(q)).count() == 0)
                    parameters.add(new SqlQueryParameter(m[i]));
            }
        }
    }

//    public SqlQuery addParameter(SqlQueryParameter parameter) {
//        parameters.add(parameter);
//        return this;
//    }
//
//    public SqlQuery addParameters(String... names) {
//        for (String name : names)
//            this.parameters.add(new SqlQueryParameter(name));
//        return this;
//    }


    public String getDisplayName() {
        return displayName;
    }

    public SqlQuery setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public SqlQueryParameter getParameter(String name) {
        for (SqlQueryParameter parameter : parameters)
            if (parameter.getName().equals(name))
                return parameter;
        return null;
    }

    public ArrayList<SqlQueryParameter> getParameters() {
        return parameters;
    }

    public String build() {
        String res = query;
        for (SqlQueryParameter parameter : parameters) {
            res = res.replaceAll("\\$" + parameter.getName(), parameter.getValue());
        }
        return res;
    }
}
