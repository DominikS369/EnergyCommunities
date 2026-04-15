module disys.project.disys_javafx {
    requires javafx.controls;
    requires javafx.fxml;

    opens disys.project.disys_javafx to javafx.fxml;
    exports disys.project.disys_javafx;
}