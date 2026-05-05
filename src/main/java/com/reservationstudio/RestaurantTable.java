package com.reservationstudio;

public class RestaurantTable {

    public enum TableStatus { AVAILABLE, SEATED, RESERVED }

    private final int number;
    private final int capacity;
    private double layoutX;
    private double layoutY;
    private double width;
    private double height;

    public RestaurantTable(int number, int capacity, double x, double y, double w, double h) {
        this.number = number;
        this.capacity = capacity;
        this.layoutX = x;
        this.layoutY = y;
        this.width = w;
        this.height = h;
    }

    public int getNumber() { return number; }
    public int getCapacity() { return capacity; }
    public double getLayoutX() { return layoutX; }
    public double getLayoutY() { return layoutY; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}