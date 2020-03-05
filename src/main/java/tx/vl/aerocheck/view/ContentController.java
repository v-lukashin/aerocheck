package tx.vl.aerocheck.view;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Info;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.async.AsyncClient;
import com.aerospike.client.cluster.Node;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.control.CheckComboBox;
import tx.vl.aerocheck.Main;
import tx.vl.aerocheck.model.RowAero;
import tx.vl.aerocheck.util.Util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ContentController {
    private AsyncClient client;
    private Map<String, List<String>> mapNamespaces;
    private Map<String, List<String>> mapBins;

    @FXML
    public ChoiceBox<String> namespace;
    @FXML
    private ChoiceBox<String> setname;
    @FXML
    private TableView<RowAero> tableView;
    @FXML
    private TableColumn<RowAero, String> columnId;
    @FXML
    private TableColumn<RowAero, String> columnValue;
    @FXML
    private Button getById;
    @FXML
    private Button scanAll;
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private CheckComboBox<String> projectFields;
    @FXML
    private TextField limit;
    @FXML
    private TextField recordId;

    public ContentController() {
    }

    private Main mainApp;

    @FXML
    private void initialize() {
        columnId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        columnValue.setCellValueFactory(cellData -> cellData.getValue().valueProperty());

        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.setTitle("Selected item:");
                alert.setHeaderText(null);
                alert.setContentText(null);
                alert.getButtonTypes().add(ButtonType.CANCEL);

                TextArea textArea = new TextArea(((TableView<RowAero>) event.getSource()).getSelectionModel().getSelectedItem().getValue());
                textArea.setEditable(false);
                textArea.setWrapText(true);

                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(textArea, 0, 0);
                alert.getDialogPane().setExpandableContent(expContent);
                alert.getDialogPane().setExpanded(true);

                alert.showAndWait();
            }
        });

        namespace.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setname.getItems().clear();
            setname.getItems().addAll(mapNamespaces.get(newValue));
            projectFields.getCheckModel().clearChecks();
            projectFields.getItems().clear();
            projectFields.getItems().addAll(mapBins.get(newValue));
        });


        getById.setOnAction(event -> {

            if (checkNotDefaultRepoSettings()) {
                if (recordId.getText() != null && !recordId.getText().isEmpty()) {
                    mainApp.getListValues().clear();
                    Record record = client.get(null, new Key(namespace.getValue(), setname.getValue(), recordId.getText()));
                    if (record != null) {
                        ObservableList<String> fields = projectFields.getCheckModel().getCheckedItems();
                        if (fields.size() > 0) {
                            HashSet<String> set = new HashSet<>(fields);
                            record.bins.forEach((k, v) -> {
                                if (set.contains(k)) mainApp.getListValues().add(new RowAero(k, v.toString()));
                            });
                        } else {
                            record.bins.forEach((k, v) -> mainApp.getListValues().add(new RowAero(k, v == null ? "null" : v.toString())));
                        }
                    }
                } else {
                    showWarning("Id is not set");
                }
            } else {
                showWarning("Choose namespace and setname");
            }
        });
        scanAll.setOnAction(event -> { // scan/stop-scan
            if (checkNotDefaultRepoSettings()) {
//            if (true) {
                String limitStr = limit.getText();
                int limit = 10;
                if (limitStr != null && !limitStr.isEmpty()) try {
                    limit = Integer.parseInt(limitStr);
                } catch (Exception e) {
                }
                AtomicInteger countdown = new AtomicInteger(limit);

                tableView.getItems().clear();
                ObservableList<String> fields = projectFields.getCheckModel().getCheckedItems();
                String[] projFields = (fields.size() > 0) ? fields.toArray(new String[fields.size()]) : null;

                try {
                    progressIndicator.setVisible(true);
                    for (Node node : client.getNodes()) {
                        client.scanNode(null, node, namespace.getValue(), setname.getValue(), (k, v) -> {
                            if (countdown.decrementAndGet() < 0) throw new AerospikeException("Stop scan!");
                            RowAero rowAero = new RowAero(Objects.toString(k.userKey), v.bins.toString());
                            tableView.getItems().add(rowAero);
                        }, projFields);
                    }
                } catch (Exception e) {
                    showWarning(e.getMessage());
                } finally {
                    progressIndicator.setVisible(false);
                }

            } else {
                showWarning("Choose namespace and setname");
            }
        });
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;

        tableView.setItems(mainApp.getListValues());

        refreshAerospikeInfo(mainApp.getHosts().get());
        mainApp.getHosts().addListener((observable, oldValue, newValue) -> refreshAerospikeInfo(newValue));
    }

    private void refreshAerospikeInfo(String hosts) {
        client = new AsyncClient(null, Util.parseAerospikeHosts(hosts));
        Node[] nodes = client.getNodes();
        if (nodes.length > 0) {
            parseNamespaces(Info.request(null, nodes[0], "sets"));
            parseBins(Info.request(null, nodes[0], "bins"));
        }
    }

    private void parseNamespaces(String aerospikeResponse) {
        mapNamespaces = new HashMap<>();

        String[] rawNamespaces = aerospikeResponse.split(";");
        for (int i = 0; i < rawNamespaces.length; i++) {
            String rawNamespace = rawNamespaces[i];

            int startNs = rawNamespace.indexOf("ns=") + 3;
            int endNs = rawNamespace.indexOf(":", startNs);
            String ns = rawNamespace.substring(startNs, endNs);

            int startSet = rawNamespace.indexOf("set=") + 4;
            int endSet = rawNamespace.indexOf(":", startSet);
            String set = rawNamespace.substring(startSet, endSet);

            List<String> sets;
            if (mapNamespaces.containsKey(ns)) sets = mapNamespaces.get(ns);
            else mapNamespaces.put(ns, sets = new ArrayList<>());
            sets.add(set);
        }
        namespace.getItems().clear();
        namespace.getItems().addAll(mapNamespaces.keySet());
    }

    private void parseBins(String aerospikeResponse) {
        mapBins = new HashMap<>();

        String[] namespaces = aerospikeResponse.split(";");
        for (int i = 0; i < namespaces.length; i++) {
            String[] namespaceSettings = namespaces[i].split(":");
            try {
                String namespace = namespaceSettings[0];

                String[] binsSplit = namespaceSettings[1].split("bin_names_quota=")[1].split(",");
                ArrayList<String> bins = new ArrayList<>();
                for (int j = 1; j < binsSplit.length; j++) bins.add(binsSplit[j]);
                mapBins.put(namespace, bins);
            } catch (ArrayIndexOutOfBoundsException e) {
                showWarning("Problem parse bins!: " + e.getMessage());
            }
        }
        projectFields.getCheckModel().clearChecks();
        projectFields.getItems().clear();
    }

    private boolean checkNotDefaultRepoSettings() {
        if (namespace.getValue() == null || setname.getValue() == null) return false;
        return true;
    }

    private static void showWarning(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public void close() {
        if (client != null) client.close();
    }
}
