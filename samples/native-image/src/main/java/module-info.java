module com.toocol.image {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.toocol.image to javafx.fxml;
    exports com.toocol.image;
}