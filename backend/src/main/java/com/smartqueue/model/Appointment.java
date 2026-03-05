package com.smartqueue.model;

import java.time.OffsetDateTime;

public class Appointment {
    private int id;
    private String name;
    private String phone;
    private TokenStatus status;
    private OffsetDateTime createdAt;

    public Appointment(int id, String name, String phone, TokenStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public TokenStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

