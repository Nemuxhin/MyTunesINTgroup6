package int_group6.mytunesintgroup6.gui;

import int_group6.mytunesintgroup6.be.Song;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {



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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing Controller...");


        colTitle.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));
        colArtist.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getArtist()));
        colCategory.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategory()));
        colTime.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTime()));
        colFilePath.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFilePath()));

        try {
            Song testSong = new Song(1, "Dark Mode Song", "The Developers", "Pop", "3:45", "C:/music.mp3");
            tblSongs.getItems().add(testSong);
            System.out.println("Song added to table: " + testSong.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}