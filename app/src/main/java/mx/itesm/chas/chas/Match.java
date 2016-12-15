package mx.itesm.chas.chas;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris on 12/15/2016.
 */

public class Match {
    public String name, date;
    public Map<String, Boolean> teams = new HashMap<>();

    public Match() {

    }

    public Match(String name, String date, Map<String, Boolean> teams) {
        this.name = name;
        this.date = date;
        this.teams = teams;
    }
}
