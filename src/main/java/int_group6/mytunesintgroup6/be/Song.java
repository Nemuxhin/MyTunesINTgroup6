package int_group6.mytunesintgroup6.be;

public class Song {
    private int id;
    private String title;
    private String artist;
    private String category;
    private String time;
    private String filePath;

    public Song(int id, String title, String artist, String category, String time, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.category = category;
        this.time = time;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getCategory() { return category; }
    public String getTime() { return time; }
    public String getFilePath() { return filePath; }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}