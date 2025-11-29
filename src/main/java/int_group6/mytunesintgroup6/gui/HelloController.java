package int_group6.mytunesintgroup6.gui;

import int_group6.mytunesintgroup6.be.Song;
import int_group6.mytunesintgroup6.dal.SongDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;       // Import for Music
import javafx.scene.media.MediaPlayer; // Import for Music
import java.io.File;                   // Import for File check
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class HelloController implements Initializable {

    // --- FXML INJECTIONS ---
    @FXML private Button btnPlay, btnPause, btnStop;
    @FXML private Label lblNowPlaying;
    @FXML private TextField txtFilter;
    @FXML private Button btnFilterClear;

    @FXML private TableView<?> tblPlaylists;
    @FXML private TableColumn<?, String> colPlaylistName;

    @FXML private TableView<Song> tblSongsInPlaylist;
    @FXML private TableColumn<Song, String> colPlSongTitle;
    @FXML private TableColumn<Song, String> colPlSongArtist;
    @FXML private TableColumn<Song, String> colPlSongCategory;
    @FXML private TableColumn<Song, String> colPlSongTime;

    @FXML private TableView<Song> tblSongs;
    @FXML private TableColumn<Song, String> colTitle;
    @FXML private TableColumn<Song, String> colArtist;
    @FXML private TableColumn<Song, String> colCategory;
    @FXML private TableColumn<Song, String> colTime;
    @FXML private TableColumn<Song, String> colFilePath;

    @FXML private Button btnNewPlaylist, btnEditPlaylist, btnDeletePlaylist;
    @FXML private Button btnAddToPlaylist, btnMoveUp, btnMoveDown, btnRemoveFromPlaylist;
    @FXML private Button btnNewSong, btnEditSong, btnDeleteSong, btnClose;

    private MediaPlayer mediaPlayer; // To play music

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing Controller...");

        // 1. Setup Columns
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colFilePath.setCellValueFactory(new PropertyValueFactory<>("filePath"));

        // 2. Load Data from Database
        try {
            SongDAO songDAO = new SongDAO();
            List<Song> dbSongs = songDAO.getAllSongs();
            tblSongs.getItems().addAll(dbSongs);
            System.out.println("Success! Loaded " + dbSongs.size() + " songs.");
        } catch (Exception e) {
            System.err.println("Something went wrong loading songs!");
            e.printStackTrace();
        }


        btnPlay.setOnAction(event -> playSong());

        btnNewSong.setOnAction(event -> openNewSongWindow());

        btnDeleteSong.setOnAction(event -> deleteSong());
    }

    //(The Logic Method)
    private void playSong() {
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (selectedSong != null) {
            // Stop previous song
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            // Find file
            String filePath = selectedSong.getFilePath();
            File file = new File(filePath);

            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
                lblNowPlaying.setText(selectedSong.getTitle() + " is playing...");
            } else {
                System.out.println("File not found: " + filePath);
                lblNowPlaying.setText("Error: File not found.");
            }
        } else {
            lblNowPlaying.setText("Please select a song.");
        }
    }
    @FXML
    private void openNewSongWindow() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/int_group6/mytunesintgroup6/new-song-view.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Add New Song");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait(); // Wait until the popup closes

            // Refresh table after popup closes
            tblSongs.getItems().clear();
            SongDAO songDAO = new SongDAO();
            tblSongs.getItems().addAll(songDAO.getAllSongs());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSong() {
        // 1. Get selected song
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (selectedSong != null) {
            // 2. Ask for confirmation (Standard UI practice)
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Song");
            alert.setHeaderText("Are you sure you want to delete: " + selectedSong.getTitle() + "?");
            alert.setContentText("This cannot be undone.");

            // 3. If user clicks OK, delete it
            if (alert.showAndWait().get() == ButtonType.OK) {
                // Delete from DB
                SongDAO songDAO = new SongDAO();
                songDAO.deleteSong(selectedSong);

                // Delete from Table (Update UI)
                tblSongs.getItems().remove(selectedSong);
            }
        } else {
            // Show error if nothing selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Song Selected");
            alert.setContentText("Please select a song to delete.");
            alert.showAndWait();
        }
    }
}