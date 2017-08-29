package tx.vl.aerocheck.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import tx.vl.aerocheck.Main;

public class TopMenuController {
    private Main mainApp;

    @FXML
    private void changeCluster() {
        TextInputDialog dialog = new TextInputDialog(null);
        dialog.setTitle("Setup Aerospike hosts");
        dialog.setTitle(null);
        dialog.setContentText("Setup Aerospike hosts for connect: ");
        dialog.showAndWait().ifPresent(s -> mainApp.getHosts().setValue(s));
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
}
