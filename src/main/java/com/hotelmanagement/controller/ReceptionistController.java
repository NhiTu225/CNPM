package com.hotelmanagement.controller;

import com.hotelmanagement.dao.BookingDAO;
import com.hotelmanagement.dao.InvoiceDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.dao.SystemLogDAO;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.User;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.database.CSVHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ReceptionistController {

    private User currentUser;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    @FXML
    private Label lblWelcome;

    @FXML
    private TableView<Booking> tblBookings;
    @FXML
    private TableColumn<Booking, Integer> colBookingId;
    @FXML
    private TableColumn<Booking, String> colBookingCustomer;
    @FXML
    private TableColumn<Booking, String> colBookingRoom;
    @FXML
    private TableColumn<Booking, String> colBookingCheckIn;
    @FXML
    private TableColumn<Booking, String> colBookingCheckOut;
    @FXML
    private TableColumn<Booking, Double> colBookingTotal;
    @FXML
    private TableColumn<Booking, String> colBookingStatus;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        lblWelcome.setText("Welcome, " + user.getFullName() + " (" + user.getRole() + ")!");
    }

    @FXML
    public void initialize() {
        // Cấu hình các cột của bảng Đặt phòng
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBookingCustomer.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        colBookingRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colBookingCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colBookingCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

        // Format lại tiền tệ (thêm dấu phẩy phân cách nghìn và chữ đ) để tránh lỗi hiện
        // kiểu số mũ e+06
        colBookingTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colBookingTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f ₫", item));
                }
            }
        });

        colBookingStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Tải danh sách đặt phòng khi mở màn hình lễ tân
        refreshData();
    }

    private void refreshData() {
        loadBookings();
    }

    private void loadBookings() {
        List<Booking> bookings = bookingDAO.getAllBookings();
        tblBookings.setItems(FXCollections.observableArrayList(bookings));
    }

    @FXML
    void handleCheckIn(ActionEvent event) {
        Booking booking = tblBookings.getSelectionModel().getSelectedItem();
        if (booking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một lượt đặt phòng để Check-in.");
            return;
        }

        if (!booking.getStatus().equals("BOOKED")) {
            showAlert(Alert.AlertType.WARNING, "Trạng thái không hợp lệ",
                    "Chỉ có thể check-in phòng ở trạng thái BOOKED.");
            return;
        }

        if (bookingDAO.updateBookingStatus(booking.getId(), "CHECKED_IN")) {
            roomDAO.updateRoomStatus(booking.getRoomId(), "OCCUPIED");
            SystemLogDAO.addLog(getCurrentUserEmail(),
                    "Check-in thành công cho booking ID: " + booking.getId() + ", Phòng ID: " + booking.getRoomId());
            showAlert(Alert.AlertType.INFORMATION, "Check-in thành công", "Đã thực hiện check-in cho khách.");
            refreshData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thực hiện Check-in.");
        }
    }

    @FXML
    void handleCheckOut(ActionEvent event) {
        Booking booking = tblBookings.getSelectionModel().getSelectedItem();
        if (booking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một lượt đặt phòng để Check-out.");
            return;
        }

        if (!booking.getStatus().equals("CHECKED_IN")) {
            showAlert(Alert.AlertType.WARNING, "Trạng thái không hợp lệ",
                    "Chỉ có thể check-out phòng ở trạng thái CHECKED_IN.");
            return;
        }

        // Cập nhật Trạng thái Đặt phòng -> CHECKED_OUT
        if (bookingDAO.updateBookingStatus(booking.getId(), "CHECKED_OUT")) {
            // Cập nhật trạng thái Phòng trở lại -> AVAILABLE
            roomDAO.updateRoomStatus(booking.getRoomId(), "AVAILABLE");
            // Tạo Hóa đơn (FR07)
            invoiceDAO.addInvoice(booking.getId(), booking.getTotal());

            SystemLogDAO.addLog(getCurrentUserEmail(), "Check-out & Tạo hóa đơn thành công cho booking ID: "
                    + booking.getId() + ", Tổng tiền: " + booking.getTotal());
            showAlert(Alert.AlertType.INFORMATION, "Check-out thành công",
                    "Đã Check-out. Hệ thống đã tự động xuất hóa đơn trị giá "
                            + String.format("%,.0f", booking.getTotal()) + " VND.");
            refreshData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thực hiện Check-out.");
        }
    }

    @FXML
    void handleCancelBooking(ActionEvent event) {
        Booking booking = tblBookings.getSelectionModel().getSelectedItem();
        if (booking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một lượt đặt phòng để Hủy.");
            return;
        }

        if (booking.getStatus().equals("CHECKED_OUT") || booking.getStatus().equals("CANCELLED")) {
            showAlert(Alert.AlertType.WARNING, "Trạng thái không hợp lệ",
                    "Không thể hủy lượt đặt phòng đã check-out hoặc đã hủy.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc chắn muốn hủy đặt phòng ID: " + booking.getId() + " không?", ButtonType.YES,
                ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            if (bookingDAO.updateBookingStatus(booking.getId(), "CANCELLED")) {
                roomDAO.updateRoomStatus(booking.getRoomId(), "AVAILABLE");
                SystemLogDAO.addLog(getCurrentUserEmail(), "Hủy đặt phòng ID " + booking.getId());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy lịch đặt phòng thành công.");
                refreshData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy lịch đặt phòng.");
            }
        }
    }

    @FXML
    void logout(ActionEvent event) {
        SystemLogDAO.addLog(getCurrentUserEmail(), "Lễ tân đăng xuất khỏi hệ thống");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Hotel Management System - Login");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "receptionist@gmail.com";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void importCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file CSV để nhập dữ liệu phòng");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        Stage stage = (Stage) tblBookings.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                List<Room> rooms = CSVHelper.readRoomsFromCSV(file);
                int count = 0;
                for (Room r : rooms) {
                    if (roomDAO.addRoom(r)) {
                        count++;
                    }
                }
                SystemLogDAO.addLog(getCurrentUserEmail(),
                        "Lễ tân nhập thành công " + count + " phòng từ file CSV: " + file.getName());
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Đã nhập thành công " + count + " phòng vào cơ sở dữ liệu.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập file", "Đã xảy ra lỗi khi đọc file CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    void exportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn nơi lưu file CSV phòng");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("rooms_export.csv");
        Stage stage = (Stage) tblBookings.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                List<Room> rooms = roomDAO.getAllRooms();
                CSVHelper.writeRoomsToCSV(file, rooms);
                SystemLogDAO.addLog(getCurrentUserEmail(),
                        "Lễ tân xuất danh sách phòng ra file CSV: " + file.getName());
                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                        "Xuất file CSV thành công tại: " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi xuất file", "Không thể ghi file CSV: " + e.getMessage());
            }
        }
    }
}
