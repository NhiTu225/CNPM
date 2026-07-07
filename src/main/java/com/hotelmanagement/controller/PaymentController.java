package com.hotelmanagement.controller;

import com.hotelmanagement.dao.BookingDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.dao.SystemLogDAO;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PaymentController {

    private User currentUser;
    private Room selectedRoom;
    private List<Room> selectedRooms;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice;
    private CustomerController parentController;
    private boolean isProcessing = false;

    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @FXML
    private Label lblInvRoom;
    @FXML
    private Label lblInvNights;
    @FXML
    private Label lblInvTotal;

    @FXML
    private ComboBox<String> ddlPaymentMethod;

    @FXML
    private VBox panelQR;
    @FXML
    private ImageView imgQRCode;

    @FXML
    private VBox panelVisa;
    @FXML
    private TextField txtCardNumber;
    @FXML
    private TextField txtCardHolder;
    @FXML
    private TextField txtCardExpiry;
    @FXML
    private PasswordField txtCardCvv;

    @FXML
    public void initialize() {
        ddlPaymentMethod.setItems(FXCollections.observableArrayList(
                "Ứng dụng Quét mã QR (VNPAY / Momo)",
                "Thẻ nội địa ATM / Thẻ Quốc tế Visa"
        ));
        ddlPaymentMethod.setValue("Ứng dụng Quét mã QR (VNPAY / Momo)");
        
        // Cấu hình ban đầu hiển thị QR
        panelQR.setVisible(true);
        panelQR.setManaged(true);
        panelVisa.setVisible(false);
        panelVisa.setManaged(false);
    }

    public void setBookingDetails(User customer, Room room, LocalDate checkIn, LocalDate checkOut, double total, CustomerController parent) {
        setBookingDetails(customer, java.util.Collections.singletonList(room), checkIn, checkOut, total, parent);
    }

    public void setBookingDetails(User customer, List<Room> rooms, LocalDate checkIn, LocalDate checkOut, double total, CustomerController parent) {
        this.currentUser = customer;
        this.selectedRooms = rooms;
        if (rooms != null && !rooms.isEmpty()) {
            this.selectedRoom = rooms.get(0);
        }
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.totalPrice = total;
        this.parentController = parent;

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) nights = 1;

        StringBuilder sb = new StringBuilder();
        if (rooms != null) {
            for (int i = 0; i < rooms.size(); i++) {
                sb.append(rooms.get(i).getRoomNumber());
                if (i < rooms.size() - 1) sb.append(", ");
            }
        }

        lblInvRoom.setText(rooms.get(0).getRoomType() + " (Phòng " + sb.toString() + ")");
        lblInvNights.setText(nights + " đêm (" + checkIn + " -> " + checkOut + ")");
        lblInvTotal.setText(String.format("%,.0f ₫", total));

        // Tải mã QR động từ API dựa trên mã phòng và giá tiền
        String qrUrl = String.format("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=GrandLuxuryHotel_Room_%s_Amount_%.0f", sb.toString(), total);
        try {
            imgQRCode.setImage(new Image(qrUrl, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleMethodChange(ActionEvent event) {
        String method = ddlPaymentMethod.getValue();
        if (method.contains("QR")) {
            panelQR.setVisible(true);
            panelQR.setManaged(true);
            panelVisa.setVisible(false);
            panelVisa.setManaged(false);
        } else {
            panelQR.setVisible(false);
            panelQR.setManaged(false);
            panelVisa.setVisible(true);
            panelVisa.setManaged(true);
        }
    }

    @FXML
    void handlePayment(ActionEvent event) {
        if (isProcessing) return;
        isProcessing = true;

        String method = ddlPaymentMethod.getValue();
        
        // Nếu thanh toán qua Visa, thực hiện kiểm tra biểu mẫu (Form Validation)
        if (method.contains("Visa")) {
            String cardNumber = txtCardNumber.getText().trim();
            String cardHolder = txtCardHolder.getText().trim();
            String cardExpiry = txtCardExpiry.getText().trim();
            String cardCvv = txtCardCvv.getText().trim();

            if (cardNumber.isEmpty() || cardHolder.isEmpty() || cardExpiry.isEmpty() || cardCvv.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin thẻ thanh toán.");
                isProcessing = false;
                return;
            }
        }

        boolean success = true;
        String paymentMethodText = method.contains("QR") ? "Mã QR trực tuyến" : "Thẻ Visa/ATM";

        for (Room r : selectedRooms) {
            Booking booking = new Booking();
            booking.setUserId(currentUser.getId());
            booking.setRoomId(r.getId());
            booking.setCheckIn(checkInDate);
            booking.setCheckOut(checkOutDate);
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            if (nights <= 0) nights = 1;
            booking.setTotal(r.getPrice() * nights);
            booking.setStatus("BOOKED");

            if (bookingDAO.addBooking(booking)) {
                roomDAO.updateRoomStatus(r.getId(), "BOOKED");
            } else {
                success = false;
            }
        }

        if (success) {
            SystemLogDAO.addLog(currentUser.getEmail(), String.format("Đặt phòng trực tuyến thành công: %d phòng loại %s, Tổng thanh toán %.0fđ qua %s", 
                    selectedRooms.size(), selectedRooms.get(0).getRoomType(), totalPrice, paymentMethodText));

            showAlert(Alert.AlertType.INFORMATION, "Thanh toán thành công", "Đặt phòng thành công! " + selectedRooms.size() + " phòng đã được khóa giữ chỗ an toàn.");
            closeDialog();
            if (parentController != null) {
                parentController.onBookingSuccess();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi đặt phòng", "Không thể thực hiện lưu lịch sử đặt phòng. Vui lòng thử lại.");
            isProcessing = false;
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) lblInvRoom.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
