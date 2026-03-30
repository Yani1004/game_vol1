package com.example.game_vol1.models; // Увери се, че тук е твоето име на пакета!

public class POI {

    // 1. Полета (Характеристики на обекта) - Правим ги private заради принципа Енкапсулация в ООП
    private String id;
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private String arModelUrl;
    private String controllingFaction;

    // 2. Конструктор (Празен конструктор е нужен за Firebase по-късно)
    public POI() {
    }

    // 3. Конструктор с параметри (За да създаваме обекти лесно)
    public POI(String id, String title, String description, double latitude, double longitude, String arModelUrl, String controllingFaction) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.arModelUrl = arModelUrl;
        this.controllingFaction = controllingFaction;
    }

    // 4. Getters и Setters (Методи за достъп до данните)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getArModelUrl() { return arModelUrl; }
    public void setArModelUrl(String arModelUrl) { this.arModelUrl = arModelUrl; }

    public String getControllingFaction() { return controllingFaction; }
    public void setControllingFaction(String controllingFaction) { this.controllingFaction = controllingFaction; }
}