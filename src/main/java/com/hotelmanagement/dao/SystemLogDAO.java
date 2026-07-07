package com.hotelmanagement.dao;

import com.hotelmanagement.database.DBConnection;
import com.hotelmanagement.model.SystemLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SystemLogDAO {

    public static void addLog(String userEmail, String action) {
        String sql = "INSERT INTO system_logs (user_email, action) VALUES (?, ?)";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, userEmail != null ? userEmail : "SYSTEM");
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SystemLog> getAllLogs() {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_logs ORDER BY timestamp DESC";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                SystemLog log = new SystemLog();
                log.setId(rs.getInt("id"));
                log.setUserEmail(rs.getString("user_email"));
                log.setAction(rs.getString("action"));
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) {
                    log.setTimestamp(ts.toLocalDateTime());
                }
                logs.add(log);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    public List<SystemLog> getLogsByUserEmail(String email) {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_logs WHERE user_email = ? ORDER BY timestamp DESC";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SystemLog log = new SystemLog();
                    log.setId(rs.getInt("id"));
                    log.setUserEmail(rs.getString("user_email"));
                    log.setAction(rs.getString("action"));
                    java.sql.Timestamp ts = rs.getTimestamp("timestamp");
                    if (ts != null) {
                        log.setTimestamp(ts.toLocalDateTime());
                    }
                    logs.add(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }
}
