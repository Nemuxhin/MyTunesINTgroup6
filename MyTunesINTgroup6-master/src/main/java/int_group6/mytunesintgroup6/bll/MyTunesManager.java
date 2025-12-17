package int_group6.mytunesintgroup6.bll;

import int_group6.mytunesintgroup6.be.Playlist;
import int_group6.mytunesintgroup6.be.Song;
import int_group6.mytunesintgroup6.dal.PlaylistDAO;
import int_group6.mytunesintgroup6.dal.SongDAO;

import java.util.List;

/**
 * MyTunesManager is the Business Logic Layer (BLL) entry point.
 *
 * It provides a clean API for the GUI layer, while delegating persistence
 * to the DAO classes (DAL). Keeping the GUI talking to the BLL makes the
 * architecture consistent with the MyTunes project guidelines.
 */
public class MyTunesManager {

    private final SongDAO songDAO;
    private final PlaylistDAO playlistDAO;

    public MyTunesManager() {
        this.songDAO = new SongDAO();
        this.playlistDAO = new PlaylistDAO();
    }

    // -------------------- SONGS --------------------

    public List<Song> getAllSongs() {
        return songDAO.getAllSongs();
    }

    public void createSong(Song song) {
        songDAO.createSong(song);
    }

    public void updateSong(Song song) {
        songDAO.updateSong(song);
    }

    public void deleteSong(Song song) {
        songDAO.deleteSong(song);
    }

    // -------------------- PLAYLISTS --------------------

    public List<Playlist> getAllPlaylists() {
        return playlistDAO.getAllPlaylists();
    }

    public Playlist createPlaylist(String name) {
        return playlistDAO.createPlaylist(name);
    }

    public void renamePlaylist(Playlist playlist) {
        playlistDAO.renamePlaylist(playlist);
    }

    public void deletePlaylist(Playlist playlist) {
        playlistDAO.deletePlaylist(playlist);
    }

    // -------------------- PLAYLIST SONGS --------------------

    public List<Song> getSongsInPlaylist(Playlist playlist) {
        return playlistDAO.getSongsInPlaylist(playlist);
    }

    public void addSongToPlaylist(Playlist playlist, Song song, int index) {
        playlistDAO.addSongToPlaylist(playlist, song, index);
    }

    public void removeSongFromPlaylist(Playlist playlist, Song song) {
        playlistDAO.removeSongFromPlaylist(playlist, song);
        // Keep SongIndex clean after a removal so ordering stays predictable.
        playlistDAO.updatePlaylistOrder(playlist, playlistDAO.getSongsInPlaylist(playlist));
    }

    public void updatePlaylistOrder(Playlist playlist, List<Song> orderedSongs) {
        playlistDAO.updatePlaylistOrder(playlist, orderedSongs);
    }
}
