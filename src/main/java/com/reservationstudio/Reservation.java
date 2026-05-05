package com.reservationstudio;

import javafx.beans.property.*;

public class Reservation {

    public enum Status { SEATED, RESERVATION }

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty time = new SimpleStringProperty();
    private final IntegerProperty guests = new SimpleIntegerProperty();
    private final IntegerProperty tableNumber = new SimpleIntegerProperty();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>();

    public Reservation(String name, String time, int guests, int tableNumber, Status status) {
        this.name.set(name);
        this.time.set(time);
        this.guests.set(guests);
        this.tableNumber.set(tableNumber);
        this.status.set(status);
    }

    //    Name   
    public String getName() { return name.get(); }


    //    Time   
    public String getTime() { return time.get(); }

    //    Guests   
    public int getGuests() { return guests.get(); }

    //    Table   
    public int getTableNumber() { return tableNumber.get(); }

    //    Set Table Num

    public void setTableNumber(int v) { tableNumber.set(v);}

    //    Status   
    public Status getStatus() { return status.get(); }
    public void setStatus(Status v) { status.set(v); }

    // Convert to CSV row: name,time,guests,tableNumber,status
    public String toCsvRow() {
        return String.join(",",
                getName(),
                getTime(),
                String.valueOf(getGuests()),
                String.valueOf(getTableNumber()),
                getStatus().name()
        );
    }

    // Parse from CSV row
    public static Reservation fromCsvRow(String row) {
        String[] parts = row.split(",", -1);
        if (parts.length < 5) return null;
        try {
            return new Reservation(
                    parts[0].trim(),
                    parts[1].trim(),
                    Integer.parseInt(parts[2].trim()),
                    Integer.parseInt(parts[3].trim()),
                    Status.valueOf(parts[4].trim())
            );
        } catch (Exception e) {
            return null;
        }
    }
}
