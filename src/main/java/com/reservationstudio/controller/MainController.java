package com.reservationstudio.controller;

import com.reservationstudio.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/*MainController is the primary controller for the restaurant dashboard screen
It manages three main areas of the UI:
-The floor plan showing table layout
-The reservation side panel listing upcoming and seated guests
-Header showing current date and live clock

It handles all user interactions: clicking tables, adding/moving/ending reservations, assigning servers, and
filtering side panel by search query
 */

public class MainController {
    private static final String[] ROW_COLORS = {
            "#C19A6B", "#D2793A", "#8B2635", "#5C4033", "#9E8E80", "#D8A0A8"
    };

    /*Service layer for reading and writing all CSV data files*/
    private final CsvService csvService = new CsvService("src/main/Data/reservations.csv");

    /*Observable list of all reservations currently in memory*/
    private final ObservableList<Reservation> allReservations = FXCollections.observableArrayList();

    /*List of servers*/
    private List<Server> servers;

    /*Maps table numbers to the full name of assigned server*/
    private Map<Integer, String> assignments = new HashMap<>();

    /*Physical table definitions*/
    private final List<RestaurantTable> tables = List.of(
            //Bottom row
            new RestaurantTable(1, 2, 30, 540, 90, 100),
            new RestaurantTable(2, 6, 190, 540, 165, 100),

            //Middle
            new RestaurantTable(3, 6, 40, 280, 100, 165),
            new RestaurantTable(4, 6, 190, 385, 165, 100),
            new RestaurantTable(5, 6, 190, 230, 165, 100),

            //4 center tables for 2
            new RestaurantTable(6, 2, 400, 550, 100, 90),
            new RestaurantTable(7, 2, 400, 450, 100, 90),
            new RestaurantTable(8, 2, 400, 350, 100, 90),
            new RestaurantTable(9, 2, 400, 250, 100, 90)
    );

    @FXML private TextField searchField;
    @FXML private VBox seatedList;
    @FXML private VBox reservationList;
    @FXML private Label dateLabel;
    @FXML private Label timeLabel;
    @FXML private AnchorPane floorPlanContainer;

    private FloorPlanView floorPlanView;
    private ContextMenu activeMenu;

    /*Sets up the floor plan, live clock, and loads all data*/
    @FXML
    private void initialize() {
        floorPlanView = new FloorPlanView(tables);
        floorPlanContainer.getChildren().add(floorPlanView.getRoot());
        AnchorPane.setTopAnchor(floorPlanView.getRoot(), 0.0);
        AnchorPane.setRightAnchor(floorPlanView.getRoot(), 0.0);
        AnchorPane.setBottomAnchor(floorPlanView.getRoot(), 0.0);
        AnchorPane.setLeftAnchor(floorPlanView.getRoot(), 0.0);

        dateLabel.setText("DATE: " + LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy")));



        /*Live clock fix*/
        /*Previously clock was set up once on startup and never updated, so displayed time would freeze
        at whatever time app launched.
         */
        DateTimeFormatter clockFmt = DateTimeFormatter.ofPattern("h: mm a");
        timeLabel.setText(LocalTime.now().format(clockFmt));
        Timeline clock = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> timeLabel.setText(LocalTime.now().format(clockFmt)))
        );
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        /*Register table click callback*/
        floorPlanView.setOnTableClicked(tableNum -> handleTableClicked(tableNum));
        searchField.textProperty().addListener((obs, oldValue, newValue) -> filterLists(newValue));

