module WeightConverter {
    requires javafx.baseEmpty;
    requires javafx.base;
    requires javafx.controlsEmpty;
    requires javafx.controls;
    requires javafx.graphicsEmpty;
    requires javafx.graphics;
    requires javafx.fxmlEmpty;
    requires javafx.fxml;
    
    opens com.fluffy.luffs.weight.converter to javafx.fxml;

    exports com.fluffy.luffs.weight.converter;
}
