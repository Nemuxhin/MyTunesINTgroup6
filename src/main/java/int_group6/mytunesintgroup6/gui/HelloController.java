package int_group6.mytunesintgroup6.gui;

import int_group6.mytunesintgroup6.be.Playlist;
import int_group6.mytunesintgroup6.dal.PlaylistDAO;
import javafx.util.Duration;
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
import javafx.scene.control.TextInputDialog;
import int_group6.mytunesintgroup6.be.Playlist;

public class HelloController implements Initializable {

    // --- FXML INJECTIONS ---
    @FXML private Button btnPlay, btnPause, btnStop;
    @FXML private Label lblNowPlaying;
    @FXML private TextField txtFilter;
    @FXML private Button btnFilterClear;

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

    @FXML private Slider songSlider;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblTotalTime;

    @FXML private TableView<Playlist> tblPlaylists;
    @FXML private TableColumn<Playlist, String> colPlaylistName;


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
        // --- MIDDLE TABLE SETUP (Songs inside Playlist) ---
        colPlSongTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPlSongArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colPlSongCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPlSongTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        // --- CONNECT PLAYLIST SONG BUTTONS ---
        btnAddToPlaylist.setOnAction(event -> handleAddToPlaylist());
        btnRemoveFromPlaylist.setOnAction(event -> handleRemoveFromPlaylist());
        btnMoveUp.setOnAction(event -> handleMoveUp());
        btnMoveDown.setOnAction(event -> handleMoveDown());


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

        // LISTENER: When a Playlist is clicked, load its songs
        tblPlaylists.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Use your DAO to get the songs
                PlaylistDAO playlistDAO = new PlaylistDAO();
                List<Song> songs = playlistDAO.getSongsInPlaylist(newVal);

