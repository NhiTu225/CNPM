package com.hotelmanagement.controller;

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

public class LoginController {

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    void login(ActionEvent event) {

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập đầy đủ email và mật khẩu.");
            alert.show();
            return;
        }

        User user = userDAO.login(email, password);

        if (user == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Sai email hoặc mật khẩu.");
            alert.show();
            SystemLogDAO.addLog(email, "Đăng nhập thất bại - Sai thông tin");
            return;
        }

        if (user.getRole().equalsIgnoreCase("ADMIN")) {
            SystemLogDAO.addLog(user.getEmail(), "Đăng nhập thành công với vai trò ADMIN");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminView.fxml"));
                Parent root = loader.load();
 
                AdminController adminController = loader.getController();
                adminController.setCurrentUser(user);
 
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Hotel Management System - Admin Dashboard");
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Không thể mở giao diện Admin: " + e.getMessage());
                alert.show();
            }
        } else if (user.getRole().equalsIgnoreCase("RECEPTIONIST")) {
            SystemLogDAO.addLog(user.getEmail(), "Đăng nhập thành công với vai trò RECEPTIONIST");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReceptionistView.fxml"));
                Parent root = loader.load();
 
                ReceptionistController receptionistController = loader.getController();
                receptionistController.setCurrentUser(user);
 
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Hotel Management System - Receptionist Panel");
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Không thể mở giao diện Lễ tân: " + e.getMessage());
                alert.show();
            }
        } else {
            // Đối với vai trò CUSTOMER
            SystemLogDAO.addLog(user.getEmail(), "Khách hàng đăng nhập thành công");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomerView.fxml"));
                Parent root = loader.load();

                // Lấy controller để truyền thông tin User
                CustomerController customerController = loader.getController();
                customerController.setCurrentUser(user);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Grand Luxury Hotel - Portal đặt phòng");
                stage.centerOnScreen();
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("Không thể mở giao diện Khách hàng: " + e.getMessage());
                alert.show();
            }
        }
    }

    @FXML
    void showRegisterView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Hotel Management System - Đăng Ký Tài Khoản");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở giao diện Đăng Ký: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    void showForgotPasswordView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForgotPasswordView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Hotel Management System - Khôi Phục Mật Khẩu");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở giao diện Khôi Phục Mật Khẩu: " + e.getMessage());
            alert.show();
        }
    }
}