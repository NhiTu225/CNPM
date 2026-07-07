package com.hotelmanagement.model;

import java.time.LocalDateTime;

public class Invoice {

    private int id;
    private int bookingId;
    private LocalDateTime invoiceDate;
    private double total;

    public Invoice() {
    }

    public Invoice(int id, int bookingId, LocalDateTime invoiceDate, double total) {
        this.id = id;
        this.bookingId = bookingId;
        this.invoiceDate = invoiceDate;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public double getTotal() {
        return total;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}