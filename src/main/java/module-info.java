module com.kerem.ordersystem.carrentalsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires jbcrypt;
    requires static lombok;

    opens com.kerem.ordersystem.carrentalsystem to javafx.fxml;
    opens com.kerem.ordersystem.carrentalsystem.controller to javafx.fxml;
    opens com.kerem.ordersystem.carrentalsystem.model to javafx.base;

    exports com.kerem.ordersystem.carrentalsystem;
}