package dbmanager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class Viewer
{
    private Stage stage;
    private Scene scene;
    private boolean maximized;

    public Viewer(Stage stage) {
        this.stage = stage;
        this.scene = null;
        this.maximized = false;
    }

    public Viewer setScene(Scene scene) {
        this.scene = scene;
        return this;
    }

    public Viewer setScene(String name) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(Manager.getLangBundle());
            Parent root = loader.load(new FileInputStream("res/fxml/" + name));
            if (stage.getScene() == null)
                scene = new Scene(root);
            else {
                scene = stage.getScene();
                stage.getScene().setRoot(root);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return this;
    }

    public Viewer setMaximized(boolean maximized) {
        this.maximized = maximized;
        return this;
    }

    public void show() {
        stage.setScene(scene);
        stage.setMaximized(maximized);
        stage.show();
    }
}
