package int_group6.mytunesintgroup6.gui;

import int_group6.mytunesintgroup6.be.Playlist;
import int_group6.mytunesintgroup6.bll.MyTunesManager;
import javafx.util.Duration;
import int_group6.mytunesintgroup6.be.Song;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

/**
 * Main controller for the MyTunes UI.
 *
 * Samu: This controller focuses on UI behavior and delegates data operations to the
 * Business Logic Layer (MyTunesManager), which in turn uses the DAO classes.
 */
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

    @FXML private Button btnNewPlaylist, btnEditPlaylist, btnDeletePlaylist;
    @FXML private Button btnAddToPlaylist, btnMoveUp, btnMoveDown, btnRemoveFromPlaylist;
    @FXML private Button btnNewSong, btnEditSong, btnDeleteSong, btnClose;

    @FXML private Slider songSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblTotalTime;

    @FXML private TableView<Playlist> tblPlaylists;
    @FXML private TableColumn<Playlist, String> colPlaylistName;
    
    @FXML private Button btnRewind;
    @FXML private Button btnFastForward;
    @FXML private javafx.scene.web.WebView youtubeView;


    // --- LOGIC VARIABLES ---
    private MyTunesManager manager;
    private MediaPlayer mediaPlayer;
    private javafx.collections.ObservableList<Song> allSongs;
    private javafx.collections.transformation.FilteredList<Song> filteredSongs;

    // A variable to remember the playlist even if the table loses focus
    private Playlist storedPlaylist = null;

    public void setManager(MyTunesManager manager) {
        this.manager = manager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing Controller...");

        // --- 1. SETUP COLUMNS ---
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        colPlaylistName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPlSongTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colPlSongArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        colPlSongCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPlSongTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        // --- 2. SETUP DATA ---
        if (manager == null) manager = new MyTunesManager();

        allSongs = javafx.collections.FXCollections.observableArrayList(manager.getAllSongs());
        filteredSongs = new javafx.collections.transformation.FilteredList<>(allSongs, b -> true);
        javafx.collections.transformation.SortedList<Song> sortedData = new javafx.collections.transformation.SortedList<>(filteredSongs);
        sortedData.comparatorProperty().bind(tblSongs.comparatorProperty());
        tblSongs.setItems(sortedData);

        // --- 3. SEARCH LISTENER ---
        txtFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) btnFilterClear.setText("Clear");
            else btnFilterClear.setText("Filter");

            filteredSongs.setPredicate(song -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return song.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        song.getArtist().toLowerCase().contains(lowerCaseFilter);
            });
        });

        // --- 4. SELECTION LOGIC (FIXED) ---

        // A. When clicking "All Songs", just clear the visual selection of playlist, BUT keep 'storedPlaylist'
        tblSongs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tblPlaylists.getSelectionModel().clearSelection();
                // Do NOT set storedPlaylist to null here, or add button breaks!
            }
        });

        // B. When clicking "Playlist", save it to 'storedPlaylist'
        tblPlaylists.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                storedPlaylist = newVal; // <--- SAVE SELECTION HERE
                tblSongs.getSelectionModel().clearSelection();
                tblSongsInPlaylist.getItems().setAll(manager.getSongsInPlaylist(newVal));
            }
        });

        // C. When clicking "Playlist Songs"
        tblSongsInPlaylist.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                tblSongs.getSelectionModel().clearSelection();
            }
        });

        // --- 5. BUTTON ACTIONS ---
        btnPlay.setOnAction(event -> playSong());
        btnPause.setOnAction(event -> pauseSong());
        btnStop.setOnAction(event -> stopSong());

        if (btnRewind != null) btnRewind.setOnAction(event -> rewindSong());
        if (btnFastForward != null) btnFastForward.setOnAction(event -> fastForwardSong());
        if (btnClose != null) btnClose.setOnAction(event -> closeApp());

        btnNewSong.setOnAction(event -> openNewSongWindow());
        btnEditSong.setOnAction(event -> editSong());
        btnDeleteSong.setOnAction(event -> deleteSong());

        btnNewPlaylist.setOnAction(event -> handleNewPlaylist());
        btnEditPlaylist.setOnAction(event -> handleEditPlaylist());
        btnDeletePlaylist.setOnAction(event -> handleDeletePlaylist());

        btnAddToPlaylist.setOnAction(event -> handleAddToPlaylist());
        btnRemoveFromPlaylist.setOnAction(event -> handleRemoveFromPlaylist());
        btnMoveUp.setOnAction(event -> handleMoveUp());
        btnMoveDown.setOnAction(event -> handleMoveDown());

        btnFilterClear.setOnAction(event -> {
            if (!txtFilter.getText().isEmpty()) txtFilter.clear();
        });

        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (mediaPlayer != null) mediaPlayer.setVolume(newValue.doubleValue() / 100);
            });
        }

        if (youtubeView != null) {
            youtubeView.managedProperty().bind(youtubeView.visibleProperty());
        }

        refreshPlaylists();
    }

    // --- MEDIA PLAYER CONTROLS ---

    @FXML
    private void playSong() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            mediaPlayer.play();
            lblNowPlaying.setText("Playing: " + getCurrentSongTitle());
            return;
        }

        Song selectedSong = null;
        if (tblSongsInPlaylist.getSelectionModel().getSelectedItem() != null) {
            selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();
        } else if (tblSongs.getSelectionModel().getSelectedItem() != null) {
            selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        }

        if (selectedSong != null) {
            playMediaFile(selectedSong);
        } else {
            lblNowPlaying.setText("Please select a song.");
        }
    }

    private void playMediaFile(Song song) {
        String path = song.getFilePath();
        if (mediaPlayer != null) mediaPlayer.stop();

        if (path.toLowerCase().startsWith("http")) {
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    System.out.println("Opening in browser: " + path);
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(path));
                    lblNowPlaying.setText("Playing in Browser: " + song.getTitle());
                } else {
                    System.out.println("Browse action not supported on this system.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                lblNowPlaying.setText("Error opening link.");
            }
        } else {
            File file = new File(path);
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                if (volumeSlider != null) mediaPlayer.setVolume(volumeSlider.getValue() / 100);
                setupProgressBar();

                mediaPlayer.setOnEndOfMedia(this::playNextSong);
                mediaPlayer.play();
                lblNowPlaying.setText(song.getTitle() + " ... is playing");
            } else {
                System.out.println("File not found: " + path);
                lblNowPlaying.setText("File not found.");
            }
        }
    }

    @FXML
    private void playNextSong() {
        TableView<Song> activeTable = null;
        if (tblSongsInPlaylist.getSelectionModel().getSelectedItem() != null) activeTable = tblSongsInPlaylist;
        else if (tblSongs.getSelectionModel().getSelectedItem() != null) activeTable = tblSongs;

        if (activeTable != null) {
            int currentIndex = activeTable.getSelectionModel().getSelectedIndex();
            int totalSize = activeTable.getItems().size();
            int nextIndex = currentIndex + 1;

            if (nextIndex < totalSize) {
                activeTable.getSelectionModel().select(nextIndex);
                playMediaFile(activeTable.getItems().get(nextIndex));
            } else {
                lblNowPlaying.setText("End of Playlist.");
                if (mediaPlayer != null) mediaPlayer.stop();
            }
        }
    }

    private String getCurrentSongTitle() {
        if (mediaPlayer != null) return lblNowPlaying.getText().replace("Playing: ", "");
        return "...";
    }

    @FXML
    private void rewindSong() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(10)));
        }
    }

    @FXML
    private void fastForwardSong() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
        }
    }

    @FXML
    private void closeApp() {
        if (mediaPlayer != null) mediaPlayer.stop();
        javafx.application.Platform.exit();
        System.exit(0);
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
            songSlider.setValue(0);
            lblCurrentTime.setText("0:00");
        }
    }

    private void setupProgressBar() {
        mediaPlayer.setOnReady(() -> {
            double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
            songSlider.setMax(totalDuration);
            lblTotalTime.setText(formatTime(totalDuration));
        });
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!songSlider.isValueChanging()) {
                songSlider.setValue(newValue.toSeconds());
                lblCurrentTime.setText(formatTime(newValue.toSeconds()));
            }
        });
        songSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (songSlider.isValueChanging()) lblCurrentTime.setText(formatTime(newValue.doubleValue()));
        });
        songSlider.setOnMouseReleased(event -> mediaPlayer.seek(Duration.seconds(songSlider.getValue())));
    }

    private String formatTime(double timeSeconds) {
        int minutes = (int) timeSeconds / 60;
        int seconds = (int) timeSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // --- POPUPS & CRUD ---

    @FXML
    private void openNewSongWindow() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("/int_group6/mytunesintgroup6/new-song-view.fxml"));
            javafx.scene.Parent root = fxmlLoader.load();

            NewSongController controller = fxmlLoader.getController();
            controller.setManager(manager);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Add New Song");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            refreshSongTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSong() {
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Song");
            alert.setHeaderText("Delete " + selectedSong.getTitle() + "?");
            alert.setContentText("This cannot be undone.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                manager.deleteSong(selectedSong);
                allSongs.remove(selectedSong);
                Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();
                if (selectedPlaylist != null) {
                    tblSongsInPlaylist.getItems().setAll(manager.getSongsInPlaylist(selectedPlaylist));
                }
            }
        }
    }

    @FXML
    private void editSong() {
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();
        if (selectedSong != null) {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/int_group6/mytunesintgroup6/new-song-view.fxml"));
                javafx.scene.Parent root = loader.load();

                NewSongController controller = loader.getController();
                controller.setManager(manager);
                controller.setSongToEdit(selectedSong);

                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Edit Song");
                stage.setScene(new javafx.scene.Scene(root));
                stage.showAndWait();
                refreshSongTable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --- PLAYLIST LOGIC ---

    @FXML
    private void handleNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Please enter playlist name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Playlist newPlaylist = manager.createPlaylist(name);
                if (newPlaylist != null) tblPlaylists.getItems().add(newPlaylist);
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
                    manager.renamePlaylist(selectedPlaylist);
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
            alert.setContentText("Delete " + selectedPlaylist.getName() + "?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                manager.deletePlaylist(selectedPlaylist);
                tblPlaylists.getItems().remove(selectedPlaylist);
                tblSongsInPlaylist.getItems().clear();
            }
        }
    }

    // Logic for Adding Songs
    @FXML
    private void handleAddToPlaylist() {
        // Use 'storedPlaylist' because the table might be visually empty
        Playlist targetPlaylist = storedPlaylist;
        Song selectedSong = tblSongs.getSelectionModel().getSelectedItem();

        if (targetPlaylist != null && selectedSong != null) {
            int newIndex = tblSongsInPlaylist.getItems().size();
            manager.addSongToPlaylist(targetPlaylist, selectedSong, newIndex);

            // If the currently viewed list matches the target, update it instantly
            if (storedPlaylist.equals(targetPlaylist)) {
                tblSongsInPlaylist.getItems().add(selectedSong);
            }
            System.out.println("Added " + selectedSong.getTitle() + " to " + targetPlaylist.getName());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Selection Error");
            alert.setContentText("Please select a Playlist first, then a Song.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRemoveFromPlaylist() {
        Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();
        Song selectedSong = tblSongsInPlaylist.getSelectionModel().getSelectedItem();

        if (selectedPlaylist != null && selectedSong != null) {
            manager.removeSongFromPlaylist(selectedPlaylist, selectedSong);
            tblSongsInPlaylist.getItems().remove(selectedSong);
        }
    }

    @FXML
    private void handleMoveUp() {
        int index = tblSongsInPlaylist.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            Song song = tblSongsInPlaylist.getItems().get(index);
            tblSongsInPlaylist.getItems().remove(index);
            tblSongsInPlaylist.getItems().add(index - 1, song);
            tblSongsInPlaylist.getSelectionModel().select(index - 1);

            Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null) {
                manager.updatePlaylistOrder(selectedPlaylist, tblSongsInPlaylist.getItems());
            }
        }
    }

    @FXML
    private void handleMoveDown() {
        int index = tblSongsInPlaylist.getSelectionModel().getSelectedIndex();
        if (index < tblSongsInPlaylist.getItems().size() - 1 && index >= 0) {
            Song song = tblSongsInPlaylist.getItems().get(index);
            tblSongsInPlaylist.getItems().remove(index);
            tblSongsInPlaylist.getItems().add(index + 1, song);
            tblSongsInPlaylist.getSelectionModel().select(index + 1);

            Playlist selectedPlaylist = tblPlaylists.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null) {
                manager.updatePlaylistOrder(selectedPlaylist, tblSongsInPlaylist.getItems());
            }
        }
    }

    private void refreshPlaylists() {
        tblPlaylists.getItems().clear();
        tblPlaylists.getItems().addAll(manager.getAllPlaylists());
    }

    private void refreshSongTable() {
        allSongs.setAll(manager.getAllSongs());
    }
}
