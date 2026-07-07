package com.hotelmanagement.controller;

import com.hotelmanagement.dao.BookingDAO;
import com.hotelmanagement.dao.InvoiceDAO;
import com.hotelmanagement.dao.RoomDAO;
import com.hotelmanagement.dao.SystemLogDAO;
import com.hotelmanagement.dao.UserDAO;
import com.hotelmanagement.database.CSVHelper;
import com.hotelmanagement.model.Booking;
import com.hotelmanagement.model.Room;
import com.hotelmanagement.model.SystemLog;
import com.hotelmanagement.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AdminController {

    private User currentUser;

    // DAOs
    private final RoomDAO roomDAO = new RoomDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final SystemLogDAO logDAO = new SystemLogDAO();

    // Header Components
    @FXML
    private Label lblWelcome;

    // Tab 1: Room Management
    @FXML
    private TextField txtRoomNumber;
    @FXML
    private TextField txtRoomName;
    @FXML
    private TextField txtRoomType;
    @FXML
    private TextField txtRoomPrice;
    @FXML
    private TextField txtRoomImage;
    @FXML
    private ComboBox<String> cbRoomStatus;

    @FXML
    private TableView<Room> tblRooms;
    @FXML
    private TableColumn<Room, Integer> colRoomId;
    @FXML
    private TableColumn<Room, String> colRoomNumber;
    @FXML
    private TableColumn<Room, String> colRoomName;
    @FXML
    private TableColumn<Room, String> colRoomType;
    @FXML
    private TableColumn<Room, Double> colRoomPrice;
    @FXML
    private TableColumn<Room, String> colRoomStatus;

    private Room selectedRoom = null;

    // Tab 3: Reports & Analytics
    @FXML
    private BarChart<String, Number> chartRevenue;
    @FXML
    private PieChart chartOccupancy;

    // Tab 4: System Logs
    @FXML
    private TableView<SystemLog> tblLogs;
    @FXML
    private TableColumn<SystemLog, Integer> colLogId;
    @FXML
    private TableColumn<SystemLog, String> colLogUser;
    @FXML
    private TableColumn<SystemLog, String> colLogAction;
    @FXML
    private TableColumn<SystemLog, String> colLogTime;

    // Tab 5: User & Role Management
    @FXML
    private TextField txtUserFullName;
    @FXML
    private TextField txtUserEmail;
    @FXML
    private TextField txtUserPassword;
    @FXML
    private ComboBox<String> cbUserRole;

    @FXML
    private TableView<User> tblUsers;
    @FXML
    private TableColumn<User, Integer> colUserId;
    @FXML
    private TableColumn<User, String> colUserFullName;
    @FXML
    private TableColumn<User, String> colUserEmail;
    @FXML
    private TableColumn<User, String> colUserRole;

    private final UserDAO userDAO = new UserDAO();
    private User selectedUser = null;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        lblWelcome.setText("Welcome, " + user.getFullName() + " (" + user.getRole() + ")!");
    }

    @FXML
    public void initialize() {
        // Cấu hình danh mục trạng thái phòng
        cbRoomStatus.setItems(FXCollections.observableArrayList("AVAILABLE", "BOOKED", "OCCUPIED", "REPAIRING", "MAINTENANCE"));
        cbRoomStatus.setValue("AVAILABLE");

        // Cấu hình cột bảng phòng
        colRoomId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        colRoomType.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        colRoomPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colRoomStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cấu hình cột bảng nhật ký
        colLogId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLogUser.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        colLogAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colLogTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Cấu hình vai trò tài khoản
        cbUserRole.setItems(FXCollections.observableArrayList("CUSTOMER", "ADMIN", "RECEPTIONIST"));
        cbUserRole.setValue("CUSTOMER");

        // Cấu hình cột bảng người dùng
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Tải dữ liệu ban đầu
        refreshData();
    }

    private void refreshData() {
        loadRooms();
        loadLogs();
        loadCharts();
        loadUsers();
    }

    private void loadRooms() {
        List<Room> rooms = roomDAO.getAllRooms();
        tblRooms.setItems(FXCollections.observableArrayList(rooms));
    }

    private void loadLogs() {
        List<SystemLog> logs = logDAO.getAllLogs();
        tblLogs.setItems(FXCollections.observableArrayList(logs));
    }

    private void loadUsers() {
        List<User> users = userDAO.getAllUsers();
        tblUsers.setItems(FXCollections.observableArrayList(users));
    }

    private void loadCharts() {
        // 1. Biểu đồ tròn: Tình trạng phòng (Occupancy Analysis)
        chartOccupancy.getData().clear();
        Map<String, Integer> roomCounts = roomDAO.getRoomStatusCounts();
        for (Map.Entry<String, Integer> entry : roomCounts.entrySet()) {
            chartOccupancy.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        // 2. Biểu đồ cột: Doanh thu theo tháng
        chartRevenue.getData().clear();
        Map<String, Double> monthlyRev = invoiceDAO.getRevenueByMonth();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        for (Map.Entry<String, Double> entry : monthlyRev.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chartRevenue.getData().add(series);
    }

    // --- TAB 1: ROOM CRUD HANDLERS ---

    @FXML
    void handleRoomTableClick(MouseEvent event) {
        selectedRoom = tblRooms.getSelectionModel().getSelectedItem();
        if (selectedRoom != null) {
            txtRoomNumber.setText(selectedRoom.getRoomNumber());
            txtRoomName.setText(selectedRoom.getRoomName());
            txtRoomType.setText(selectedRoom.getRoomType());
            txtRoomPrice.setText(String.valueOf(selectedRoom.getPrice()));
            txtRoomImage.setText(selectedRoom.getImage());
            cbRoomStatus.setValue(selectedRoom.getStatus());
        }
    }

    @FXML
    void addRoom(ActionEvent event) {
        try {
            String baseNumStr = txtRoomNumber.getText().trim();
            String baseName = txtRoomName.getText().trim();
            String type = txtRoomType.getText().trim();
            double price = Double.parseDouble(txtRoomPrice.getText().trim());
            String img = txtRoomImage.getText().trim();
            String status = cbRoomStatus.getValue();

            if (baseNumStr.isEmpty() || baseName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền mã và tên phòng.");
                return;
            }

            // Hỏi số lượng phòng cần thêm
            TextInputDialog qtyDialog = new TextInputDialog("1");
            qtyDialog.setTitle("Nhập số lượng");
            qtyDialog.setHeaderText("Thêm nhiều phòng cùng thông số");
            qtyDialog.setContentText("Nhập số lượng phòng muốn thêm:");
            java.util.Optional<String> result = qtyDialog.showAndWait();
            if (!result.isPresent()) return;

            int qty = 1;
            try {
                qty = Integer.parseInt(result.get().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số lượng phải là số nguyên hợp lệ.");
                return;
            }
            if (qty <= 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số lượng phải lớn hơn 0.");
                return;
            }

            int startNum = 0;
            boolean isNumeric = true;
            try {
                startNum = Integer.parseInt(baseNumStr);
            } catch (NumberFormatException e) {
                isNumeric = false;
            }

            int successCount = 0;
            for (int i = 0; i < qty; i++) {
                String roomNum;
                if (isNumeric) {
                    roomNum = String.valueOf(startNum + i);
                } else {
                    roomNum = (qty == 1) ? baseNumStr : (baseNumStr + "_" + (i + 1));
                }

                Room r = new Room();
                r.setRoomNumber(roomNum);
                r.setRoomName(baseName);
                r.setRoomType(type);
                r.setPrice(price);
                r.setImage(img);
                r.setStatus(status);

                if (roomDAO.addRoom(r)) {
                    SystemLogDAO.addLog(getCurrentUserEmail(), "Thêm phòng mới: " + roomNum);
                    successCount++;
                }
            }

            if (successCount > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm " + successCount + " phòng mới thành công.");
                clearRoomFields(null);
                refreshData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm phòng nào (Mã phòng có thể đã tồn tại).");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá phòng phải là số hợp lệ.");
        }
    }

    @FXML
    void updateRoom(ActionEvent event) {
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phòng từ bảng để sửa.");
            return;
        }
        try {
            selectedRoom.setRoomNumber(txtRoomNumber.getText().trim());
            selectedRoom.setRoomName(txtRoomName.getText().trim());
            selectedRoom.setRoomType(txtRoomType.getText().trim());
            selectedRoom.setPrice(Double.parseDouble(txtRoomPrice.getText().trim()));
            selectedRoom.setImage(txtRoomImage.getText().trim());
            selectedRoom.setStatus(cbRoomStatus.getValue());

            if (roomDAO.updateRoom(selectedRoom)) {
                SystemLogDAO.addLog(getCurrentUserEmail(), "Cập nhật phòng ID " + selectedRoom.getId() + " (" + selectedRoom.getRoomNumber() + ")");
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật phòng thành công.");
                clearRoomFields(null);
                refreshData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật phòng.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giá phòng phải là số hợp lệ.");
        }
    }

    @FXML
    void deleteRoom(ActionEvent event) {
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phòng để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa phòng " + selectedRoom.getRoomNumber() + " không?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            if (roomDAO.deleteRoom(selectedRoom.getId())) {
                SystemLogDAO.addLog(getCurrentUserEmail(), "Xóa phòng ID " + selectedRoom.getId() + " (" + selectedRoom.getRoomNumber() + ")");
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa phòng thành công.");
                clearRoomFields(null);
                refreshData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa phòng (Phòng này có thể đang có lịch đặt).");
            }
        }
    }

    @FXML
    void clearRoomFields(ActionEvent event) {
        selectedRoom = null;
        txtRoomNumber.clear();
        txtRoomName.clear();
        txtRoomType.clear();
        txtRoomPrice.clear();
        txtRoomImage.clear();
        cbRoomStatus.setValue("AVAILABLE");
        tblRooms.getSelectionModel().clearSelection();
    }



    // --- TAB 4: LOGS HANDLERS ---

    @FXML
    void refreshLogs(ActionEvent event) {
        loadLogs();
    }

    // --- SYSTEM LOG OUT ---

    @FXML
    void logout(ActionEvent event) {
        SystemLogDAO.addLog(getCurrentUserEmail(), "Đăng xuất khỏi hệ thống");
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

    // --- UTILITIES ---

    private String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "admin@gmail.com";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- TAB 5: USER CRUD HANDLERS ---

    @FXML
    void handleUserTableClick(MouseEvent event) {
        selectedUser = tblUsers.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            txtUserFullName.setText(selectedUser.getFullName());
            txtUserEmail.setText(selectedUser.getEmail());
            txtUserPassword.setText(selectedUser.getPassword());
            cbUserRole.setValue(selectedUser.getRole());
        }
    }

    @FXML
    void addUser(ActionEvent event) {
        String fullName = txtUserFullName.getText().trim();
        String email = txtUserEmail.getText().trim();
        String password = txtUserPassword.getText();
        String role = cbUserRole.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (userDAO.isEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi thêm", "Email này đã tồn tại trên hệ thống.");
            return;
        }

        User u = new User(0, fullName, email, password, role);
        if (userDAO.addUser(u)) {
            SystemLogDAO.addLog(getCurrentUserEmail(), "Thêm tài khoản người dùng mới: " + email + " với vai trò " + role);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm tài khoản mới thành công.");
            clearUserFields(null);
            refreshData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm tài khoản.");
        }
    }

    @FXML
    void updateUser(ActionEvent event) {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một tài khoản từ danh sách.");
            return;
        }

        String fullName = txtUserFullName.getText().trim();
        String email = txtUserEmail.getText().trim();
        String password = txtUserPassword.getText();
        String role = cbUserRole.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (!email.equalsIgnoreCase(selectedUser.getEmail()) && userDAO.isEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi cập nhật", "Email mới này đã được sử dụng.");
            return;
        }

        selectedUser.setFullName(fullName);
        selectedUser.setEmail(email);
        selectedUser.setPassword(password);
        selectedUser.setRole(role);

        if (userDAO.updateUser(selectedUser)) {
            SystemLogDAO.addLog(getCurrentUserEmail(), "Cập nhật tài khoản ID " + selectedUser.getId() + " (" + email + ")");
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật tài khoản thành công.");
            clearUserFields(null);
            refreshData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật thông tin tài khoản.");
        }
    }

    @FXML
    void deleteUser(ActionEvent event) {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn tài khoản cần xóa.");
            return;
        }

        if (selectedUser.getId() == currentUser.getId()) {
            showAlert(Alert.AlertType.ERROR, "Không thể tự xóa", "Bạn không thể tự xóa chính tài khoản đang đăng nhập của mình.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa tài khoản " + selectedUser.getEmail() + " không?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            if (userDAO.deleteUser(selectedUser.getId())) {
                SystemLogDAO.addLog(getCurrentUserEmail(), "Xóa tài khoản ID " + selectedUser.getId() + " (" + selectedUser.getEmail() + ")");
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa tài khoản thành công.");
                clearUserFields(null);
                refreshData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài khoản này (Tài khoản này có thể đang liên kết với các hóa đơn/lịch đặt phòng).");
            }
        }
    }

    @FXML
    void clearUserFields(ActionEvent event) {
        selectedUser = null;
        txtUserFullName.clear();
        txtUserEmail.clear();
        txtUserPassword.clear();
        cbUserRole.setValue("CUSTOMER");
        tblUsers.getSelectionModel().clearSelection();
    }
}
