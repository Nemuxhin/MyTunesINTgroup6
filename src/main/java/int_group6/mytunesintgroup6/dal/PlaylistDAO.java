package int_group6.mytunesintgroup6.dal;

import int_group6.mytunesintgroup6.be.Playlist;
import int_group6.mytunesintgroup6.be.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {

    // Responsible for creating database connections
    private final ConnectionProvider connectionProvider;

    public PlaylistDAO() {
        connectionProvider = new ConnectionProvider();
    }


    // READ: Retrieve all playlists from the database

    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT * FROM Playlist";

        // Try-with-resources ensures everything is closed automatically
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Loop through all rows in the playlist table
            while (rs.next()) {
                int id = rs.getInt("Id");
                String name = rs.getString("Name");

                // Convert each row into a Playlist object
                Playlist playlist = new Playlist(id, name);
                playlists.add(playlist);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playlists;
    }


    // CREATE: Add a new playlist to the database
    // Returns the Playlist object including its generated ID

    public Playlist createPlaylist(String name) {
        String sql = "INSERT INTO Playlist (Name) VALUES (?)";

        // RETURN_GENERATED_KEYS allows us to get the ID assigned by the database
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();

            // Read the auto-generated playlist ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new Playlist(id, name);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // In case something goes wrong
    }


    // DELETE: Remove a playlist and all its song relations
    // Uses a transaction to ensure database consistency

    public void deletePlaylist(Playlist playlist) {
        String deleteLinksSql = "DELETE FROM PlaylistSong WHERE PlaylistId = ?";
        String deletePlaylistSql = "DELETE FROM Playlist WHERE Id = ?";

        try (Connection conn = connectionProvider.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement pstmtLinks = conn.prepareStatement(deleteLinksSql);
                 PreparedStatement pstmtPlaylist = conn.prepareStatement(deletePlaylistSql)) {

                // Step 1: Remove all song relations from PlaylistSong
                pstmtLinks.setInt(1, playlist.getId());
                pstmtLinks.executeUpdate();

                // Step 2: Remove the playlist itself
                pstmtPlaylist.setInt(1, playlist.getId());
                pstmtPlaylist.executeUpdate();

                conn.commit(); // Everything successful

            } catch (SQLException e) {
                conn.rollback(); // Undo changes on failure
                throw e;

            } finally {
                conn.setAutoCommit(true); // Restore default behavior
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // READ: Retrieve all songs assigned to a specific playlist
    // Ordered by SongIndex to maintain playlist order

    public List<Song> getSongsInPlaylist(Playlist playlist) {
        List<Song> songs = new ArrayList<>();

        String sql =
                "SELECT s.Id, s.Title, s.Artist, s.Category, s.Time, s.FilePath " +
                        "FROM Song s " +
                        "JOIN PlaylistSong ps ON s.Id = ps.SongId " +
                        "WHERE ps.PlaylistId = ? " +
                        "ORDER BY ps.SongIndex";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlist.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("Id");
                    String title = rs.getString("Title");
                    String artist = rs.getString("Artist");
                    String category = rs.getString("Category");
                    String time = rs.getString("Time");
                    String filePath = rs.getString("FilePath");

                    Song song = new Song(id, title, artist, category, time, filePath);
                    songs.add(song);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return songs;
    }


    // ADD: Link a song to a playlist in a specific position
    // SongIndex determines the order of songs in the playlist

    public void addSongToPlaylist(Playlist playlist, Song song, int index) {
        String sql = "INSERT INTO PlaylistSong (PlaylistId, SongId, SongIndex) VALUES (?, ?, ?)";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlist.getId());
            pstmt.setInt(2, song.getId());
            pstmt.setInt(3, index);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // REMOVE: Delete the link between a playlist and a song
    // The song remains in the Song table (only unlinked)

    public void removeSongFromPlaylist(Playlist playlist, Song song) {
        String sql = "DELETE FROM PlaylistSong WHERE PlaylistId = ? AND SongId = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playlist.getId());
            pstmt.setInt(2, song.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // UPDATE: Rename an existing playlist

    public void renamePlaylist(Playlist playlist) {
        String sql = "UPDATE Playlist SET Name = ? WHERE Id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playlist.getName());
            pstmt.setInt(2, playlist.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
