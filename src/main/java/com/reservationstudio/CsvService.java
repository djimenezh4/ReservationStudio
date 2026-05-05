package com.reservationstudio;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CsvService {

    private static final String RESERVATIONS_HEADER = "name,time,guests,tableNumber,status";
    private static final String SERVERS_HEADERS = "firstName,lastName";
    private static final String ASSIGNMENTS_HEADERS = "tableNumber,serverName";

    private final Path filePath;
    private final Path serversPath;
    private final Path assignmentsPath;

    public CsvService(String reservationsFileName) {
        this.filePath = Paths.get(reservationsFileName);
        Path dataDir = this.filePath.getParent();
        this.serversPath = dataDir.resolve("Servers.csv");
        this.assignmentsPath = dataDir.resolve("ServerAssignments.csv");
    }

    // Load all reservations from the CSV file, creates  file if it doesn't exist
    public List<Reservation> loadAll() {
        List<Reservation> list = new ArrayList<>();
        if (!Files.exists(filePath)) {
            // Create file with header
            try {
                Files.writeString(filePath, RESERVATIONS_HEADER + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;
        }
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                line = line.trim();
                if (line.isEmpty()) continue;
                Reservation r = Reservation.fromCsvRow(line);
                if (r != null) list.add(r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Save the full list to the CSV file. */
    public void saveAll(List<Reservation> reservations) {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(filePath))) {
            pw.println(RESERVATIONS_HEADER);
            for (Reservation r : reservations) {
                pw.println(r.toCsvRow());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Append single reservation to CSV
    public void append(Reservation r) {
        // If file doesn't exist yet, write header first
        boolean needsHeader = !Files.exists(filePath);
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath.toFile(), true))) {
            if (needsHeader) pw.println(RESERVATIONS_HEADER);
            pw.println(r.toCsvRow());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Server> loadServers() {
        List<Server> list = new ArrayList<>();
        if (!Files.exists(serversPath)) {
            try{
                Files.writeString(serversPath, SERVERS_HEADERS + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;
        }
        try (BufferedReader br = Files.newBufferedReader(serversPath)){
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;
                Server s = Server.fromCsvRow(line);
                if (s != null){
                    list.add(s);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return list;
    }

    public Map<Integer, String> loadAssignments() {
        Map<Integer, String> map = new LinkedHashMap<>();
        if (!Files.exists(assignmentsPath)) {
            try{
                Files.writeString(assignmentsPath, ASSIGNMENTS_HEADERS + System.lineSeparator());
            } catch (IOException e){
                e.printStackTrace();
            }
            return map;
        }
        try (BufferedReader br = Files.newBufferedReader(assignmentsPath)){
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null){
                if (firstLine) {
                    firstLine = false; continue;
                }
                line = line.trim();

                if(line.isEmpty()){
                    continue;
                }
                String[] parts = line.split(",", 2);
                if(parts.length < 2){
                    continue;
                }
                try{
                    int tableNum = Integer.parseInt(parts[0].trim());
                    String srvName = parts[1].trim();
                    map.put(tableNum, srvName);
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return map;
    }

    public void saveAssignments(Map<Integer, String> assignments){
        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(assignmentsPath))){
            pw.println(ASSIGNMENTS_HEADERS);
            for (Map.Entry<Integer, String> entry : assignments.entrySet()){
                pw.println(entry.getKey() + "," + entry.getValue());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
