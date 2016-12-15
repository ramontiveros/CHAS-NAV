package mx.itesm.chas.chas;

/**
 * Created by Christopher on 11/17/2016.
 */

public class Video {
    public String title, length, date, matchId;

    public Video() {

    }

    public Video(String title, String length, String date, String matchId) {
        this.title = title;
        this.length = length;
        this.date = date;
        this.matchId = matchId;
    }
}