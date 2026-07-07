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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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

            // Lấy mã đặt phòng mới nhất vừa tạo để hiển thị trong popup xác nhận (FR11)
            List<Booking> latestBookings = bookingDAO.getBookingsByUserId(currentUser.getId());
            int newBookingId = latestBookings.isEmpty() ? 0 : latestBookings.get(0).getId();

            closeDialog();
            if (parentController != null) {
                parentController.onBookingSuccess();
            }

            // FR11: Hiển thị popup xác nhận chi tiết sau khi đặt phòng thành công
            showBookingConfirmationPopup(newBookingId, paymentMethodText);
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

    // FR11: Popup xác nhận đặt phòng chi tiết
    private void showBookingConfirmationPopup(int bookingId, String paymentMethod) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("✅ Xác Nhận Đặt Phòng Thành Công");

        VBox root = new VBox(14);
        root.setPadding(new Insets(28, 32, 24, 32));
        root.setStyle("-fx-background-color: #ffffff; -fx-font-family: 'Segoe UI';");
        root.setPrefWidth(420);
        root.setAlignment(Pos.TOP_LEFT);

        // Tiêu đề
        Label lblTitle = new Label("🎉  Đặt Phòng Thành Công!");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #276749;");

        Label lblSub = new Label("Chi tiết đơn đặt phòng của quý khách:");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #718096;");

        // Đường phân cách
        Separator sep1 = new Separator();

        // Xây dựng tên phòng
        StringBuilder roomNums = new StringBuilder();
        for (int i = 0; i < selectedRooms.size(); i++) {
            roomNums.append(selectedRooms.get(i).getRoomNumber());
            if (i < selectedRooms.size() - 1) roomNums.append(", ");
        }
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) nights = 1;

        // Các dòng thông tin
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-background-color: #f7fafc; -fx-padding: 14; -fx-background-radius: 8;");
        infoBox.getChildren().addAll(
            makeInfoRow("📋  Mã đặt phòng:",  bookingId > 0 ? "#" + bookingId : "(đang xử lý)"),
            makeInfoRow("🛏️  Loại phòng:",    selectedRooms.get(0).getRoomType()),
            makeInfoRow("🔢  Số phòng:",       roomNums.toString()),
            makeInfoRow("📅  Nhận phòng:",    checkInDate.toString()),
            makeInfoRow("📅  Trả phòng:",     checkOutDate.toString()),
            makeInfoRow("🌙  Số đêm:",        nights + " đêm"),
            makeInfoRow("💳  Thanh toán:",    paymentMethod),
            makeInfoRow("💰  Tổng tiền:",     String.format("%,.0f ₫", totalPrice))
        );

        Separator sep2 = new Separator();

        Label lblNote = new Label("⏰  Giờ nhận phòng: 14:00  |  Giờ trả phòng: 12:00");
        lblNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0; -fx-font-style: italic;");

        Button btnClose = new Button("Đã hiểu, Đóng lại");
        btnClose.setPrefWidth(Double.MAX_VALUE);
        btnClose.setPrefHeight(38);
        btnClose.setStyle("-fx-background-color: #276749; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 8;");
        btnClose.setOnAction(e -> popup.close());

        root.getChildren().addAll(lblTitle, lblSub, sep1, infoBox, sep2, lblNote, btnClose);

        Scene scene = new Scene(root);
        popup.setScene(scene);
        popup.setResizable(false);
        popup.centerOnScreen();
        popup.showAndWait();
    }

    private HBox makeInfoRow(String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setMinWidth(140);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568; -fx-font-weight: bold;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: #2d3748;");
        row.getChildren().addAll(lbl, val);
        return row;
    }
}