                // Fill the middle table
                tblSongsInPlaylist.getItems().setAll(songs);
            }
        });

        btnPlay.setOnAction(event -> playSong());

        btnPause.setOnAction(event -> pauseSong());

        btnStop.setOnAction(event -> stopSong());

        btnNewSong.setOnAction(event -> openNewSongWindow());

        btnDeleteSong.setOnAction(event -> deleteSong());

        btnEditSong.setOnAction(event -> editSong());

        // --- PLAYLIST SETUP ---
        // 1. Setup Column
        colPlaylistName.setCellValueFactory(new PropertyValueFactory<>("name"));

        // 2. Load Data
        refreshPlaylists();

        // 3. Connect Buttons
        btnNewPlaylist.setOnAction(event -> handleNewPlaylist());
        btnEditPlaylist.setOnAction(event -> handleEditPlaylist());
        btnDeletePlaylist.setOnAction(event -> handleDeletePlaylist());
    }
    // --- MEDIA PLAYER CONTROLS ---

    @FXML
    private void playSong() {
        // CASE 1: Resume if paused
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            mediaPlayer.play();
            lblNowPlaying.setText("Playing: " + tblSongs.getSelectionModel().getSelectedItem().getTitle());
            return;
        }

        // CASE 2: Start new song
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            // Stop old music if playing
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose(); // Release memory
            }

            File file = new File(selectedSong.getFilePath());
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                // 1. Hook up the Progress Bar (The complex part)
                setupProgressBar();

                // 2. Play
                mediaPlayer.play();
                lblNowPlaying.setText("Playing: " + selectedSong.getTitle());
            } else {
                lblNowPlaying.setText("Error: File not found.");
            }
        }
    }

    @FXML
    private void pauseSong() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            lblNowPlaying.setText("Paused");
        }
    }

    @FXML
    private void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            lblNowPlaying.setText("Stopped");
            // Reset slider
            songSlider.setValue(0);
            lblCurrentTime.setText("0:00");
        }
    }

    /**
     * This connects the math of the MediaPlayer to the Visual Slider
     */
    private void setupProgressBar() {
        // 1. When the media is ready, set the Slider Max value (Total Duration)
        mediaPlayer.setOnReady(() -> {
            double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
            songSlider.setMax(totalDuration);
            lblTotalTime.setText(formatTime(totalDuration));
        });

        // 2. As the song plays, update the Slider position automatically
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!songSlider.isValueChanging()) { // Only update if user isn't dragging it!
                songSlider.setValue(newValue.toSeconds());
                lblCurrentTime.setText(formatTime(newValue.toSeconds()));
            }
        });

        // 3. Allow user to drag the slider to seek (Skip ahead)
        songSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (songSlider.isValueChanging()) {
                lblCurrentTime.setText(formatTime(newValue.doubleValue()));
            }
        });

        // When user releases mouse, jump to that time
        songSlider.setOnMouseReleased(event -> {
            mediaPlayer.seek(Duration.seconds(songSlider.getValue()));
        });
    }

    // Helper to turn "95.0" seconds into "1:35"
    private String formatTime(double timeSeconds) {
        int minutes = (int) timeSeconds / 60;
        int seconds = (int) timeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
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
            // Show error if nothing is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Song Selected");
            alert.setContentText("Please select a song to delete.");
            alert.showAndWait();
        }
    }

    @FXML
    private void editSong() {
        // 1. Get selected song
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (selectedSong != null) {
            try {
                // 2. Load the FXML
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/int_group6/mytunesintgroup6/new-song-view.fxml"));
                javafx.scene.Parent root = loader.load();

                // 3. GET THE CONTROLLER and pass the song data
                NewSongController controller = loader.getController();
                controller.setSongToEdit(selectedSong); // <--- The magic happens here

                // 4. Show the window
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Edit Song");
                stage.setScene(new javafx.scene.Scene(root));
                stage.showAndWait();

                // 5. Refresh table
                refreshSongTable();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Error if nothing selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Song Selected");
            alert.setContentText("Please select a song to edit.");
            alert.showAndWait();
        }
    }

    // --- PLAYLIST CRUD ---

    @FXML
    private void handleNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Please enter playlist name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                PlaylistDAO playlistDAO = new PlaylistDAO();
                // Capture the new object returned by your DAO
                Playlist newPlaylist = playlistDAO.createPlaylist(name);

                if (newPlaylist != null) {
                    // Add directly to table (Fast!)
                    tblPlaylists.getItems().add(newPlaylist);
                }
            }
        });
    }

    @FXML
    private void handleEditPlaylist() {
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null) {
            TextInputDialog dialog = new TextInputDialog(selectedPlaylist.getName());
            dialog.setTitle("Edit Playlist");
            dialog.setHeaderText("Rename Playlist");
            dialog.setContentText("Please enter new name:");

            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.trim().isEmpty()) {
                    selectedPlaylist.setName(newName);

                    PlaylistDAO playlistDAO = new PlaylistDAO();
                    // Use YOUR method name: renamePlaylist
                    playlistDAO.renamePlaylist(selectedPlaylist);

                    tblPlaylists.refresh();
                }
            });
        }
    }

    @FXML
    private void handleDeletePlaylist() {
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Playlist");
            alert.setHeaderText("Delete " + selectedPlaylist.getName() + "?");
            alert.setContentText("This will remove the playlist.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                PlaylistDAO playlistDAO = new PlaylistDAO();
                playlistDAO.deletePlaylist(selectedPlaylist);

                tblPlaylists.getItems().remove(selectedPlaylist);
                // Also clear the "Songs in Playlist" table since the playlist is gone
                tblSongsInPlaylist.getItems().clear();
            }
        }
    }

    @FXML
    private void handleAddToPlaylist() {
        // 1. Get both selections
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null && selectedSong != null) {
            // 2. Add to DB
            PlaylistDAO playlistDAO = new PlaylistDAO();
            // We calculate the new index (put it at the end of the list)
            int newIndex = tblSongsInPlaylist.getItems().size();

            playlistDAO.addSongToPlaylist(selectedPlaylist, selectedSong, newIndex);

            // 3. Update UI (Add to the middle table immediately)
            tblSongsInPlaylist.getItems().add(selectedSong);
        } else {
            // Error handling
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Selection Error");
            alert.setContentText("Please select a Playlist AND a Song.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRemoveFromPlaylist() {
        // 1. Get selections
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();
        Song selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null && selectedSong != null) {
            // 2. Remove from DB
            PlaylistDAO playlistDAO = new PlaylistDAO();
            playlistDAO.removeSongFromPlaylist(selectedPlaylist, selectedSong);

            // 3. Update UI (Remove from list)
            tblSongsInPlaylist.getItems().remove(selectedSong);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Selection Error");
            alert.setContentText("Please select a song in the playlist to remove.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleMoveUp() {
        // 1. Get selected index
        int index = tblSongsInPlaylist.getSelectionModel().getSelectedIndex();

        // 2. Check if we can move up (can't move the top item up)
        if (index > 0) {
            // Swap items in the list
            Song song = tblSongsInPlaylist.getItems().get(index);
            tblSongsInPlaylist.getItems().remove(index);
            tblSongsInPlaylist.getItems().add(index - 1, song);

            // Keep it selected
            tblSongsInPlaylist.getSelectionModel().select(index - 1);
        }
    }

    @FXML
    private void handleMoveDown() {
        // 1. Get selected index
        int index = tblSongsInPlaylist.getSelectionModel().getSelectedIndex();

        // 2. Check if we can move down (can't move the bottom item down)
        if (index < tblSongsInPlaylist.getItems().size() - 1 && index >= 0) {
            // Swap items in the list
            Song song = tblSongsInPlaylist.getItems().get(index);
            tblSongsInPlaylist.getItems().remove(index);
            tblSongsInPlaylist.getItems().add(index + 1, song);

            // Keep it selected
            tblSongsInPlaylist.getSelectionModel().select(index + 1);
        }
    }


    // Helper to reload data
    private void refreshPlaylists() {
        tblPlaylists.getItems().clear();
        PlaylistDAO playlistDAO = new PlaylistDAO();
        tblPlaylists.getItems().addAll(playlistDAO.getAllPlaylists());
    }

    private void refreshSongTable() {
        SongDAO songDAO = new SongDAO();
        // Clear the current table
        tblSongs.getItems().clear();
        // Add the new data directly to the table
        tblSongs.getItems().addAll(songDAO.getAllSongs());
    }


}