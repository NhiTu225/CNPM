package com.hotelmanagement.controller;

import com.hotelmanagement.dao.NotificationDAO;
import com.hotelmanagement.dao.SystemLogDAO;
import com.hotelmanagement.dao.UserDAO;
import com.hotelmanagement.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField txtFullName;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @FXML
    void register(ActionEvent event) {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // 1. Validation for empty fields
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // 2. Email format validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Địa chỉ email không đúng định dạng.");
            return;
        }

        // 3. Password length check
        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        // 4. Passwords match validation
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu và xác nhận mật khẩu không khớp.");
            return;
        }

        // 5. Unique email validation
        if (userDAO.isEmailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đăng ký", "Email này đã được sử dụng bởi tài khoản khác.");
            return;
        }

        // 6. DB Registration query (Role CUSTOMER as default)
        boolean success = userDAO.register(fullName, email, password, "CUSTOMER");
        if (success) {
            SystemLogDAO.addLog(email, "Đăng ký tài khoản mới thành công");
            
            // Tạo thông báo chào mừng
            User newUser = userDAO.getUserByEmail(email);
            if (newUser != null) {
                notificationDAO.addNotification(newUser.getId(), "Chào mừng quý khách " + fullName + " đến với Grand Luxury Hotel! Trải nghiệm dịch vụ đặt phòng trực tuyến 5 sao chất lượng cao.");
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký tài khoản thành công! Quay lại màn hình đăng nhập.");
            backToLogin(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn tất việc đăng ký. Vui lòng thử lại sau.");
        }
    }

    @FXML
    void backToLogin(ActionEvent event) {
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
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể quay lại màn hình Đăng Nhập: " + e.getMessage());
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
