package com.hotelmanagement.dao;

import com.hotelmanagement.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class InvoiceDAO {

    public boolean addInvoice(int bookingId, double total) {
        String sql = "INSERT INTO invoices (booking_id, total) VALUES (?, ?)";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, bookingId);
            ps.setDouble(2, total);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, Double> getRevenueByMonth() {
        Map<String, Double> revenue = new LinkedHashMap<>();
        // SQL Server query: CONVERT(VARCHAR(7), invoice_date, 120) yields 'yyyy-MM'
        String sql = """
                SELECT CONVERT(VARCHAR(7), invoice_date, 120) AS Month, SUM(total) AS Total
                FROM invoices
                GROUP BY CONVERT(VARCHAR(7), invoice_date, 120)
                ORDER BY Month ASC
                """;
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                revenue.put(rs.getString("Month"), rs.getDouble("Total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revenue;
    }

    public Map<String, Double> getRevenueByDay() {
        Map<String, Double> revenue = new LinkedHashMap<>();
        // SQL Server query: CONVERT(VARCHAR(10), invoice_date, 120) yields 'yyyy-MM-dd'
        String sql = """
                SELECT CONVERT(VARCHAR(10), invoice_date, 120) AS Day, SUM(total) AS Total
                FROM invoices
                GROUP BY CONVERT(VARCHAR(10), invoice_date, 120)
                ORDER BY Day ASC
                """;
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                revenue.put(rs.getString("Day"), rs.getDouble("Total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return revenue;
    }
}