        servers = csvService.loadServers();
        System.out.println("Servers loaded: " + servers.size());
        assignments = csvService.loadAssignments();
        loadData();
    }

    /*Delegates to showaddDialog*/
    @FXML
    private void handleAddReservation() {
        showAddDialog();
    }

    /*Loads all reservations from data files, populates list and refreshes side panel and floor plan*/
    private void loadData() {
        List<Reservation> loaded = csvService.loadAll();
        allReservations.setAll(loaded);
        refreshLists(allReservations);
        floorPlanView.refresh(allReservations, assignments);
    }

    /*Handles a table node being clicked on the floor plan
    Builds a menu with actions appropriate to the table's current state
     */
    private void handleTableClicked(int tableNum){
        /*Close menus first*/
        if (activeMenu != null){
            activeMenu.hide();
        }

        /*Find reservation at this table, if it exists*/
        Optional<Reservation> maybeRes = allReservations.stream().filter(r -> r.getTableNumber() == tableNum).findFirst();

        System.out.println("Table clicked: " + tableNum);
        System.out.println("Reservation found: " + maybeRes.isPresent());

        /*Find physical table object to show its capacity*/

        RestaurantTable table = tables.stream()
                .filter(t -> t.getNumber() == tableNum)
                .findFirst()
                .orElse(null);

        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #3a3a3a; " + "-fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;");

        String headerText = table != null
                ? "Table " + tableNum + " (cap: " + table.getCapacity() + ")"
                : "Table " + tableNum;
        MenuItem header = new MenuItem(headerText);
        header.setStyle("-fx-text-fill: #888888; -fx-font-family: 'Georgia'; " + "-fx-font-size: 11px; -fx-font-style: italic;");
        header.setDisable(true); /*not clickable*/

        menu.getItems().add(header);

        /*Current server line in header*/
        String currentServer = assignments.getOrDefault(tableNum, null);
        MenuItem serverLine = new MenuItem(currentServer != null
                ? "Server: " + currentServer
                : "Server: Unassigned");
        serverLine.setStyle("-fx-text-fill: #aaaaaa; -fx-font-family: 'Georgia'; " + "-fx-font-size: 11px; -fx-font-style: italic;");
        serverLine.setDisable(true);
        menu.getItems().add(serverLine);

        menu.getItems().add(new SeparatorMenuItem());

        if (maybeRes.isEmpty()){
            /*empty table*/
            MenuItem addItem = new MenuItem("+ Add Reservation");
            addItem.setStyle("-fx-text-fill: #cccccc; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
            addItem.setOnAction(e->showAddDialog());
            menu.getItems().add(addItem);
        } else {
            /*Occupied table*/
            Reservation res = maybeRes.get();

            MenuItem guestInfo = new MenuItem(res.getName() + " ~ " + res.getGuests() + " guests");
            guestInfo.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Georgia'; " + "-fx-font-size: 12px; -fx-font-weight: bold;");
            guestInfo.setDisable(true);
            menu.getItems().add(guestInfo);
            menu.getItems().add(new SeparatorMenuItem());

            /*Seat/Unseat toggle*/

            boolean isSeated = res.getStatus() == Reservation.Status.SEATED;
            MenuItem toggleItem = new MenuItem(isSeated ? " Mark as reserved" : " Seat Guests");
            toggleItem.setStyle("-fx-text-fill: #cccccc; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
            toggleItem.setOnAction(e -> toggleStatus(res));

            /*move to different table*/

            MenuItem moveItem = new MenuItem("Move to another table");
            moveItem.setStyle("-fx-text-fill: #cccccc; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
            moveItem.setOnAction(e -> handleMoveTable(res));

            /*End reservation*/

            MenuItem endItem = new MenuItem("End Reservation");
            endItem.setStyle("-fx-text-fill: #cccccc; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
            endItem.setOnAction(e -> deleteReservation(res));

            menu.getItems().addAll(toggleItem, moveItem, endItem);
            menu.getItems().add(new SeparatorMenuItem());
        }

        /*Assign server*/

        MenuItem assignItem = new MenuItem("Assign Server");
        assignItem.setStyle("-fx-text-fill: #cccccc; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
        assignItem.setOnAction(e -> handleAssignServer(tableNum));
        menu.getItems().add(assignItem);

        activeMenu = menu;


        /*Show menu at cursor position*/
        StackPane tableNode = floorPlanView.getTableNode(tableNum);
        if(tableNode != null){
            menu.show(tableNode, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    /*Assign server handler*/
    private void handleAssignServer(int tableNum){

        if (activeMenu != null){
            activeMenu.hide();
        }

        ContextMenu serverMenu = new ContextMenu();
        serverMenu.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #3a3a3a; " + "-fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;");

        MenuItem prompt = new MenuItem("Assign a server to Table " + tableNum);
        prompt.setStyle("-fx-text-fill: #888888; -fx-font-family: 'Georgia'; " + "-fx-font-size: 11px; -fx-font-style: italic;");
        prompt.setDisable(true);
        serverMenu.getItems().add(prompt);
        serverMenu.getItems().add(new SeparatorMenuItem());

        MenuItem unassign = new MenuItem("Unassign");
        unassign.setStyle("-fx-text-fill: #e05555; -fx-font-family: 'Georgia'; -fx-font-size: 12 px;");
        unassign.setOnAction(e -> {
            assignments.remove(tableNum);
            csvService.saveAssignments(assignments);
            floorPlanView.refresh(allReservations, assignments);
        });
        serverMenu.getItems().add(unassign);
        serverMenu.getItems().add(new SeparatorMenuItem());

        if (servers.isEmpty()) {
            MenuItem none = new MenuItem("No servers on file");
            none.setStyle("-fx-text-fill: #666666; -fx-font-family: 'Georgia'; -fx-font-size: 12px;");
            none.setDisable(true);
            serverMenu.getItems().add(none);
        } else {
            for (Server s : servers) {
                boolean isCurrent = s.getFullName().equals(assignments.get(tableNum));
                MenuItem item = new MenuItem((isCurrent ? " " : " ") + s.getFullName());
                item.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 12px; " + (isCurrent ? "-fx-text-fill: #88cc88," : "-fx-text-fill: #cccccc;"));
                item.setOnAction(e -> {
                    assignments.put(tableNum, s.getFullName());
                    csvService.saveAssignments(assignments);
                    floorPlanView.refresh(allReservations, assignments);
                });
                serverMenu.getItems().add(item);
            }
        }
        activeMenu = serverMenu;

        StackPane tableNode = floorPlanView.getTableNode(tableNum);
        if (tableNode != null){
            javafx.geometry.Bounds bounds = tableNode.localToScreen(tableNode.getBoundsInLocal());
            serverMenu.show(tableNode, bounds.getMinX(), bounds.getMaxY());
        }
    }

    /*Move table handler:
    Allows staff to reassign a reservation to a different table
     */

    private void handleMoveTable(Reservation res) {
        ContextMenu moveMenu = new ContextMenu();
        moveMenu.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #3a3a3a; " + "-fx-border-width: 1; -fx-background-radius: 6; -fx-border-radius: 6;");

        MenuItem prompt = new MenuItem("Move to which table?");
        prompt.setStyle("-fx-text-fill: #888888; -fx-font-family: 'Georgia'; " + "-fx-font-size: 11px; -fx-font-style: italic;");
        prompt.setDisable(true);
        moveMenu.getItems().add(prompt);
        moveMenu.getItems().add(new SeparatorMenuItem());

        /*Collect tables that are occupied*/

        java.util.Set<Integer> occupiedTables = allReservations.stream().map(Reservation::getTableNumber).collect(java.util.stream.Collectors.toSet());

        boolean anyAvailable = false;
        for (RestaurantTable t : tables) {
            if (t.getNumber() == res.getTableNumber()) continue;

            boolean isFree = !occupiedTables.contains(t.getNumber());
            boolean fitsGuests = res.getGuests() <= t.getCapacity();

            String label = "Table " + t.getNumber() + " (cap: " + t.getCapacity() + ")";
            if (!isFree) {
                label += "  - occupied";
            }
            if (!fitsGuests) {
                label += "  - too small";
            }

            MenuItem tableOption = new MenuItem(label);
            tableOption.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 12px; " +
                    (isFree && fitsGuests ? "-fx-text-fill: #cccccc;" : "-fx-text-fill: #666666;"));
            tableOption.setDisable(!isFree || !fitsGuests);

            if (isFree && fitsGuests) {
                final int targetNum = t.getNumber();
                tableOption.setOnAction(e -> {
                    res.setTableNumber(targetNum);
                    csvService.saveAll(allReservations);
                    refreshLists(allReservations);
                    floorPlanView.refresh(allReservations, assignments);
                });
                anyAvailable = true;
            }
            moveMenu.getItems().add(tableOption);
        }
        if (!anyAvailable) {
            MenuItem none = new MenuItem("No available tables");
            none.setStyle("-fx-text-fill: #666666; -fx-font-family: 'Georgia'; -fx-font-size: 12px");
            none.setDisable(true);
            moveMenu.getItems().add(none);
        }

        activeMenu = moveMenu;

        StackPane tableNode = floorPlanView.getTableNode(res.getTableNumber());
        if (tableNode != null) {
            javafx.geometry.Bounds bounds = tableNode.localToScreen(tableNode.getBoundsInLocal());
            moveMenu.show(tableNode, bounds.getMinX(), bounds.getMaxY() + 4);
        }
    }

/*Clears and rebuilds both side panel lists (seated) + (Reservation)*/
    private void refreshLists(List<Reservation> toShow) {
        seatedList.getChildren().clear();
        reservationList.getChildren().clear();

        for (Reservation reservation : toShow) {
            HBox row = buildRow(reservation);
            if (reservation.getStatus() == Reservation.Status.SEATED) {
                seatedList.getChildren().add(row);
            } else {
                reservationList.getChildren().add(row);
            }
        }

        if (seatedList.getChildren().isEmpty()) {
            seatedList.getChildren().add(emptyLabel("No seated guests"));
        }
        if (reservationList.getChildren().isEmpty()) {
            reservationList.getChildren().add(emptyLabel("No reservations"));
        }
    }

    /*Filters the side panel lists*/
    private void filterLists(String query) {
        if (query == null || query.isBlank()) {
            refreshLists(allReservations);
            return;
        }

        String lowered = query.toLowerCase();
        List<Reservation> filtered = allReservations.stream()
                .filter(r -> r.getName().toLowerCase().contains(lowered) || r.getTime().contains(lowered))
                .collect(Collectors.toList());
        refreshLists(filtered);
    }
    /*Builds a single Hbox row representing one row in the side panel. Row contains guest name, time, table number
    and buttons toggle to seated status or end the reservation
     */
    private HBox buildRow(Reservation reservation) {

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 12, 7, 12));
        row.setStyle("-fx-background-color:  #ffffff; -fx-background-radius: 4;");

        Label name = new Label(reservation.getName());
        name.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        name.setTextFill(Color.BLACK);
        name.setPrefWidth(80);

        Label time = new Label(reservation.getTime());
        time.setFont(Font.font("Georgia", 12));
        time.setTextFill(Color.web("#1e1e1e"));
        time.setPrefWidth(50);

        Label tableNum = new Label(String.valueOf(reservation.getTableNumber()));
        tableNum.setFont(Font.font("Georgia", 12));
        tableNum.setTextFill(Color.web("#1e1e1e"));
        tableNum.setPrefWidth(50);


        Label guests = new Label(reservation.getGuests() + "guests");
        guests.setFont(Font.font("Georgia", 12));
        guests.setTextFill(Color.web("#1e1e1e"));
        guests.setPrefWidth(55);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button toggle = new Button(reservation.getStatus() == Reservation.Status.SEATED ? "Reserve" : "Seat");
        toggle.setStyle("-fx-background-color: rgba(0,0,0,0.25); -fx-text-fill: white; "
                + "-fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 2 6 2 6;");
        toggle.setOnAction(event -> toggleStatus(reservation));

        Button delete = new Button("...");
        delete.setStyle("-fx-background-color: transparent; -fx-text-fill: black; "
                + "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 0 2 0 2;");
        delete.setOnAction(event -> deleteReservation(reservation));

        row.getChildren().addAll(name, time, tableNum, spacer, toggle, delete);
        return row;
    }
    /*Returns a placeholder label used when a side panel is empty*/
    private Label emptyLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #555555; -fx-font-family: 'Georgia'; "
                + "-fx-font-size: 11px; -fx-padding: 6 10 6 10;");
        return label;
    }
    /*Opens the reservation dialogue box*/
    private void showAddDialog() {
        AddReservationDialog dialog = new AddReservationDialog(tables, allReservations);
        Optional<Reservation> result = dialog.showAndWait();
        result.ifPresent(reservation -> {
            allReservations.add(reservation);
            csvService.saveAll(allReservations);
            refreshLists(allReservations);
            floorPlanView.refresh(allReservations, assignments);
        });
    }
    /*Handles reservation deletion*/
    private void deleteReservation(Reservation reservation) {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Remove " + reservation.getName() + " from the list?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirm.setTitle("Remove Reservation");
        confirm.setHeaderText(null);
        confirm.getDialogPane().setStyle("-fx-background-color: #1a1a1a;");
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.YES) {
                allReservations.remove(reservation);
                csvService.saveAll(allReservations);
                refreshLists(allReservations);
                floorPlanView.refresh(allReservations, assignments);
            }
        });
    }
    /*Toggles a reservations status between SEATED and RESERVATION*/
    private void toggleStatus(Reservation reservation) {
        if (reservation.getStatus() == Reservation.Status.SEATED) {
            reservation.setStatus(Reservation.Status.RESERVATION);
        } else {
            reservation.setStatus(Reservation.Status.SEATED);
        }
        csvService.saveAll(allReservations);
        refreshLists(allReservations);
        floorPlanView.refresh(allReservations, assignments);
    }
}
