package int_group6.mytunesintgroup6.dal;

import int_group6.mytunesintgroup6.be.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {

    // Gestisce la connessione al database
    private final ConnectionProvider connectionProvider;

    public SongDAO() {
        connectionProvider = new ConnectionProvider();
    }

    // CREATE: add new song
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

    // READ: leggere tutte le canzoni
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
            e.printStackTrace();
        }

        return allSongs;
    }

    // DELETE: cancellare una canzone
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

    // (Opzionale) UPDATE: modificare una canzone esistente
    public void updateSong(Song song) {
        String sql = "UPDATE Song SET Title = ?, Artist = ?, Category = ?, Time = ?, FilePath = ? WHERE Id = ?";

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
}
