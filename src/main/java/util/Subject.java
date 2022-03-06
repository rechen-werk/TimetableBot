package util;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Data-class that holds the information about the specific subject.
 * Used in class Appointment.
 */
public class Subject implements Comparable<Subject>{
    private final String type;
    private final String title;
    private final String lecturer;
    private final String semester;
    private final int lvanr;

    // Constructor.
    public Subject(String type, String title, String lecturer, String semester, int lvanr) {
        this.type = type;
        this.title = title;
        this.lecturer = lecturer;
        this.semester = semester;
        this.lvanr = lvanr;
    }

    // Getters.
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getLecturer() { return lecturer; }
    public int getLvanr() { return lvanr; }
    public String getSemester() { return semester; }

    // Json-ification.
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("type", type);
        obj.put("title", title);
        obj.put("lecturer", lecturer);
        obj.put("semester", semester);
        obj.put("lvanr", lvanr);
        return obj;
    }

    public static Subject fromJSONObject(JSONObject obj) {
        return new Subject(
                (String) obj.get("type"),
                (String) obj.get("title"),
                (String) obj.get("lecturer"),
                (String) obj.get("semester"),
                (int) obj.get("lvanr")
        );
    }

    // toString, equals, hashCode and compareTo.
    @Override
    public String toString() {
        return toJSONObject().toString(2);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return lvanr == subject.lvanr &&
                type.equals(subject.type) &&
                Objects.equals(title, subject.title) &&
                Objects.equals(lecturer, subject.lecturer) &&
                Objects.equals(semester, subject.semester);
    }
    @Override
    public int hashCode() {
        return Objects.hash(type, title, lecturer, semester, lvanr);
    }
    @Override
    public int compareTo(@NotNull Subject o) {
        return lvanr - o.lvanr;
    }
}