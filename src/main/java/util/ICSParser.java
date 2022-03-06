package util;

import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * An ics-file parser, that receives an ics-file from KUSSS (JKU) and creates a list of all appointments as Appointment
 * objects in java. These Objects are designed in a way, that they are easily printable as a json-file.
 */
public class ICSParser {
    public static List<Appointment> parse(File icsFile) throws IllegalArgumentException {
        List<Appointment> appointments = new ArrayList<>();
        try {
            Scanner s = new Scanner(icsFile);
            String line = "";
            // While not done.
            while (!line.equals("END:VCALENDAR")) {
                // If new event.
                if (line.equals("BEGIN:VEVENT")) {
                    String start = "", end = "", summary = "", location = "";
                    while (!line.equals("END:VEVENT")) {
                        if(line.startsWith("DTSTART")) {
                            start = line.substring(line.length()-15);
                        } else if (line.startsWith("DTEND")) {
                            end = line.substring(line.length()-15);
                        } else if (line.startsWith("LOCATION")) {
                            location = line.substring(9).replaceAll("\\s+$", "").replace("\\", "");
                        } else if (line.startsWith("SUMMARY")) {
                            summary = line.substring(8);
                        }
                        if(s.hasNextLine()) line = s.nextLine();
                        else throw new IllegalArgumentException("ICS-File is invalid");
                    }
                    // Example: "KV Special Topics / Hanspeter Mössenböck / (339341/2022S)".
                    // Split by the slashes.
                    String[] summaryArray = summary.split(" / ");
                    if(summaryArray.length == 4) {
                        // 4 entries means, that someone has made an extra field with "Prüfung" or something at index 0.
                        summaryArray[0] = summaryArray[1];
                        summaryArray[1] = summaryArray[2];
                        summaryArray[2] = summaryArray[3];
                    }
                    // LVA-type is always in the first 2 letters.
                    String type = summaryArray[0].substring(0,2);
                    // Title is the rest, separated by a whitespace.
                    String title = summaryArray[0].substring(3);
                    // Lecturer is in the second part of the array.
                    String lecturer = summaryArray[1];
                    // And the semester and LVAnr (339280/2021W)
                    String semester = summaryArray[2].substring(8,13);
                    int lvanr = Integer.parseInt(summaryArray[2].substring(1,7));
                    appointments.add(new Appointment(
                            new Subject(type, title, lecturer, semester, lvanr),
                            new Timestamp(start),
                            new Timestamp(end),
                            location)
                    );
                }
                // Else scan until event or end.
                if(s.hasNextLine()) line = s.nextLine();
                else throw new IllegalArgumentException("ICS-File is invalid.");
            }
            s.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }

        Collections.sort(appointments);
        return appointments;
    }

    public static void parseToFile(File icsFile, File destination) {
        List<Appointment> appointments = parse(icsFile);
        try {
            FileWriter fw = new FileWriter(destination);
            JSONArray array = new JSONArray();
            for (Appointment appointment: appointments) array.put(appointment.toJSONObject());
            fw.write(array.toString(2));
            fw.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void parseToFile(String icsFile, String destination) {
        parseToFile(new File(icsFile), new File(destination));
    }
}