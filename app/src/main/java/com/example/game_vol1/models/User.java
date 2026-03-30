package com.example.game_vol1.models;

public class User {

    // 1. Полета (Характеристики на потребителя)
    private String id; // Уникален ID от Firebase
    private String username;
    private String email;
    private int totalPoints;
    private String factionId; // Към кой отбор принадлежи (Траки, Славяни, Прабългари)

    // 2. Празен конструктор (Задължителен за Firebase)
    public User() {
    }

    // 3. Конструктор за създаване на нов играч
    public User(String id, String username, String email, int totalPoints, String factionId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.totalPoints = totalPoints;
        this.factionId = factionId;
    }

    // 4. Getters и Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public String getFactionId() { return factionId; }
    public void setFactionId(String factionId) { this.factionId = factionId; }

    // 5. Метод за добавяне на точки (Бизнес логика директно в модела)
    public void addPoints(int points) {
        this.totalPoints += points;
    }
}