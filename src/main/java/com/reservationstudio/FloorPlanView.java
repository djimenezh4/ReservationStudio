package com.reservationstudio;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FloorPlanView {

    private static final String DEFAULT_COLOR = "#5a5a5a";
    // Table colors matching screenshot palette
    private static final String[] OCCUPIED_COLORS = {
            "#0097A7", // Table 1 – teal
            "#1565C0", // Table 2 – deep blue
            "#6A1B9A", // Table 3 – deep purple
            "#00838F", // Table 4 – dark cyan
            "#283593", // Table 5 – indigo
            "#4527A0"  // Table 6 – violet
    };


    private final Pane canvas = new Pane();
    private final List<RestaurantTable> tables;
    // Map: tableNumber -> StackPane node
    private final Map<Integer, StackPane> tableNodes = new HashMap<>();
    private final Map<Integer, Label> serverLabels = new HashMap<>();

    
    private Consumer<Integer> onTableClicked;
    
    public void setOnTableClicked(Consumer<Integer> handler){
        this.onTableClicked = handler;
    }
    
    public FloorPlanView(List<RestaurantTable> tables) {
        this.tables = tables;
        buildLayout();
    }

    private void buildLayout() {
        // Black background
        canvas.setStyle("-fx-background-color: #141414;");
        canvas.setPrefSize(680, 700);

        // Floor Plan pill header
        Label header = new Label("Floor Plan");
        header.setFont(Font.font("Georgia", FontWeight.NORMAL, 18));
        header.setTextFill(Color.WHITE);
        header.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 20; " +
                "-fx-padding: 6 30 6 30;");
        header.setLayoutX(200);
        header.setLayoutY(20);
        canvas.getChildren().add(header);

        // Bar label
        Label bar = new Label("Bar");
        bar.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        bar.setTextFill(Color.WHITE);
        bar.setStyle("-fx-background-color: #2a2a2a; -fx-background-radius: 6; " +
                "-fx-padding: 10 4 10 4;");
        bar.setPrefWidth(380);
        bar.setLayoutX(30);
        bar.setLayoutY(140);
        canvas.getChildren().add(bar);

        // Draw tables
        for (int i = 0; i < tables.size(); i++) {
            RestaurantTable t = tables.get(i);
            StackPane node = createTableNode(t);
            node.setLayoutX(t.getLayoutX());
            node.setLayoutY(t.getLayoutY());
            tableNodes.put(t.getNumber(), node);
            canvas.getChildren().add(node);
        }
    }

    private StackPane createTableNode(RestaurantTable t) {
        StackPane sp = new StackPane();
        sp.setPrefSize(t.getWidth(), t.getHeight());

        Rectangle bg = new Rectangle(t.getWidth(), t.getHeight());
        bg.setFill(Color.web(DEFAULT_COLOR));
        bg.setArcWidth(30);
        bg.setArcHeight(30);

        Label numLabel = new Label(String.valueOf(t.getNumber()));
        numLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        numLabel.setTextFill(Color.web("#f5f0eb"));

        /*Server Name Label*/

        Label serverLabel = new Label("");
        serverLabel.setFont(Font.font("Georgia", 9));
        serverLabel.setTextFill(Color.web("#cccccc"));
        serverLabel.setStyle("-fx-font-style: italic;");

        serverLabels.put(t.getNumber(), serverLabel);

        VBox content = new VBox(2, numLabel, serverLabel);
        content.setAlignment(Pos.CENTER);


        sp.getChildren().addAll(bg, numLabel);
        
        sp.setOnMouseEntered(e -> sp.setOpacity(0.75));
        sp.setOnMouseExited(e -> sp.setOpacity(1.0));
        
        sp.setOnMouseClicked(e -> {
            if (onTableClicked != null){
                onTableClicked.accept(t.getNumber());
            }
        });
        
        sp.setStyle("-fx-cursor: hand;");
        return sp;
    }

    // Refresh table based on current reservation list
    public void refresh(List<Reservation> reservations, Map<Integer, String> assignments) {
        // Reset all tables to dark grey
        for (int i = 0; i < tables.size(); i++) {
            RestaurantTable t = tables.get(i);
            StackPane sp = tableNodes.get(t.getNumber());
            if (sp == null) continue;
            Rectangle bg = (Rectangle) sp.getChildren().get(0);
            bg.setFill(Color.web(DEFAULT_COLOR));
            bg.setStroke(null);
            bg.setStrokeWidth(0);
        }

        // Apply vibrant color when seated
        for (Reservation r : reservations) {
            StackPane sp = tableNodes.get(r.getTableNumber());
            if (sp == null) continue;
            Rectangle bg = (Rectangle) sp.getChildren().get(0);
            int idx = (r.getTableNumber() - 1) % OCCUPIED_COLORS.length;
            if (r.getStatus() == Reservation.Status.SEATED) {
                bg.setFill(Color.web(OCCUPIED_COLORS[idx]));
                bg.setStroke(null);
            } else {
                // Reserved — dark grey with a subtle yellow outline
                bg.setFill(Color.web(DEFAULT_COLOR));
                bg.setStroke(Color.web("#FFD700"));
                bg.setStrokeWidth(2);
            }
        }

        for (RestaurantTable t : tables){
            Label lbl = serverLabels.get(t.getNumber());
            if (lbl == null) continue;
            String name = assignments.get(t.getNumber());
            lbl.setText(name != null ? name : "");
        }
    }

    public Pane getRoot() { return canvas; }

    /*Table node accessor*/

    public StackPane getTableNode(int Tablenumber) {
        return tableNodes.get(Tablenumber);
    }
}
