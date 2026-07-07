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

public class ForgotPasswordController {

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtFullName;

    @FXML
    private PasswordField txtNewPassword;

    @FXML
    private PasswordField txtConfirmPassword;

    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @FXML
    void handleResetPassword(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String fullName = txtFullName.getText().trim();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (email.isEmpty() || fullName.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng điền đầy đủ tất cả các trường.");
            return;
        }

        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            return;
        }

        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tài khoản email này không tồn tại trong hệ thống.");
            return;
        }

        if (user.getFullName() == null || !user.getFullName().trim().equalsIgnoreCase(fullName)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xác thực", "Họ và tên không khớp với tài khoản email đã đăng ký.");
            return;
        }

        // Thực hiện cập nhật mật khẩu
        if (userDAO.updatePasswordByEmail(email, newPassword)) {
            SystemLogDAO.addLog(email, "Đặt lại mật khẩu thành công qua chức năng Quên mật khẩu");
            notificationDAO.addNotification(user.getId(), "Mật khẩu của bạn đã được đặt lại thành công.");
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.");
            backToLogin(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật mật khẩu mới. Vui lòng thử lại sau.");
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
