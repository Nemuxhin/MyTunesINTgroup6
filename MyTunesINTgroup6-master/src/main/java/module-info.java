module int_group.mytunesintgroup {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires javafx.web;
    requires java.desktop;

    opens int_group6.mytunesintgroup6 to javafx.fxml;
    exports int_group6.mytunesintgroup6;

    opens int_group6.mytunesintgroup6.gui to javafx.fxml;
    exports int_group6.mytunesintgroup6.gui;

    opens int_group6.mytunesintgroup6.be to javafx.base;
    exports int_group6.mytunesintgroup6.be;
}