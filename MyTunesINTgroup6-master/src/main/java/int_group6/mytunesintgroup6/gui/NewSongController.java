package int_group6.mytunesintgroup6.gui;

import int_group6.mytunesintgroup6.be.Song;
import int_group6.mytunesintgroup6.bll.MyTunesManager;
import int_group6.mytunesintgroup6.dal.SongDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
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

    // This variable tells us if we are Editing (not null) or Creating (null)
    private Song songToEdit = null;

    // Samu: injected from HelloController so the dialog follows the GUI -> BLL -> DAL flow.
    private MyTunesManager manager;

    public void setManager(MyTunesManager manager) {
        this.manager = manager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbCategory.getItems().addAll("Pop", "Rock", "Techno", "Classical", "Rap", "Hip-Hop", "Jazz");
        cmbCategory.getSelectionModel().selectFirst();

        btnCancel.setOnAction(event -> closeWindow());
        btnSave.setOnAction(event -> saveSong());
        btnChoose.setOnAction(event -> chooseFile());
    }

    /**
     * Call this method immediately after opening the window to set "Edit Mode"
     */
    public void setSongToEdit(Song song) {
        this.songToEdit = song;

        // Pre-fill the fields
        txtTitle.setText(song.getTitle());
        txtArtist.setText(song.getArtist());
        cmbCategory.setValue(song.getCategory());
        txtTime.setText(song.getTime());
        txtFile.setText(song.getFilePath());

        // Change button text to make it obvious
        btnSave.setText("Update");
    }

    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select MP3 File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"));

        // Try to open in Music folder
        String userMusicDir = System.getProperty("user.home") + File.separator + "Music";
        File initialDir = new File(userMusicDir);
        if (initialDir.exists()) fileChooser.setInitialDirectory(initialDir);

        Stage stage = (Stage) btnChoose.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            txtFile.setText(selectedFile.getAbsolutePath());
            if (txtTitle.getText().isEmpty()) {
                txtTitle.setText(selectedFile.getName().replace(".mp3", ""));
            }
            if (txtTime.getText().isEmpty()) txtTime.setText("3:00");
        }
    }

    private void saveSong() {
        String title = txtTitle.getText();
        String artist = txtArtist.getText();
        String category = cmbCategory.getValue();
        String time = txtTime.getText();
        String file = txtFile.getText();

        if (title.isEmpty() || artist.isEmpty() || category == null || file.isEmpty()) {
            showAlert("Missing Information", "Please fill in all fields.");
            return;
        }

        try {
            // Fallback to keep the window functional even if opened without injection.
            if (manager == null) manager = new MyTunesManager();

            if (songToEdit != null) {
                // --- UPDATE MODE ---
                // Update the existing object
                songToEdit.setTitle(title);
                songToEdit.setArtist(artist);
                songToEdit.setCategory(category);
                // Samu: these two fields were not being updated before, so editing a song did not persist them.
                songToEdit.setTime(time);
                songToEdit.setFilePath(file);

                // Save to DB
                manager.updateSong(songToEdit);
            } else {
                // --- CREATE MODE ---
                Song newSong = new Song(0, title, artist, category, time, file);
                manager.createSong(newSong);
            }

            closeWindow();
        } catch (Exception e) {
            showAlert("Error", "Could not save song.");
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}