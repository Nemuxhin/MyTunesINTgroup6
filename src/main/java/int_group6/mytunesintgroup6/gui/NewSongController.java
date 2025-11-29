package int_group6.mytunesintgroup6.gui;

import int_group6.mytunesintgroup6.be.Song;
import int_group6.mytunesintgroup6.dal.SongDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class NewSongController implements Initializable {

    @FXML private TextField txtTitle, txtArtist, txtTime, txtFile;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private Button btnCancel, btnSave, btnChoose;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Pre-fill categories
        cmbCategory.getItems().addAll("Pop", "Rock", "Techno", "Classical", "Rap");

        // Button actions
        btnCancel.setOnAction(event -> closeWindow());
        btnSave.setOnAction(event -> saveSong());
        btnChoose.setOnAction(event -> chooseFile());
    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select MP3 File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));

        // Show open dialog
        Stage stage = (Stage) btnChoose.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            txtFile.setText(selectedFile.getAbsolutePath());
            txtTime.setText("0:00"); // Dummy time for now
        }
    }

    private void saveSong() {
        String title = txtTitle.getText();
        String artist = txtArtist.getText();
        String category = cmbCategory.getValue();
        String time = txtTime.getText();
        String file = txtFile.getText();

        if (!title.isEmpty() && !file.isEmpty()) {
            // 1. Create Song Object (ID is 0 because DB auto-generates it)
            Song newSong = new Song(0, title, artist, category, time, file);

            // 2. Save to DB
            SongDAO songDAO = new SongDAO();
            songDAO.createSong(newSong);

            // 3. Close window
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}