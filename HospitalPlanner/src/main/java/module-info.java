module com.example.hospitalplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.hospitalplanner to javafx.fxml;
    exports com.example.hospitalplanner;
}