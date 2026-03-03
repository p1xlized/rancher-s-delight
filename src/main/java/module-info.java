
module com.example.ranchers_delight {
        requires javafx.controls;
        requires javafx.fxml;
        requires com.almasb.fxgl.all;

        // You must OPEN the packages to the FXGL core module
        opens com.example.ranchers_delight to com.almasb.fxgl.core;
        opens com.example.ranchers_delight.components to com.almasb.fxgl.core;
        opens com.example.ranchers_delight.factories to com.almasb.fxgl.core;

        exports com.example.ranchers_delight;
}