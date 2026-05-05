package com.reservationstudio;

public class Server {

    private final String firstName;
    private final String lastName;

    public Server(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFullName(){
        return firstName + " " + lastName;
    }

    public static Server fromCsvRow(String row){
        String[] parts = row.split(",", -1);
        if (parts.length < 2) return null;
        return new Server(parts[0], parts[1].trim());
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
