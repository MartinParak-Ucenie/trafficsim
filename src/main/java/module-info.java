module sk.mpar.trafficsim {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens sk.mpar.trafficsim to javafx.fxml;
    exports sk.mpar.trafficsim;
    exports sk.mpar.trafficsim.model;
    opens sk.mpar.trafficsim.model to javafx.fxml;
}
