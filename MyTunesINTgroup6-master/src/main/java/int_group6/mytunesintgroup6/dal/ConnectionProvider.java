package int_group6.mytunesintgroup6.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionProvider {

    private static final String URL = "jdbc:sqlite:mytunes.db";

    public ConnectionProvider() {
        createTables();
        seedData(); // <--- New method to add dummy data automatically
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void createTables() {
        String sql = """
            CREATE TABLE IF NOT EXISTS Song (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                Title TEXT,
                Artist TEXT,
                Category TEXT,
                Time TEXT,
                FilePath TEXT
            );
            CREATE TABLE IF NOT EXISTS Playlist (
                Id INTEGER PRIMARY KEY AUTOINCREMENT,
                Name TEXT
            );
            CREATE TABLE IF NOT EXISTS PlaylistSong (
                PlaylistId INTEGER,
                SongId INTEGER,
                SongIndex INTEGER,
                FOREIGN KEY (PlaylistId) REFERENCES Playlist(Id),
                FOREIGN KEY (SongId) REFERENCES Song(Id)
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This method adds data ONLY if the table is empty
    private void seedData() {
        String countSql = "SELECT COUNT(*) FROM Song";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if we already have songs
            if (stmt.executeQuery(countSql).getInt(1) == 0) {
                System.out.println("Database is empty. Adding dummy songs...");

                String insertSql = """
                    INSERT INTO Song (Title, Artist, Category, Time, FilePath) VALUES 
                    ('Piano Man', 'Billy Joel', 'Pop', '5:38', 'data/pianoman.mp3'),
                    ('Darkside', 'iann dior ft. Travis Barker', 'Pop', '2:42', 'data/iann dior - Darkside ft. Travis Barker [Official Music Video].mp3');
                """;
                stmt.executeUpdate(insertSql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}