package com.reservationstudio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class User {

    // =========================
    // PERSON FIELDS (merged)
    // =========================
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;

    // =========================
    // USER FIELDS
    // =========================
    private final String username;
    private final String password;

    // =========================
    // CONSTRUCTOR
    // =========================
    public User(String firstName, String lastName, String email,
                String phoneNumber, String username, String password) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password;
    }



    // =========================
    // CSV AUTHENTICATION
    // =========================
    public static User authenticate(String username, String password) {

        String filePath = "src/main/Data/Users.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;

            while ((line = br.readLine()) != null) {

                String[] parts = line.split(",");

                if (parts.length < 6) continue;

                String firstName = parts[0].trim();
                String lastName = parts[1].trim();
                String email = parts[2].trim();
                String phone = parts[3].trim();
                String fileUsername = parts[4].trim();
                String filePassword = parts[5].trim();

                if (fileUsername.equalsIgnoreCase(username.trim())
                        && filePassword.equals(password.trim())) {

                    return new User(firstName, lastName, email, phone, fileUsername, filePassword);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading users.csv: " + e.getMessage());
        }

        return null;
    }

    @Override
    public String toString() {
        return "First Name= " + firstName +
                ", Last Name= " + lastName +
                ", Email= " + email +
                ", Phone Number= " + phoneNumber +
                ", Username= " + username;
    }
}