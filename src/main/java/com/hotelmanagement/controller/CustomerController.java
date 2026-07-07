package com.hotelmanagement.controller;

import com.hotelmanagement.dao.BookingDAO;
import com.hotelmanagement.dao.NotificationDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.dao.SystemLogDAO;
import com.hotelmanagement.dao.UserDAO;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.Notification;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.SystemLog;
import com.hotelmanagement.model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CustomerController {

    private User currentUser;
    private final RoomDAO roomDAO = new RoomDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final SystemLogDAO logDAO = new SystemLogDAO();

    // Elements of Sidebar & Header
    @FXML
    private Circle circleAvatar;
    @FXML
    private Text avatarText;
    @FXML
    private Label lblCustomerName;

    // Sidebar navigation buttons
    @FXML
    private Button btnTabHome;
    @FXML
    private Button btnTabProfile;
    @FXML
    private Button btnTabPolicy;
    @FXML
    private Button btnTabHistory;
    @FXML
    private Button btnTabNotifications;
    @FXML
    private Button btnTabChangePassword;
    @FXML
    private Button btnTabAccount;

    // StackPane & Inner panels
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox panelHome;
    @FXML
    private VBox panelProfile;
    @FXML
    private VBox panelPolicy;
    @FXML
    private VBox panelHistory;
    @FXML
    private VBox panelNotifications;
    @FXML
    private VBox panelChangePassword;
    @FXML
    private VBox panelAccount;

    // Tab 1: Room Booking fields
    @FXML
    private DatePicker dpCheckIn;
    @FXML
    private DatePicker dpCheckOut;
    @FXML
    private ComboBox<String> cbFilterType;
    @FXML
    private ComboBox<String> cbFilterPriceRange;
    @FXML
    private FlowPane flowRooms;

    // Tab 2: Profile fields
    @FXML
    private TextField txtProfileEmail;
    @FXML
    private TextField txtProfileFullName;

    // Tab 4: Booking History table & fields
    @FXML
    private TableView<Booking> tableHistory;
    @FXML
    private TableColumn<Booking, Integer> colHistId;
    @FXML
    private TableColumn<Booking, String> colHistRoom;
    @FXML
    private TableColumn<Booking, LocalDate> colHistCheckIn;
    @FXML
    private TableColumn<Booking, LocalDate> colHistCheckOut;
    @FXML
    private TableColumn<Booking, Double> colHistTotal;
    @FXML
    private TableColumn<Booking, String> colHistStatus;
    @FXML
    private Button btnCancelBooking;

    // Tab 5: Notifications VBox list
    @FXML
    private VBox vboxNotifications;

    // Tab 6: Change password fields
    @FXML
    private PasswordField txtCurrentPassword;
    @FXML
    private PasswordField txtNewPassword;
    @FXML
    private PasswordField txtConfirmNewPassword;

    // Tab 7: Account Management logs table
    @FXML
    private TableView<SystemLog> tableLogs;
    @FXML
    private TableColumn<SystemLog, LocalDateTime> colLogTime;
    @FXML
    private TableColumn<SystemLog, String> colLogAction;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        lblCustomerName.setText(user.getEmail());
        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            avatarText.setText(String.valueOf(user.getFullName().charAt(0)).toUpperCase());
        } else {
            avatarText.setText("C");
        }
        // Gán dữ liệu hồ sơ cá nhân ban đầu
        txtProfileEmail.setText(user.getEmail());
        txtProfileFullName.setText(user.getFullName());
    }

    @FXML
    public void initialize() {
        // Thiết lập ngày mặc định
        dpCheckIn.setValue(LocalDate.now());
        dpCheckOut.setValue(LocalDate.now().plusDays(4));

        // Thiết lập các bộ lọc phòng
        cbFilterType.setItems(FXCollections.observableArrayList("Tất cả các phòng", "Standard", "Deluxe", "Executive"));
        cbFilterType.setValue("Tất cả các phòng");

        cbFilterPriceRange.setItems(FXCollections.observableArrayList("Tất cả mức giá", "Dưới 1,500,000 ₫",
                "1,500,000 ₫ - 2,500,000 ₫", "Trên 2,500,000 ₫"));
        cbFilterPriceRange.setValue("Tất cả mức giá");

        // Cấu hình các cột của bảng lịch sử đặt phòng
        colHistId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHistRoom.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colHistCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colHistCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colHistTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colHistStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cấu hình các cột của bảng log hoạt động cá nhân
        colLogTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        // Hiển thị danh sách phòng ban đầu
        displayRooms(roomDAO.getAllRooms());
    }

    // --- TAB SWITCHER LOGIC ---
    @FXML
    void handleTabSwitch(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Ẩn tất cả các panel
        panelHome.setVisible(false);
        panelHome.setManaged(false);
        panelProfile.setVisible(false);
        panelProfile.setManaged(false);
        panelPolicy.setVisible(false);
        panelPolicy.setManaged(false);
        panelHistory.setVisible(false);
        panelHistory.setManaged(false);
        panelNotifications.setVisible(false);
        panelNotifications.setManaged(false);
        panelChangePassword.setVisible(false);
        panelChangePassword.setManaged(false);
        panelAccount.setVisible(false);
        panelAccount.setManaged(false);

        // Xóa màu active của tất cả các nút
        btnTabHome.getStyleClass().remove("sidebar-btn-active");
        btnTabProfile.getStyleClass().remove("sidebar-btn-active");
        btnTabPolicy.getStyleClass().remove("sidebar-btn-active");
        btnTabHistory.getStyleClass().remove("sidebar-btn-active");
        btnTabNotifications.getStyleClass().remove("sidebar-btn-active");
        btnTabChangePassword.getStyleClass().remove("sidebar-btn-active");
        btnTabAccount.getStyleClass().remove("sidebar-btn-active");

        // Kích hoạt panel và nút tương ứng
        if (clickedButton == btnTabHome) {
            panelHome.setVisible(true);
            panelHome.setManaged(true);
            btnTabHome.getStyleClass().add("sidebar-btn-active");
            handleFilterRooms(null); // Làm mới danh sách phòng
        } else if (clickedButton == btnTabProfile) {
            panelProfile.setVisible(true);
            panelProfile.setManaged(true);
            btnTabProfile.getStyleClass().add("sidebar-btn-active");
            loadProfileData();
        } else if (clickedButton == btnTabPolicy) {
            panelPolicy.setVisible(true);
            panelPolicy.setManaged(true);
            btnTabPolicy.getStyleClass().add("sidebar-btn-active");
        } else if (clickedButton == btnTabHistory) {
            panelHistory.setVisible(true);
            panelHistory.setManaged(true);
            btnTabHistory.getStyleClass().add("sidebar-btn-active");
            loadBookingHistory();
        } else if (clickedButton == btnTabNotifications) {
            panelNotifications.setVisible(true);
            panelNotifications.setManaged(true);
            btnTabNotifications.getStyleClass().add("sidebar-btn-active");
            loadNotifications();
        } else if (clickedButton == btnTabChangePassword) {
            panelChangePassword.setVisible(true);
            panelChangePassword.setManaged(true);
            btnTabChangePassword.getStyleClass().add("sidebar-btn-active");
        } else if (clickedButton == btnTabAccount) {
            panelAccount.setVisible(true);
            panelAccount.setManaged(true);
            btnTabAccount.getStyleClass().add("sidebar-btn-active");
            loadAccountLogs();
        }
    }

    // --- TAB 1: ROOMS GRID & BOOKING ---
    @FXML
    void handleFilterRooms(ActionEvent event) {
        LocalDate checkIn = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();

        if (checkIn == null || checkOut == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn ngày nhận và trả phòng.");
            return;
        }

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày trả phòng phải sau ngày nhận phòng.");
            return;
        }

        String selectedType = cbFilterType.getValue();
        String selectedPriceRange = cbFilterPriceRange.getValue();

        List<Room> allRooms = roomDAO.getAllRooms();
        List<Room> filtered = allRooms.stream().filter(room -> {
            // Lọc theo loại phòng
            boolean matchType = "Tất cả các phòng".equals(selectedType)
                    || room.getRoomType().equalsIgnoreCase(selectedType);

            // Lọc theo khoảng giá
            boolean matchPrice = false;
            double price = room.getPrice();
            if ("Tất cả mức giá".equals(selectedPriceRange)) {
                matchPrice = true;
            } else if ("Dưới 1,500,000 ₫".equals(selectedPriceRange) && price < 1500000) {
                matchPrice = true;
            } else if ("1,500,000 ₫ - 2,500,000 ₫".equals(selectedPriceRange) && price >= 1500000 && price <= 2500000) {
                matchPrice = true;
            } else if ("Trên 2,500,000 ₫".equals(selectedPriceRange) && price > 2500000) {
                matchPrice = true;
            }

            return matchType && matchPrice;
        }).collect(Collectors.toList());

        displayRooms(filtered);
    }

    private void displayRooms(List<Room> rooms) {
        flowRooms.getChildren().clear();

        if (rooms.isEmpty()) {
            Label lblNoRooms = new Label("Không tìm thấy phòng nào phù hợp với khoảng giá và tiêu chí chọn.");
            lblNoRooms.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 15px; -fx-font-weight: bold;");
            flowRooms.getChildren().add(lblNoRooms);
            return;
        }

        // Tải toàn bộ phòng để biết số lượng trống của từng loại phòng cùng tên
        List<Room> allRooms = roomDAO.getAllRooms();
        Map<String, List<Room>> grouped = allRooms.stream()
                .collect(Collectors.groupingBy(r -> r.getRoomName().toLowerCase()));

        // Gom nhóm các phòng đã lọc theo tên phòng để chỉ hiển thị đúng 1 thẻ cho mỗi loại phòng
        Map<String, List<Room>> filteredGrouped = rooms.stream()
                .collect(Collectors.groupingBy(r -> r.getRoomName().toLowerCase()));

        List<Room> representativeRooms = new ArrayList<>();
        for (List<Room> group : filteredGrouped.values()) {
            if (group.isEmpty()) continue;
            // Chọn phòng có trạng thái AVAILABLE làm đại diện nếu có, ngược lại lấy phòng đầu tiên
            Room representative = group.stream()
                    .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                    .findFirst()
                    .orElse(group.get(0));
            representativeRooms.add(representative);
        }

        // Sắp xếp các phòng đại diện theo số phòng để hiển thị ngăn nắp
        representativeRooms.sort(java.util.Comparator.comparing(Room::getRoomNumber));

        for (Room room : representativeRooms) {
            VBox card = new VBox();
            card.getStyleClass().add("room-card-box");
            card.setPrefWidth(240);
            card.setPrefHeight(345); // Chiều cao phù hợp
            card.setSpacing(8);
            card.setCursor(javafx.scene.Cursor.HAND);
            card.setOnMouseClicked(event -> {
                if (event.getTarget() instanceof Button) return;
                showRoomDetails(room);
            });

            ImageView imgView = new ImageView();
            imgView.setFitWidth(238);
            imgView.setFitHeight(140);
            imgView.setPreserveRatio(false);

            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(238, 140);
            clip.setArcWidth(20);
            clip.setArcHeight(20);
            imgView.setClip(clip);

            // Tải ảnh chạy ngầm và giả lập User-Agent trình duyệt để tránh bị chặn (lỗi 403)
            javafx.concurrent.Task<Image> loadImgTask = new javafx.concurrent.Task<>() {
                @Override
                protected Image call() throws Exception {
                    String urlString = room.getImage();
                    if (urlString == null || urlString.isEmpty()) {
                        urlString = "https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80";
                    }
                    java.net.URL url = new java.net.URL(urlString);
                    java.net.URLConnection connection = url.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
                    try (java.io.InputStream is = connection.getInputStream()) {
                        return new Image(is);
                    }
                }
            };
            loadImgTask.setOnSucceeded(e -> imgView.setImage(loadImgTask.getValue()));
            loadImgTask.setOnFailed(e -> imgView.setImage(new Image("https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80", true)));
            new Thread(loadImgTask).start();

            VBox infoBox = new VBox(5);
            infoBox.setPadding(new Insets(8));

            Label lblTitle = new Label(room.getRoomName() + " (P." + room.getRoomNumber() + ")");
            lblTitle.getStyleClass().add("room-card-title");
            lblTitle.setWrapText(true);
            lblTitle.setPrefHeight(40);
            lblTitle.setAlignment(Pos.TOP_LEFT);

            // Số lượng phòng còn trống của loại phòng này (cùng tên)
            List<Room> availableRoomsOfName = grouped.getOrDefault(room.getRoomName().toLowerCase(), new ArrayList<>()).stream()
                    .filter(r -> "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                    .collect(Collectors.toList());
            int availableCount = availableRoomsOfName.size();

            // Trạng thái phòng hiện tại
            Label lblStatus = new Label();
            if ("AVAILABLE".equalsIgnoreCase(room.getStatus())) {
                lblStatus.setText("●  Sẵn sàng (Trống - Loại còn " + availableCount + ")");
                lblStatus.setStyle("-fx-text-fill: #38a169; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if ("BOOKED".equalsIgnoreCase(room.getStatus())) {
                lblStatus.setText("●  Đã đặt trước");
                lblStatus.setStyle("-fx-text-fill: #2b6cb0; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if ("OCCUPIED".equalsIgnoreCase(room.getStatus())) {
                lblStatus.setText("●  Đang có khách");
                lblStatus.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else if ("REPAIRING".equalsIgnoreCase(room.getStatus())) {
                lblStatus.setText("●  Đang nâng cấp");
                lblStatus.setStyle("-fx-text-fill: #dd6b20; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else { // MAINTENANCE
                lblStatus.setText("●  Đang bảo trì");
                lblStatus.setStyle("-fx-text-fill: #e53e3e; -fx-font-size: 11px; -fx-font-weight: bold;");
            }

            HBox priceRow = new HBox();
            priceRow.setAlignment(Pos.CENTER_LEFT);
            priceRow.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-padding: 8 0 0 0;");

            VBox priceBox = new VBox(1);
            Label lblPrice = new Label(String.format("%,.0f ₫", room.getPrice()));
            lblPrice.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e53e3e;");
            Label lblPerNight = new Label("/ đêm");
            lblPerNight.setStyle("-fx-font-size: 9px; -fx-text-fill: #718096;");
            priceBox.getChildren().addAll(lblPrice, lblPerNight);

            HBox.setHgrow(priceBox, Priority.ALWAYS);

            Button btnBook = new Button();
            btnBook.setPrefHeight(30);
            btnBook.setPrefWidth(72);

            if ("AVAILABLE".equalsIgnoreCase(room.getStatus())) {
                btnBook.setText("Đặt");
                btnBook.getStyleClass().add("button-accent");
                btnBook.setDisable(false);
                btnBook.setOnAction(e -> {
                    // Mở hộp thoại chọn số lượng phòng muốn đặt cùng loại
                    handleQuickBook(room, availableRoomsOfName);
                });
            } else if ("BOOKED".equalsIgnoreCase(room.getStatus())) {
                btnBook.setText("Đã đặt");
                btnBook.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-weight: bold; -fx-background-radius: 6;");
                btnBook.setDisable(true);
            } else if ("OCCUPIED".equalsIgnoreCase(room.getStatus())) {
                btnBook.setText("Đang ở");
                btnBook.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #a0aec0; -fx-font-weight: bold; -fx-background-radius: 6;");
                btnBook.setDisable(true);
            } else if ("REPAIRING".equalsIgnoreCase(room.getStatus())) {
                btnBook.setText("Nâng cấp");
                btnBook.setStyle("-fx-background-color: #feebc8; -fx-text-fill: #dd6b20; -fx-font-weight: bold; -fx-background-radius: 6;");
                btnBook.setDisable(true);
            } else { // MAINTENANCE
                btnBook.setText("Bảo trì");
                btnBook.setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #e53e3e; -fx-font-weight: bold; -fx-background-radius: 6;");
                btnBook.setDisable(true);
            }

            priceRow.getChildren().addAll(priceBox, btnBook);
            infoBox.getChildren().addAll(lblTitle, lblStatus, priceRow);
            card.getChildren().addAll(imgView, infoBox);

            flowRooms.getChildren().add(card);
        }
    }

    private void handleQuickBook(Room room, List<Room> availableRoomsOfName) {
        int availableCount = availableRoomsOfName.size();
        if (availableCount <= 1) {
            // Chỉ có 1 phòng khả dụng, đặt trực tiếp phòng này
            handleBookRooms(java.util.Collections.singletonList(room));
            return;
        }

        // Tạo danh sách tùy chọn số lượng (ví dụ: từ 1 đến số phòng trống)
        List<Integer> choices = new ArrayList<>();
        for (int i = 1; i <= availableCount; i++) {
            choices.add(i);
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(1, choices);
        dialog.setTitle("Chọn số lượng đặt phòng");
        dialog.setHeaderText("Đặt phòng loại " + room.getRoomName());
        dialog.setContentText(String.format("Loại phòng '%s' hiện còn trống %d phòng.\nQuý khách muốn đặt bao nhiêu phòng?", room.getRoomName(), availableCount));
        
        java.util.Optional<Integer> result = dialog.showAndWait();
        if (result.isPresent()) {
            int qty = result.get();
            
            // Lấy danh sách các phòng để đặt, đảm bảo phòng hiện tại (room) luôn nằm đầu tiên
            List<Room> roomsToBook = new ArrayList<>();
            roomsToBook.add(room);
            
            int added = 1;
            for (Room r : availableRoomsOfName) {
                if (added >= qty) break;
                if (r.getId() != room.getId()) {
                    roomsToBook.add(r);
                    added++;
                }
            }
            
            handleBookRooms(roomsToBook);
        }
    }

    private void handleBookRoom(Room room) {
        handleBookRooms(java.util.Collections.singletonList(room));
    }

    private void handleBookRooms(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) return;
        LocalDate checkIn = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0)
            nights = 1;

        double total = 0;
        for (Room r : rooms) {
            total += r.getPrice() * nights;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PaymentDialog.fxml"));
            Parent root = loader.load();

            PaymentController paymentController = loader.getController();
            paymentController.setBookingDetails(currentUser, rooms, checkIn, checkOut, total, this);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thanh Toán Hóa Đơn - Grand Luxury");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(flowRooms.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cổng thanh toán: " + e.getMessage());
        }
    }

    private void showRoomDetails(Room room) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(flowRooms.getScene().getWindow());
        dialog.setTitle("Chi Tiết Phòng - " + room.getRoomName());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff; -fx-font-family: 'Segoe UI';");
        root.setPrefWidth(460);

        ImageView imgView = new ImageView();
        imgView.setFitWidth(420);
        imgView.setFitHeight(230);
        imgView.setPreserveRatio(false);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(420, 230);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imgView.setClip(clip);

        // Tải ảnh chạy ngầm và giả lập User-Agent trình duyệt để tránh bị chặn (lỗi 403)
        javafx.concurrent.Task<Image> loadImgTask = new javafx.concurrent.Task<>() {
            @Override
            protected Image call() throws Exception {
                String urlString = room.getImage();
                if (urlString == null || urlString.isEmpty()) {
                    urlString = "https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80";
                }
                java.net.URL url = new java.net.URL(urlString);
                java.net.URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
                try (java.io.InputStream is = connection.getInputStream()) {
                    return new Image(is);
                }
            }
        };
        loadImgTask.setOnSucceeded(e -> imgView.setImage(loadImgTask.getValue()));
        loadImgTask.setOnFailed(e -> imgView.setImage(new Image("https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=600&q=80", true)));
        new Thread(loadImgTask).start();

        Label nameLbl = new Label(room.getRoomName() + " (Số phòng: " + room.getRoomNumber() + ")");
        nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        nameLbl.setWrapText(true);

        HBox infoRow = new HBox(12);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        Label typeLbl = new Label(room.getRoomType());
        typeLbl.setStyle("-fx-background-color: #ebf8ff; -fx-text-fill: #2b6cb0; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");

        // Tìm số phòng trống của loại phòng này (cùng tên)
        List<Room> allRooms = roomDAO.getAllRooms();
        List<Room> availableRoomsOfName = allRooms.stream()
                .filter(r -> r.getRoomName().equalsIgnoreCase(room.getRoomName()) && "AVAILABLE".equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.toList());
        int availableCount = availableRoomsOfName.size();

        Label statusLbl = new Label();
        if ("AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            statusLbl.setText("Trống (Loại còn " + availableCount + " phòng trống)");
            statusLbl.setStyle("-fx-background-color: #f0fff4; -fx-text-fill: #38a169; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if ("BOOKED".equalsIgnoreCase(room.getStatus())) {
            statusLbl.setText("Đã được đặt");
            statusLbl.setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #4a5568; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if ("OCCUPIED".equalsIgnoreCase(room.getStatus())) {
            statusLbl.setText("Đang có khách");
            statusLbl.setStyle("-fx-background-color: #edf2f7; -fx-text-fill: #4a5568; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if ("REPAIRING".equalsIgnoreCase(room.getStatus())) {
            statusLbl.setText("Đang nâng cấp");
            statusLbl.setStyle("-fx-background-color: #feebc8; -fx-text-fill: #dd6b20; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            statusLbl.setText("Đang bảo trì");
            statusLbl.setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #e53e3e; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
        }

        Label priceLbl = new Label(String.format("%,.0f ₫ / đêm", room.getPrice()));
        priceLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e53e3e;");

        infoRow.getChildren().addAll(typeLbl, statusLbl, priceLbl);

        // Mô tả hạng phòng
        String descText;
        if ("Deluxe".equalsIgnoreCase(room.getRoomType())) {
            descText = "Phòng Deluxe thiết kế theo phong cách hiện đại với ban công riêng hướng ra cảnh quan tuyệt đẹp. Nội thất gỗ tự nhiên sang trọng, giường lớn cùng hệ thống thiết bị tiện nghi 5 sao.";
        } else if ("Executive".equalsIgnoreCase(room.getRoomType())) {
            descText = "Executive Suite là hạng phòng đẳng cấp kết hợp hài hòa giữa không gian nghỉ ngơi sang trọng và góc làm việc chuyên nghiệp, có đặc quyền trà chiều.";
        } else {
            descText = "Phòng Standard mang lại sự tiện nghi, ấm cúng và đầy đủ các trang thiết bị thiết yếu cho chuyến du lịch hoặc công tác của bạn với chi phí tiết kiệm nhất.";
        }

        Label descTitle = new Label("Mô tả phòng:");
        descTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label descLbl = new Label(descText);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #4a5568;");
        descLbl.setWrapText(true);

        // Chọn số lượng phòng đặt cùng loại
        HBox qtyRow = new HBox(10);
        qtyRow.setAlignment(Pos.CENTER_LEFT);
        Label qtyTitle = new Label();
        qtyTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        Spinner<Integer> qtySpinner = new Spinner<>();
        if ("AVAILABLE".equalsIgnoreCase(room.getStatus()) && availableCount > 0) {
            qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, availableCount, 1));
            qtyTitle.setText("Số lượng phòng đặt cùng loại (" + room.getRoomName() + "):");
            qtySpinner.setDisable(false);
        } else {
            qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0));
            qtySpinner.setDisable(true);
            qtyTitle.setText("Không thể đặt phòng này ở trạng thái hiện tại.");
        }
        qtySpinner.setPrefWidth(70);
        qtyRow.getChildren().addAll(qtyTitle, qtySpinner);

        Button bookBtn = new Button();
        bookBtn.setPrefWidth(Double.MAX_VALUE);
        bookBtn.setPrefHeight(38);

        if ("AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            bookBtn.setText("Tiến Hành Đặt Phòng");
            bookBtn.getStyleClass().add("button-primary");
            bookBtn.setDisable(false);
            bookBtn.setOnAction(e -> {
                dialog.close();
                int qty = qtySpinner.getValue();
                List<Room> roomsToBook = new ArrayList<>();
                roomsToBook.add(room);
                int added = 1;
                for (Room r : availableRoomsOfName) {
                    if (added >= qty) break;
                    if (r.getId() != room.getId()) {
                        roomsToBook.add(r);
                        added++;
                    }
                }
                handleBookRooms(roomsToBook);
            });
        } else if ("BOOKED".equalsIgnoreCase(room.getStatus())) {
            bookBtn.setText("Phòng Đã Được Đặt Trước");
            bookBtn.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #718096; -fx-font-weight: bold;");
            bookBtn.setDisable(true);
        } else if ("OCCUPIED".equalsIgnoreCase(room.getStatus())) {
            bookBtn.setText("Phòng Đang Có Khách Ở");
            bookBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #a0aec0; -fx-font-weight: bold;");
            bookBtn.setDisable(true);
        } else if ("REPAIRING".equalsIgnoreCase(room.getStatus())) {
            bookBtn.setText("Phòng Đang Được Nâng Cấp");
            bookBtn.setStyle("-fx-background-color: #feebc8; -fx-text-fill: #dd6b20; -fx-font-weight: bold;");
            bookBtn.setDisable(true);
        } else { // MAINTENANCE
            bookBtn.setText("Phòng Đang Bảo Trì");
            bookBtn.setStyle("-fx-background-color: #fed7d7; -fx-text-fill: #e53e3e; -fx-font-weight: bold;");
            bookBtn.setDisable(true);
        }

        Button closeBtn = new Button("Đóng");
        closeBtn.setStyle("-fx-background-color: #cbd5e0; -fx-text-fill: #2d3748; -fx-font-weight: bold;");
        closeBtn.setPrefWidth(Double.MAX_VALUE);
        closeBtn.setPrefHeight(32);
        closeBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(imgView, nameLbl, infoRow, descTitle, descLbl, qtyRow, bookBtn, closeBtn);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception ex) {
            // Bỏ qua nếu lỗi CSS
        }
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.centerOnScreen();
        dialog.show();
    }

    public void onBookingSuccess() {
        // Tải lại phòng và gửi thông báo đơn hàng mới
        handleFilterRooms(null);

        // Thêm thông báo đặt phòng thành công
        List<Booking> userBookings = bookingDAO.getBookingsByUserId(currentUser.getId());
        if (!userBookings.isEmpty()) {
            Booking newest = userBookings.get(0);
            notificationDAO.addNotification(currentUser.getId(),
                    String.format(
                            "Đặt phòng thành công! Mã đơn hàng #%d. Phòng số %s đã được đặt trước cho ngày %s đến %s. Vui lòng nhận phòng đúng giờ (14:00).",
                            newest.getId(), newest.getRoomNumber(), newest.getCheckIn(), newest.getCheckOut()));
        }
    }

    // --- TAB 2: PROFILE LOGIC ---
    private void loadProfileData() {
        if (currentUser != null) {
            txtProfileEmail.setText(currentUser.getEmail());
            txtProfileFullName.setText(currentUser.getFullName());
        }
    }

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        String newFullName = txtProfileFullName.getText().trim();
        if (newFullName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Họ và tên không được để trống.");
            return;
        }

        currentUser.setFullName(newFullName);
        if (userDAO.updateUser(currentUser)) {
            SystemLogDAO.addLog(currentUser.getEmail(), "Cập nhật họ tên thành công thành: " + newFullName);
            notificationDAO.addNotification(currentUser.getId(),
                    "Thông tin họ tên đầy đủ đã được cập nhật thành công.");

            lblCustomerName.setText(currentUser.getEmail());
            avatarText.setText(String.valueOf(newFullName.charAt(0)).toUpperCase());

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin cá nhân thành công!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật hồ sơ cá nhân.");
        }
    }

    // --- TAB 4: BOOKING HISTORY LOGIC ---
    private void loadBookingHistory() {
        List<Booking> userBookings = bookingDAO.getBookingsByUserId(currentUser.getId());
        tableHistory.setItems(FXCollections.observableArrayList(userBookings));
    }

    @FXML
    void handleCancelBooking(ActionEvent event) {
        Booking selected = tableHistory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một lịch đặt phòng cần hủy từ bảng.");
            return;
        }

        if (!"BOOKED".equalsIgnoreCase(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Không thể hủy phòng",
                    "Chỉ có thể hủy phòng khi trạng thái là BOOKED (Chờ nhận phòng).");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn hủy đặt phòng số " + selected.getRoomNumber() + " không?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (bookingDAO.updateBookingStatus(selected.getId(), "CANCELLED")) {
                roomDAO.updateRoomStatus(selected.getRoomId(), "AVAILABLE");

                SystemLogDAO.addLog(currentUser.getEmail(), "Khách hàng hủy đơn đặt phòng #" + selected.getId());
                notificationDAO.addNotification(currentUser.getId(),
                        String.format("Đã hủy thành công đơn đặt phòng #%d. Phòng số %s đã được giải phóng trở lại.",
                                selected.getId(), selected.getRoomNumber()));

                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy đơn đặt phòng thành công.");
                loadBookingHistory();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thực hiện hủy phòng.");
            }
        }
    }

    // --- TAB 5: NOTIFICATIONS LOGIC ---
    private void loadNotifications() {
        vboxNotifications.getChildren().clear();
        List<Notification> list = notificationDAO.getNotificationsByUserId(currentUser.getId());

        if (list.isEmpty()) {
            Label lblEmpty = new Label("Bạn không có thông báo nào từ hệ thống.");
            lblEmpty.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 20;");
            vboxNotifications.getChildren().add(lblEmpty);
            return;
        }

        for (Notification n : list) {
            VBox card = new VBox(6);
            card.setStyle(
                    "-fx-background-color: " + (n.isRead() ? "#ffffff" : "#f7fafc") + ";" +
                            "-fx-border-color: #e2e8f0 #e2e8f0 #e2e8f0 " + (n.isRead() ? "#cbd5e0" : "#2b6cb0") + ";" +
                            "-fx-border-width: 1 1 1 4;" +
                            "-fx-border-radius: 6;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 12;");

            Label msg = new Label(n.getMessage());
            msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #2d3748; -fx-font-weight: "
                    + (n.isRead() ? "normal" : "bold") + ";");
            msg.setWrapText(true);

            String timeStr = n.getTimestamp().toString().replace("T", " ").substring(0, 19);
            Label time = new Label("📅  " + timeStr);
            time.setStyle("-fx-font-size: 10px; -fx-text-fill: #a0aec0;");

            card.getChildren().addAll(msg, time);
            vboxNotifications.getChildren().add(card);
        }
    }

    @FXML
    void handleMarkAllRead(ActionEvent event) {
        if (currentUser != null) {
            notificationDAO.markAllAsRead(currentUser.getId());
            loadNotifications();
        }
    }

    // --- TAB 6: CHANGE PASSWORD LOGIC ---
    @FXML
    void handleChangePassword(ActionEvent event) {
        String currentPass = txtCurrentPassword.getText();
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmNewPassword.getText();

        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin mật khẩu.");
            return;
        }

        if (!currentPass.equals(currentUser.getPassword())) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu hiện tại không chính xác.");
            return;
        }

        if (newPass.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return;
        }

        currentUser.setPassword(newPass);
        if (userDAO.updateUser(currentUser)) {
            SystemLogDAO.addLog(currentUser.getEmail(), "Đổi mật khẩu tài khoản thành công");
            notificationDAO.addNotification(currentUser.getId(),
                    "Mật khẩu tài khoản của bạn đã được thay đổi thành công.");

            txtCurrentPassword.clear();
            txtNewPassword.clear();
            txtConfirmNewPassword.clear();

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu tài khoản thành công!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật mật khẩu mới.");
        }
    }

    // --- TAB 7: ACCOUNT MANAGEMENT & LOGS ---
    private void loadAccountLogs() {
        if (currentUser != null) {
            List<SystemLog> logs = logDAO.getLogsByUserEmail(currentUser.getEmail());
            tableLogs.setItems(FXCollections.observableArrayList(logs));
        }
    }

    @FXML
    void handleDeleteAccount(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cảnh báo xóa tài khoản");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Bạn có chắc chắn muốn xóa vĩnh viễn tài khoản của mình? Mọi dữ liệu đặt phòng liên quan sẽ bị ngắt liên kết và tài khoản không thể phục hồi.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (userDAO.deleteUser(currentUser.getId())) {
                SystemLogDAO.addLog(currentUser.getEmail(), "Xóa tài khoản cá nhân vĩnh viễn");
                showAlert(Alert.AlertType.INFORMATION, "Xóa tài khoản thành công",
                        "Tài khoản của bạn đã được xóa thành công. Tạm biệt quý khách.");
                logout(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài khoản của bạn.");
            }
        }
    }

    // --- GLOBAL HELPER METHODS ---
    @FXML
    void logout(ActionEvent event) {
        if (currentUser != null) {
            SystemLogDAO.addLog(currentUser.getEmail(), "Khách hàng đăng xuất khỏi hệ thống");
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage;
            if (event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                // Hỗ trợ trường hợp gọi từ sự kiện khác
                stage = (Stage) contentArea.getScene().getWindow();
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Hotel Management System - Login");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
