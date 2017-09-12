package tx.vl.aerocheck;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tx.vl.aerocheck.model.RowAero;
import tx.vl.aerocheck.view.ContentController;
import tx.vl.aerocheck.view.TopMenuController;

public class Main extends Application {

    private Stage stage;
    private ObservableList<RowAero> listValues = FXCollections.observableArrayList();
    private StringProperty hosts = new SimpleStringProperty();

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        stage.getIcons().add(new Image("images/aerospike.png"));

        FXMLLoader menuLoader = new FXMLLoader();
        menuLoader.setLocation(getClass().getResource("view/top-menu.fxml"));
        BorderPane menuBar = menuLoader.load();
        menuLoader.<TopMenuController>getController().setMainApp(this);

        FXMLLoader contentLoader = new FXMLLoader();
        contentLoader.setLocation(getClass().getResource("view/content.fxml"));
        AnchorPane content = contentLoader.load();
        contentLoader.<ContentController>getController().setMainApp(this);

        menuBar.setCenter(content);

        primaryStage.setTitle("AeroCheck");
        primaryStage.setScene(new Scene(menuBar));
        primaryStage.show();
    }

    public ObservableList<RowAero> getListValues() {
        return listValues;
    }

    public StringProperty getHosts() {
        return hosts;
    }

    public Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
