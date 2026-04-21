package gui;

import gui.EnergyApiClient.HistoricalUsage;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private Label currentHourLabel;
    @FXML private Label communityDepletedLabel;
    @FXML private Label gridPortionLabel;
    @FXML private Label statusLabel;
    @FXML private DatePicker startPicker;
    @FXML private DatePicker endPicker;
    @FXML private TableView<HistoricalUsage> historyTable;
    @FXML private TableColumn<HistoricalUsage, String> hourCol;
    @FXML private TableColumn<HistoricalUsage, Number> producedCol;
    @FXML private TableColumn<HistoricalUsage, Number> usedCol;
    @FXML private TableColumn<HistoricalUsage, Number> gridCol;

    private final EnergyApiClient api = new EnergyApiClient();
    private final ObservableList<HistoricalUsage> rows = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        hourCol.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().hour().format(HOUR_FORMAT)));
        producedCol.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().communityProduced()));
        usedCol.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().communityUsed()));
        gridCol.setCellValueFactory(c -> new ReadOnlyDoubleWrapper(c.getValue().gridUsed()));
        historyTable.setItems(rows);

        startPicker.setValue(LocalDate.now().minusDays(1));
        endPicker.setValue(LocalDate.now());
    }

    @FXML
    public void onLoadCurrent() {
        setStatus("Loading current...");
        new Thread(() -> {
            try {
                var cur = api.fetchCurrent();
                Platform.runLater(() -> {
                    currentHourLabel.setText("Hour: " + cur.hour().format(HOUR_FORMAT));
                    communityDepletedLabel.setText(String.format("Community depleted: %.2f %%", cur.communityDepleted()));
                    gridPortionLabel.setText(String.format("Grid portion: %.2f %%", cur.gridPortion()));
                    setStatus("Current loaded.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Error: " + e.getMessage()));
            }
        }, "fetch-current").start();
    }

    @FXML
    public void onLoadHistorical() {
        LocalDate startDate = startPicker.getValue();
        LocalDate endDate = endPicker.getValue();
        if (startDate == null || endDate == null) {
            setStatus("Pick both start and end dates.");
            return;
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.of(23, 59, 59));
        setStatus("Loading historical...");
        new Thread(() -> {
            try {
                var list = api.fetchHistorical(start, end);
                Platform.runLater(() -> {
                    rows.setAll(list);
                    setStatus("Historical: " + list.size() + " row(s).");
                });
            } catch (Exception e) {
                Platform.runLater(() -> setStatus("Error: " + e.getMessage()));
            }
        }, "fetch-historical").start();
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }
}
