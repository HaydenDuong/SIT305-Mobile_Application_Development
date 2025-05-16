package com.example.lostfoundapp.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "lost_found_table")
public class Item {

    @PrimaryKey(autoGenerate = true)
    private final int id;
    private final String type;
    private final String name;
    private final String phoneNumber;
    private final String description;
    private final Long date;
    private final String location;
    private final Double latitude;
    private final Double longitude;

    // Constructor
    public Item(int id, String type, String name, String phoneNumber, String description, Long date, String location, Double latitude, Double longitude) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.date = date;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public Item(String type, String name, String phoneNumber, String description, Long date, String location, Double latitude, Double longitude) {
        this(0, type, name, phoneNumber, description, date, location, latitude, longitude);
    }

    // Getters
    public int getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDescription() { return description; }
    public Long getDate() { return date; }
    public String getLocation() { return location; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
