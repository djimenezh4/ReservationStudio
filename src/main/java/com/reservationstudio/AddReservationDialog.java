package com.reservationstudio;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class AddReservationDialog extends Dialog<Reservation> {

    private final TextField nameField = new TextField();
    private final TextField timeField = new TextField();
    private final Spinner<Integer> guestSpinner = new Spinner<>(1, 4, 2);
    private final ComboBox<String> tableCombo = new ComboBox<>();
    private final ComboBox<String> statusCombo = new ComboBox<>();

    private final Label errorLabel = new Label();
    private static final int BUFFER_MINUTES = 60;
    public AddReservationDialog(List<RestaurantTable> tables, List<Reservation> existingReservations) {
        setTitle("Add Reservation");
        setHeaderText(null);

        // Style the dialog pane
        DialogPane dp = getDialogPane();
        dp.setStyle("-fx-background-color: #1a1a1a;");

        // Title label
        Label title = new Label("NEW RESERVATION");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#e8c5a0"));

        // Form grid
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 30));

        // Style labels
        String labelStyle = "-fx-text-fill: #aaaaaa; -fx-font-family: 'Georgia'; -fx-font-size: 12px;";
        String fieldStyle = "-fx-background-color: #2d2d2d; -fx-text-fill: white; " +
                "-fx-border-color: #555555; -fx-border-radius: 4; -fx-background-radius: 4; " +
                "-fx-font-size: 13px;";

        Label nameLabel = new Label("Guest Name:");
        nameLabel.setStyle(labelStyle);
        nameField.setPromptText("e.g. JOHN");
        nameField.setStyle(fieldStyle);
        nameField.setPrefWidth(200);

        nameField.setTextFormatter(new TextFormatter<>(change ->{
            String newText = change.getControlNewText();
            if(newText.matches("[a-zA-Z\\s\\-]*")){
                return change;
            }
            return null;
        }));

        Label timeLabel = new Label("Time (HH:MM):");
        timeLabel.setStyle(labelStyle);
        timeField.setPromptText("e.g. 14:30");
        timeField.setStyle(fieldStyle);

        timeField.setTextFormatter(new TextFormatter<>(change ->{
            String newText = change.getControlNewText();
            if (newText.matches("[0-9:]{0,5}")){
                return change;
            }
            return null;
        }));

        Label guestLabel = new Label("Guests:");
        guestLabel.setStyle(labelStyle);
        guestSpinner.setStyle(fieldStyle);
        guestSpinner.setEditable(true);

        guestSpinner.focusedProperty().addListener((obs, wasFocused, isNowFocused)->{
            if (!isNowFocused) {
                guestSpinner.increment(0);
                SpinnerValueFactory.IntegerSpinnerValueFactory vf = (SpinnerValueFactory.IntegerSpinnerValueFactory) guestSpinner.getValueFactory();
                int clamped = Math.max(vf.getMin(), Math.min(vf.getMax(), guestSpinner.getValue()));
                vf.setValue(clamped);
            }

            });

        Label tableLabel = new Label("Table:");
        tableLabel.setStyle(labelStyle);
        for (RestaurantTable t : tables) {
            tableCombo.getItems().add("Table " + t.getNumber() + " (cap: " + t.getCapacity() + ")");
        }
        if (!tableCombo.getItems().isEmpty()) tableCombo.getSelectionModel().selectFirst();
        tableCombo.setStyle(labelStyle);
        tableCombo.setPrefWidth(200);

        if (!tableCombo.getItems().isEmpty()) tableCombo.getSelectionModel().selectFirst();
        tableCombo.setStyle(labelStyle);
        tableCombo.setPrefWidth(200);

        tableCombo.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) ->
                {
        int idx = newIdx.intValue();
        if (idx >= 0 && idx < tables.size()) {
            int cap = tables.get(idx).getCapacity();
            int current = guestSpinner.getValue();
            guestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, cap, Math.min(current, cap)));

        }
        });

        if (!tables.isEmpty()){
            int cap = tables.get(0).getCapacity();
            guestSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, cap, Math.min(2, cap)));
        }

        Label statusLabel = new Label("Status:");
        statusLabel.setStyle(labelStyle);
        statusCombo.getItems().addAll("RESERVATION", "SEATED");
        statusCombo.getSelectionModel().selectFirst();
        statusCombo.setStyle(labelStyle);
        statusCombo.setPrefWidth(200);

        grid.add(nameLabel,   0, 0); grid.add(nameField,   1, 0);
        grid.add(timeLabel,   0, 1); grid.add(timeField,   1, 1);
        grid.add(guestLabel,  0, 2); grid.add(guestSpinner,1, 2);
        grid.add(tableLabel,  0, 3); grid.add(tableCombo,  1, 3);
        grid.add(statusLabel, 0, 4); grid.add(statusCombo, 1, 4);

        errorLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-family: 'Georgia'; -fx-font-size:11px;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

        VBox content = new VBox(14, title, new Separator(), grid, errorLabel);
        content.setPadding(new Insets(10));
        dp.setContent(content);

        // Buttons
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dp.getButtonTypes().addAll(addBtn, cancelBtn);

        // Style buttons
        Button addButton = (Button) dp.lookupButton(addBtn);
        addButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; " +
                "-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 13px; " +
                "-fx-background-radius: 6;");

        Button cancelButton = (Button) dp.lookupButton(cancelBtn);
        cancelButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; " +
                "-fx-font-family: 'Georgia'; -fx-font-size: 13px; -fx-background-radius: 6;");

        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event ->{
            String validationError = validate(tables, existingReservations);
            if(validationError !=null){
                errorLabel.setText(validationError);
                errorLabel.setVisible(true);
                event.consume();
            }
        });

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == addBtn) {
                String name = nameField.getText().trim().toUpperCase();
                String time = timeField.getText().trim();
                int guests = guestSpinner.getValue();
                int tableIdx = tableCombo.getSelectionModel().getSelectedIndex();
                int tableNum = (tableIdx >= 0 && tableIdx < tables.size())
                        ? tables.get(tableIdx).getNumber() : 1;
                Reservation.Status status = statusCombo.getValue().equals("SEATED")
                        ? Reservation.Status.SEATED : Reservation.Status.RESERVATION;
                if (!name.isEmpty() && !time.isEmpty()) {
                    return new Reservation(name, time, guests, tableNum, status);
                }
            }
            return null;
        });

}private String validate(List<RestaurantTable> tables, List<Reservation> existingReservations) {
    // 1. Name must not be empty and must be letters only
    String name = nameField.getText().trim();
    if (name.isEmpty()) {
        return "Guest name cannot be empty.";
    }
    //if (!name.matches("[a-zA-Z\\s\\-]+")) {
      //  return "Guest name must contain letters only (no numbers or symbols).";
    //}

    // 2. Time must be valid 24-hour HH:MM
    String time = timeField.getText().trim();
    if (!isValidTime(time)) {
        return "Time must be in HH:MM format (00:00 – 23:59), e.g. 14:30.";
    }

    // 3. Guest count vs table capacity
    int tableIdx = tableCombo.getSelectionModel().getSelectedIndex();
    if (tableIdx < 0 || tableIdx >= tables.size()) {
        return "Please select a valid table.";
    }
    RestaurantTable selectedTable = tables.get(tableIdx);
    int guests = guestSpinner.getValue();
    if (guests > selectedTable.getCapacity()) {
        return "Table " + selectedTable.getNumber() + " only has capacity for "
                + selectedTable.getCapacity() + " guests.";
    }

    // 4. No duplicate: same table + same time
    int newMinutes = toMinutes(normalizeTime(time));
    int tableNum = selectedTable.getNumber();
    for (Reservation r : existingReservations){
        if (r.getTableNumber() != tableNum) continue;
        if (!isValidTime(r.getTime()))continue;
        int existingMinutes = toMinutes(r.getTime());
        if(Math.abs(newMinutes - existingMinutes) < BUFFER_MINUTES){
            return "Table " + tableNum + " already has a reservation at " + r.getTime() + ".New reservations must be atleast " + BUFFER_MINUTES + "minutes apart.";
        }
    }
    return null;
}


private boolean isValidTime(String time) {
    if (time == null || !time.matches("\\d{1,2}:\\d{2}")) return false;
    String[] parts = time.split(":");
    int hours = Integer.parseInt(parts[0]);
    int minutes = Integer.parseInt(parts[1]);
    return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59;
}

private int toMinutes(String time){
        String[]parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
}
private String normalizeTime(String time) {
    if (time == null || !time.contains(":")) return time;
    String[] parts = time.split(":");
    return String.format("%02d:%02d", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
}
}
