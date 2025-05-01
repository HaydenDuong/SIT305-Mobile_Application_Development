package com.example.itubeapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.apache.commons.codec.digest.DigestUtils;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String fullName;
    private String userName;
    private String password;

    // Method for hashing password for newly created User-account
    private String hashPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }

    // Constructor for class User
    public User(String fullName, String userName, String password) {
        this.fullName = fullName;
        this.userName = userName;
        this.password = hashPassword(password);
    }

    public User() {

    }

    // Getters
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setPassword(String password) { this.password = hashPassword(password); }
}
