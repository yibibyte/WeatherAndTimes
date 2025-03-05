module ru.japp.weathertime.weathertime {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens ru.japp.weathertime.weathertime to javafx.fxml;
    exports ru.japp.weathertime.weathertime;
}