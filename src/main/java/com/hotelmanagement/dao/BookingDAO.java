package com.hotelmanagement.dao;

import com.hotelmanagement.database.DBConnection;
import com.hotelmanagement.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public boolean addBooking(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, room_id, check_in, check_out, total, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getRoomId());
            ps.setDate(3, java.sql.Date.valueOf(booking.getCheckIn()));
            ps.setDate(4, java.sql.Date.valueOf(booking.getCheckOut()));
            ps.setDouble(5, booking.getTotal());
            ps.setString(6, booking.getStatus() != null ? booking.getStatus() : "BOOKED");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
                SELECT b.id, b.user_id, b.room_id, b.check_in, b.check_out, b.total, b.status, 
                       u.email AS customer_email, r.room_number 
                FROM bookings b
                LEFT JOIN users u ON b.user_id = u.id
                LEFT JOIN rooms r ON b.room_id = r.id
                ORDER BY b.id DESC
                """;
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("id"));
                booking.setUserId(rs.getInt("user_id"));
                booking.setRoomId(rs.getInt("room_id"));
                booking.setCheckIn(rs.getDate("check_in").toLocalDate());
                booking.setCheckOut(rs.getDate("check_out").toLocalDate());
                booking.setTotal(rs.getDouble("total"));
                booking.setStatus(rs.getString("status"));
                booking.setCustomerEmail(rs.getString("customer_email"));
                booking.setRoomNumber(rs.getString("room_number"));
                bookings.add(booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, status);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Booking> getBookingsByUserId(int userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
                SELECT b.id, b.user_id, b.room_id, b.check_in, b.check_out, b.total, b.status, 
                       u.email AS customer_email, r.room_number 
                FROM bookings b
                LEFT JOIN users u ON b.user_id = u.id
                LEFT JOIN rooms r ON b.room_id = r.id
                WHERE b.user_id = ?
                ORDER BY b.id DESC
                """;
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking booking = new Booking();
                    booking.setId(rs.getInt("id"));
                    booking.setUserId(rs.getInt("user_id"));
                    booking.setRoomId(rs.getInt("room_id"));
                    booking.setCheckIn(rs.getDate("check_in").toLocalDate());
                    booking.setCheckOut(rs.getDate("check_out").toLocalDate());
                    booking.setTotal(rs.getDouble("total"));
                    booking.setStatus(rs.getString("status"));
                    booking.setCustomerEmail(rs.getString("customer_email"));
                    booking.setRoomNumber(rs.getString("room_number"));
                    bookings.add(booking);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }
}