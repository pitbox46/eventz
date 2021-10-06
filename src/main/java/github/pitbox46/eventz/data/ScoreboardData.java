package github.pitbox46.eventz.data;

import jdk.nashorn.api.scripting.JSObject;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardData {
    public final Map<String, Integer> scores;

    private ScoreboardData(Map<String, Integer> scores) {
        this.scores = scores;
    }

    public static ScoreboardData readFromJSObject(JSObject scoreboardData) {
        Map<String, Integer> scores = new HashMap<>();
        for(String key: scoreboardData.keySet()) {
            Object value = scoreboardData.getMember(key);
            if(value instanceof Number)
                scores.put(key, ((Number) scoreboardData.getMember(key)).intValue());
        }
        return new ScoreboardData(scores);
    }
}
