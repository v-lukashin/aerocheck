package tx.vl.aerocheck.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RowAero {
    private final StringProperty id;
    private final StringProperty value;

    public RowAero(String id, String value) {
        this.id = new SimpleStringProperty(id);
        this.value = new SimpleStringProperty(value);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    @Override
    public String toString() {
        return "RowAero{" +
                "id=" + id.get() +
                ", value=" + value.get() +
                '}';
    }
}
