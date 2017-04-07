package dbmanager;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import dbmanager.database.Database;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;

public class Manager extends Application
{
    private static Config config;
    private static Database database;
    private static ResourceBundle langBundle;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        config = ConfigFactory.load("configs/manager");

        database = new Database(config.getString("database.url"), config.getString("database.username"), config.getString("database.password"));
        database.connect();

        new Viewer(stage).setScene("manager.fxml").setMaximized(config.getBoolean("maximized")).show();
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfigValue(String s, ConfigValue value) {
        Manager.config = config.withValue(s, value);
    }

    public static Database getDatabase() {
        return database;
    }

    public static String getString(String type, String name) {
        String key = type == null ? name : type + "." + name;
        return langBundle.containsKey(key) ? langBundle.getString(key) : name;
    }

    public static ResourceBundle getLangBundle() {
        Locale curLocale = new Locale(config.getString("language"));
        if (langBundle == null || !langBundle.getLocale().equals(curLocale))
            langBundle = ResourceBundle.getBundle("bundles.strings", curLocale, new Utils.UTF8Control());
        return langBundle;
    }
}
