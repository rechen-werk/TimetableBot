package util;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Timestamps for easier use than Date, Calendar and whatsoever... although they are used here to a certain extent.
 */
public class Timestamp implements Comparable<Timestamp>{
    private final int year, month, day, hour, minute, second;

    // Constructors.
    public Timestamp() {
        Date temp = new Date();
        year = temp.getYear() + 1900;
        month = temp.getMonth() + 1;
        day = temp.getDate();
        hour = temp.getHours();
        minute = temp.getMinutes();
        second = temp.getSeconds();
    }

    public Timestamp(String icsString) {
        this(
            Integer.parseInt(icsString.substring(0,4)),
            Integer.parseInt(icsString.substring(4,6)),
            Integer.parseInt(icsString.substring(6,8)),
            Integer.parseInt(icsString.substring(9,11)),
            Integer.parseInt(icsString.substring(11,13)),
            Integer.parseInt(icsString.substring(13,15))
        );
    }

    /*
        This constructor is just public for convenience,
        if you mess up something while using this constructor it is your fault...
     */
    public Timestamp(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    // Creates a Timestamp from a String with format DD.MM.YYYY.
    public static Timestamp fromString(String string) throws IllegalArgumentException{
        String[] arr = string.split("\\.");
        try {
            string = String.format("%02d.%02d.%04d",Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));

            LocalDate d = LocalDate.parse(string , DateTimeFormatter.ofPattern ("dd.MM.uuuu").withResolverStyle ( ResolverStyle.STRICT ));
            return new Timestamp(d.getYear(), d.getMonthValue(), d.getDayOfMonth(), 0,0,0);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date!");
        }
    }

    // Builder methods.
    public Timestamp addSeconds(int seconds) {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        c.add(Calendar.SECOND, seconds);
        return new Timestamp(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
    public Timestamp addMinutes(int minutes) {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        c.add(Calendar.MINUTE, minutes);
        return new Timestamp(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
    public Timestamp addHours(int hours) {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        c.add(Calendar.HOUR_OF_DAY, hours);
        return new Timestamp(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
    public Timestamp addDays(int days) {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        c.add(Calendar.DATE, days);
        return new Timestamp(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
    public Timestamp addWeeks(int weeks) {
        return addDays(7*weeks);
    }
    public Timestamp addMonths(int months) {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        c.add(Calendar.MONTH, months);
        return new Timestamp(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }
    public Timestamp addYears(int years) {
        return new Timestamp(year+years, month, day, hour, minute, second);
    }

    // Getters.
    public int getSecond() { return second; }
    public int getMinute() { return minute; }
    public int getHour() { return hour; }
    public int getDay() { return day; }
    public int getMonth() { return month; }
    public int getYear() { return year; }

    // Extended getters.
    public Timestamp getThisMonday() {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        int d = c.getTime().getDay();
        c.add(Calendar.DATE, d == 0 ? -6 : -(d-1));
        return new Timestamp(c.getTime().getYear()+1900,c.getTime().getMonth()+1,c.getTime().getDate(), 0, 0, 0);
    }
    public Timestamp getThisSaturday() {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        int d = c.getTime().getDay();
        c.add(Calendar.DATE, d == 0 ? -6 : -(d-1));
        return new Timestamp(c.getTime().getYear()+1900,c.getTime().getMonth()+1,c.getTime().getDate(), 0, 0, 0).addDays(5);
    }
    public String getDayOfWeek() {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        return switch (c.get(Calendar.DAY_OF_WEEK)) {
            case 1 -> "Sunday";
            case 2 -> "Monday";
            case 3 -> "Tuesday";
            case 4 -> "Wednesday";
            case 5 -> "Thursday";
            case 6 -> "Friday";
            case 7 -> "Saturday";
            default -> "Whut?";
        };
    }
    public int getDayOfWeekInt() {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        return (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }
    public Calendar toCalendar() {
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day, hour, minute, second);
        return c;
    }

    // toString, equals, hashCode and compareTo.
    @Override
    public String toString() {
        String monthStr = switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> month+".";
        };
        String appendix = switch (day % 100) {
            case 1, 21, 31, 41, 51, 61, 71, 81, 91-> "st";
            case 2, 22, 32, 42, 52, 62, 72, 82, 92-> "nd";
            case 3, 23, 33, 43, 53, 63, 73, 83, 93-> "rd";
            default -> "th";
        };
        if(second == 0) {
            return String.format("%s %d%s, %4d, %02d:%02d", monthStr, day, appendix, year, hour, minute);
        } else {
            return String.format("%s %d%s, %4d, %02d:%02d:%02d", monthStr, day, appendix, year, hour, minute, second);
        }

    }
    // Extended toString methods.
    public String toDateString() {
        String monthStr = switch (month) {
            case 1 -> "January";
            case 2 -> "February";
            case 3 -> "March";
            case 4 -> "April";
            case 5 -> "May";
            case 6 -> "June";
            case 7 -> "July";
            case 8 -> "August";
            case 9 -> "September";
            case 10 -> "October";
            case 11 -> "November";
            case 12 -> "December";
            default -> month+".";
        };
        String appendix = switch (day % 100) {
            case 1, 21, 31, 41, 51, 61, 71, 81, 91-> "st";
            case 2, 22, 32, 42, 52, 62, 72, 82, 92-> "nd";
            case 3, 23, 33, 43, 53, 63, 73, 83, 93-> "rd";
            default -> "th";
        };
        return String.format("%s %d%s, %4d", monthStr, day, appendix, year);
    }
    public String toHourString() {
        if(second == 0) {
            return String.format("%02d:%02d", hour, minute);
        } else {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        }
    }
    public String compact() {
        return String.format("%04d%02d%02dT%02d%02d%02d", year, month, day, hour, minute, second);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timestamp timestamp = (Timestamp) o;
        return year == timestamp.year &&
                month == timestamp.month &&
                day == timestamp.day &&
                hour == timestamp.hour &&
                minute == timestamp.minute &&
                second == timestamp.second;
    }
    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, hour, minute, second);
    }
    @Override
    public int compareTo(@NotNull Timestamp o) {
        if(year != o.year) return year - o.year;
        if(month != o.month) return month - o.month;
        if(day != o.day) return day - o.day;
        if(hour != o.hour) return hour - o.hour;
        if(minute != o.minute) return  minute - o.minute;
        return second - o.second;
    }
}