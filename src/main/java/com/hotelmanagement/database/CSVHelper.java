package com.hotelmanagement.database;

import com.hotelmanagement.model.Room;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVHelper {

    public static List<Room> readRoomsFromCSV(File file) throws IOException {
        List<Room> rooms = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            // Bỏ qua dòng tiêu đề (header)
            String header = br.readLine();
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // Cắt theo dấu phẩy (ở đây giả sử dữ liệu không chứa dấu phẩy bên trong dấu ngoặc kép)
                String[] data = line.split(",");
                if (data.length >= 5) {
                    Room room = new Room();
                    // Nếu có ID trong file CSV (Ví dụ xuất ra rồi nhập lại), ta bỏ qua ID để tự sinh trong CSDL,
                    // hoặc nếu format là: RoomNumber,RoomName,RoomType,Price,Image,Status (6 cột)
                    // Hoặc: Id,RoomNumber,RoomName,RoomType,Price,Image,Status (7 cột)
                    int indexOffset = 0;
                    if (data.length == 7) {
                        try {
                            room.setId(Integer.parseInt(data[0].trim()));
                        } catch (NumberFormatException e) {
                            // ID không hợp lệ thì coi như thêm mới
                        }
                        indexOffset = 1;
                    }
                    
                    room.setRoomNumber(data[indexOffset].trim());
                    room.setRoomName(data[indexOffset + 1].trim());
                    room.setRoomType(data[indexOffset + 2].trim());
                    room.setPrice(Double.parseDouble(data[indexOffset + 3].trim()));
                    room.setImage(data[indexOffset + 4].trim());
                    
                    if (data.length > indexOffset + 5) {
                        room.setStatus(data[indexOffset + 5].trim());
                    } else {
                        room.setStatus("AVAILABLE");
                    }
                    
                    rooms.add(room);
                }
            }
        }
        return rooms;
    }

    public static void writeRoomsToCSV(File file, List<Room> rooms) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            // Viết dòng tiêu đề
            bw.write("Id,RoomNumber,RoomName,RoomType,Price,Image,Status");
            bw.newLine();
            
            for (Room r : rooms) {
                String line = String.format("%d,%s,%s,%s,%.2f,%s,%s",
                        r.getId(),
                        escapeSpecialCharacters(r.getRoomNumber()),
                        escapeSpecialCharacters(r.getRoomName()),
                        escapeSpecialCharacters(r.getRoomType()),
                        r.getPrice(),
                        escapeSpecialCharacters(r.getImage()),
                        r.getStatus()
                );
                bw.write(line);
                bw.newLine();
            }
        }
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("'")) {
            escapedData = escapedData.replace("\"", "\"\"");
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}
