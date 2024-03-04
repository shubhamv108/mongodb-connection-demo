package code.shubham.entities;

import org.bson.types.ObjectId;

public class WeatherData {
    private ObjectId id;
    private long elevation;
    private String type;

    @Override
    public String toString() {
        return "WeatherData{" +
                "id=" + id +
                ", elevation=" + elevation +
                ", type='" + type + '\'' +
                '}';
    }
}
