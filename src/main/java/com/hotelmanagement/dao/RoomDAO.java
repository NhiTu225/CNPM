package com.hotelmanagement.dao;

import com.hotelmanagement.database.DBConnection;
import com.hotelmanagement.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomDAO {

    public RoomDAO() {
        ensureStatusConstraint();
        populateSampleRoomsIfEmpty();
    }

    private void ensureStatusConstraint() {
        String sql = """
            IF EXISTS (
                SELECT * FROM sys.check_constraints 
                WHERE parent_object_id = OBJECT_ID('rooms') 
                  AND definition LIKE '%status%' 
                  AND definition NOT LIKE '%REPAIRING%'
            )
            BEGIN
                DECLARE @ConstraintName NVARCHAR(200);
                SELECT @ConstraintName = name 
                FROM sys.check_constraints 
                WHERE parent_object_id = OBJECT_ID('rooms') 
                  AND definition LIKE '%status%';
                
                IF @ConstraintName IS NOT NULL
                BEGIN
                    EXEC('ALTER TABLE rooms DROP CONSTRAINT ' + @ConstraintName);
                END
                
                ALTER TABLE rooms ADD CONSTRAINT CK_rooms_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'OCCUPIED', 'REPAIRING', 'MAINTENANCE'));
            END
            """;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            // Ràng buộc đã có hoặc lỗi không nghiêm trọng, có thể bỏ qua
        }
    }

    private void populateSampleRoomsIfEmpty() {
        // Check xem đã cập nhật đống ảnh Unsplash mới chưa (lấy phòng 101 làm mốc check)
        String checkSql = "SELECT COUNT(*) FROM rooms WHERE room_number = '101' AND image LIKE '%photo-1505691938895%'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Reset sạch dữ liệu cũ để tránh lỗi ràng buộc khóa ngoại
                stmt.execute("DELETE FROM invoices");
                stmt.execute("DELETE FROM payments");
                stmt.execute("DELETE FROM bookings");
                stmt.execute("DELETE FROM rooms");
                
                // Tạo 45 phòng mẫu (15 loại phòng khác nhau, mỗi loại gồm 3 phòng giống nhau)
                String insertSql = """
                    INSERT INTO rooms (room_number, room_name, room_type, price, image, status)
                    VALUES
                        -- STANDARD (5 loại, mỗi loại 3 phòng)
                        -- 1. Standard Cozy Single (101, 102, 103)
                        ('101', N'Standard Cozy Single', N'Standard', 500000.00, 'https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('102', N'Standard Cozy Single', N'Standard', 500000.00, 'https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('103', N'Standard Cozy Single', N'Standard', 500000.00, 'https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        
                        -- 2. Standard Twin Classic (104, 105, 106)
                        ('104', N'Standard Twin Classic', N'Standard', 600000.00, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('105', N'Standard Twin Classic', N'Standard', 600000.00, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('106', N'Standard Twin Classic', N'Standard', 600000.00, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 3. Standard Double Garden (107, 108, 109)
                        ('107', N'Standard Double Garden', N'Standard', 700000.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('108', N'Standard Double Garden', N'Standard', 700000.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('109', N'Standard Double Garden', N'Standard', 700000.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 4. Standard Triple Suite (110, 111, 112)
                        ('110', N'Standard Triple Suite', N'Standard', 850000.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('111', N'Standard Triple Suite', N'Standard', 850000.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('112', N'Standard Triple Suite', N'Standard', 850000.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 5. Standard Family Oasis (113, 114, 115)
                        ('113', N'Standard Family Oasis', N'Standard', 1000000.00, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('114', N'Standard Family Oasis', N'Standard', 1000000.00, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('115', N'Standard Family Oasis', N'Standard', 1000000.00, 'https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- DELUXE (5 loại, mỗi loại 3 phòng)
                        -- 6. Deluxe Double View (201, 202, 203)
                        ('201', N'Deluxe Double View', N'Deluxe', 1200000.00, 'https://images.unsplash.com/photo-1611891405110-5a30d2527000?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('202', N'Deluxe Double View', N'Deluxe', 1200000.00, 'https://images.unsplash.com/photo-1611891405110-5a30d2527000?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('203', N'Deluxe Double View', N'Deluxe', 1200000.00, 'https://images.unsplash.com/photo-1611891405110-5a30d2527000?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 7. Deluxe King Premium (204, 205, 206)
                        ('204', N'Deluxe King Premium', N'Deluxe', 1400000.00, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('205', N'Deluxe King Premium', N'Deluxe', 1400000.00, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('206', N'Deluxe King Premium', N'Deluxe', 1400000.00, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 8. Deluxe Twin Ocean (207, 208, 209)
                        ('207', N'Deluxe Twin Ocean', N'Deluxe', 1500000.00, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('208', N'Deluxe Twin Ocean', N'Deluxe', 1500000.00, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('209', N'Deluxe Twin Ocean', N'Deluxe', 1500000.00, 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 9. Deluxe Garden Bungalow (210, 211, 212)
                        ('210', N'Deluxe Garden Bungalow', N'Deluxe', 1600000.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('211', N'Deluxe Garden Bungalow', N'Deluxe', 1600000.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('212', N'Deluxe Garden Bungalow', N'Deluxe', 1600000.00, 'https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 10. Deluxe Honeymoon Suite (213, 214, 215)
                        ('213', N'Deluxe Honeymoon Suite', N'Deluxe', 1800000.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('214', N'Deluxe Honeymoon Suite', N'Deluxe', 1800000.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('215', N'Deluxe Honeymoon Suite', N'Deluxe', 1800000.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- EXECUTIVE (5 loại, mỗi loại 3 phòng)
                        -- 11. Executive Suite VIP (301, 302, 303)
                        ('301', N'Executive Suite VIP', N'Executive', 2500000.00, 'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('302', N'Executive Suite VIP', N'Executive', 2500000.00, 'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('303', N'Executive Suite VIP', N'Executive', 2500000.00, 'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 12. Executive Sky Palace (304, 305, 306)
                        ('304', N'Executive Sky Palace', N'Executive', 3000000.00, 'https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('305', N'Executive Sky Palace', N'Executive', 3000000.00, 'https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('306', N'Executive Sky Palace', N'Executive', 3000000.00, 'https://images.unsplash.com/photo-1540518614846-7eded433c457?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 13. Executive Presidential Suite (307, 308, 309)
                        ('307', N'Executive Presidential Suite', N'Executive', 4500000.00, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('308', N'Executive Presidential Suite', N'Executive', 4500000.00, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('309', N'Executive Presidential Suite', N'Executive', 4500000.00, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 14. Executive Royal Suite (310, 311, 312)
                        ('310', N'Executive Royal Suite', N'Executive', 3800000.00, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('311', N'Executive Royal Suite', N'Executive', 3800000.00, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('312', N'Executive Royal Suite', N'Executive', 3800000.00, 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),

                        -- 15. Executive Terrace Club (313, 314, 315)
                        ('313', N'Executive Terrace Club', N'Executive', 2800000.00, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('314', N'Executive Terrace Club', N'Executive', 2800000.00, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE'),
                        ('315', N'Executive Terrace Club', N'Executive', 2800000.00, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=600&q=80', 'AVAILABLE');
                    """;
                stmt.execute(insertSql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setRoomName(rs.getString("room_name"));
                room.setRoomType(rs.getString("room_type"));
                room.setPrice(rs.getDouble("price"));
                room.setImage(rs.getString("image"));
                room.setStatus(rs.getString("status"));
                rooms.add(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_name, room_type, price, image, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomName());
            ps.setString(3, room.getRoomType());
            ps.setDouble(4, room.getPrice());
            ps.setString(5, room.getImage());
            ps.setString(6, room.getStatus() != null ? room.getStatus() : "AVAILABLE");
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_number = ?, room_name = ?, room_type = ?, price = ?, image = ?, status = ? WHERE id = ?";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomName());
            ps.setString(3, room.getRoomType());
            ps.setDouble(4, room.getPrice());
            ps.setString(5, room.getImage());
            ps.setString(6, room.getStatus());
            ps.setInt(7, room.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, Integer> getRoomStatusCounts() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS count FROM rooms GROUP BY status";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return counts;
    }

    public boolean updateRoomStatus(int roomId, String status) {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}