package int_group6.mytunesintgroup6.dal;

import int_group6.mytunesintgroup6.be.Song;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all the database operations for Songs.
 * We chose to use PreparedStatement everywhere to prevent SQL injection attacks.
 */
public class SongDAO {

    private final ConnectionProvider connectionProvider;

    public SongDAO() {
        connectionProvider = new ConnectionProvider();
    }

    /**
     * Reads all songs from the database.
     * We convert each database row into a Song object to use in the GUI.
     */
    public List<Song> getAllSongs() {
        List<Song> allSongs = new ArrayList<>();
        String sql = "SELECT * FROM Song";

        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("Id");
                String title = rs.getString("Title");
                String artist = rs.getString("Artist");
                String category = rs.getString("Category");
                String time = rs.getString("Time");
                String filePath = rs.getString("FilePath");

                Song song = new Song(id, title, artist, category, time, filePath);
                allSongs.add(song);
            }
        } catch (SQLException e) {
            // If something goes wrong, print it to the console so we can debug
            e.printStackTrace();
        }
        return allSongs;
    }

    /**
     * Saves a new song to the DB.
     * Note: We don't set the ID here because SQLite handles that automatically (AUTOINCREMENT).
     */
    public void createSong(Song song) {
        String sql = "INSERT INTO Song (Title, Artist, Category, Time, FilePath) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getCategory());
            pstmt.setString(4, song.getTime());
            pstmt.setString(5, song.getFilePath());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing song.
     * We match the song by its unique ID to make sure we don't update the wrong row.
     */
    public void updateSong(Song song) {
        String sql = "UPDATE Song SET Title=?, Artist=?, Category=?, Time=?, FilePath=? WHERE Id=?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, song.getTitle());
            pstmt.setString(2, song.getArtist());
            pstmt.setString(3, song.getCategory());
            pstmt.setString(4, song.getTime());
            pstmt.setString(5, song.getFilePath());
            pstmt.setInt(6, song.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a song.
     * TODO: In the future, we should probably check if the song is in a playlist before deleting it to avoid errors.
     */
    public void deleteSong(Song song) {
        String sql = "DELETE FROM Song WHERE Id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, song.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}