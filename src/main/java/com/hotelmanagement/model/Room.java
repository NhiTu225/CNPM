package com.hotelmanagement.model;

public class Room {

    private int id;
    private String roomNumber;
    private String roomName;
    private String roomType;
    private double price;
    private String image;
    private String status;

    public Room() {
    }

    public Room(int id, String roomNumber, String roomName, String roomType,
                double price, String image, String status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomName = roomName;
        this.roomType = roomType;
        this.price = price;
        this.image = image;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}