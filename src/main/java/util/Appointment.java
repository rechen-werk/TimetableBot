package util;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Objects;

/**
 * A data-class specialized to hold appointments of Type Subject.
 */
public class Appointment implements Comparable<Appointment>{
    private final Subject subject;
    private final Timestamp start, end;
    private final String location;

    // Constructor.
    public Appointment(Subject subject, Timestamp start, Timestamp end, String location) {
        this.subject = subject;
        this.start = start;
        this.end = end;
        this.location = location;
    }

    /**
     * Method to check whether the appointment overlaps with the given appointment.
     * @param other Appointment to be checked.
     * @return true if one appointment overlaps with the other, false otherwise.
     */
    public boolean overlaps(Appointment other) {
        //if start of one appointment lies between start and end of the other appointment they must overlap
        return start.compareTo(other.start) >= 0 && start.compareTo(other.end) < 0
                ||
                other.start.compareTo(start) >= 0 && other.start.compareTo(end) < 0;
    }

    // Json-ification.
    public static Appointment fromJSONObject(JSONObject obj){
        return new Appointment(
                Subject.fromJSONObject((JSONObject)obj.get("subject")),
                new Timestamp((String) obj.get("start")),
                new Timestamp((String) obj.get("end")),
                (String) obj.get("location")
        );
    }
    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("subject", subject.toJSONObject());
        obj.put("start", start.compact());
        obj.put("end", end.compact());
        obj.put("location", location);
        return obj;
    }

    // Getters.
    public String getLocation() { return location; }
    public Subject getSubject() { return subject; }
    public Timestamp getEnd() { return end; }
    public Timestamp getStart() { return start; }

    // toString, equals, hashCode and compareTo.
    @Override
    public String toString() {
        return toJSONObject().toString(2);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(subject, that.subject) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(location, that.location);
    }
    @Override
    public int hashCode() {
        return Objects.hash(subject, start, end, location);
    }
    @Override
    public int compareTo(@NotNull Appointment o) {
        int comp = start.compareTo(o.start);
        return comp != 0 ? comp : end.compareTo(o.end);
    }
}
