import java.time.*;
import java.time.format.*;

public class DayTime implements Comparable<DayTime> {
    public final String ILLEGAL_DATE = "Invalid departure time. Use the format <day_of_week> <hour:minute>, with 24h time.";

    @Override
    public int compareTo(DayTime o) {
        return DayTime.compare(this, o);
    }

    public enum weekDay {
        Monday(0), Tuesday(1), Wednesday(2), Thursday(3), Friday(4), Saturday(5), Sunday(6) {
            @Override
            public weekDay next() {
                return values()[0]; // see below for options for this line
            };
        };

        public weekDay next() {
            // No bounds checking required here, because the last instance overrides
            return values()[ordinal() + 1];
        }

        public String toString() {
            return name();
        }

        private final int numDay;

        weekDay(int numDay) {
            this.numDay = numDay;
        }

        public int getNumDay() {
            return this.numDay;
        }
    };

    private weekDay dayOfWeek;
    private LocalTime flightTime;

    public DayTime(String dayTimeString) {
        String[] dayTimes = dayTimeString.split(" ");
        LocalTime flightTime;
        weekDay dayOfWeek;
        if (dayTimes.length != 2)
            throw new IllegalArgumentException(ILLEGAL_DATE);
        try {
            String specificHour = dayTimeString.split(" ")[0];
            DateTimeFormatter formatter;
            if (specificHour.length() == 2)
                formatter = DateTimeFormatter.ofPattern("HH:mm");
            else
                formatter = DateTimeFormatter.ofPattern("H:mm");
            flightTime = LocalTime.parse(dayTimes[1], formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException(ILLEGAL_DATE);
        }
        switch (dayTimes[0].toUpperCase()) {
        case "MONDAY":
            dayOfWeek = weekDay.Monday;
            break;
        case "TUESDAY":
            dayOfWeek = weekDay.Tuesday;
            break;
        case "WEDNESDAY":
            dayOfWeek = weekDay.Wednesday;
            break;
        case "THURSDAY":
            dayOfWeek = weekDay.Thursday;
            break;
        case "FRIDAY":
            dayOfWeek = weekDay.Friday;
            break;
        case "SATURDAY":
            dayOfWeek = weekDay.Saturday;
            break;
        case "SUNDAY":
            dayOfWeek = weekDay.Sunday;
            break;
        default:
            throw new IllegalArgumentException(ILLEGAL_DATE);

        }
        this.dayOfWeek = dayOfWeek;
        this.flightTime = flightTime;
    }

    public DayTime(weekDay dayOfWeek, LocalTime flightTime) {
        this.dayOfWeek = dayOfWeek;
        this.flightTime = flightTime;
    }

    public static boolean isConflicted(DayTime d1, DayTime d2) {
        int maxTime = 24 * 7 * 60;
        // get number of minutes has passed since the start of Monday
        int timeSum1 = d1.getSumMinute();
        int timeSum2 = d2.getSumMinute();

        if (Math.abs(timeSum2 - timeSum1) <= 60 || maxTime - Math.abs(timeSum2 - timeSum1) <= 60)
            return true;

        return false;
    }

    public int getSumMinute() {
        return (this.getDayOfWeek().getNumDay() * 24 + this.getFlightTime().getHour()) * 60
                + this.getFlightTime().getMinute();
    }

    public static int compare(DayTime d1, DayTime d2) {
        int sumMin1 = d1.getSumMinute();
        int sumMin2 = d2.getSumMinute();

        if (sumMin1 > sumMin2)
            return 1;
        else if (sumMin1 < sumMin2)
            return -1;

        return 0;
    }

    // printing format for DayTime
    public String toString() {
        StringBuilder temp = new StringBuilder();
        temp.append(this.getDayOfWeek().toString().substring(0, 1).toUpperCase());
        temp.append(this.getDayOfWeek().toString().substring(1, 3));
        temp.append(" ");
        temp.append(this.getFlightTime().toString());

        return temp.toString();
    }

    public String toFullString() {
        StringBuilder temp = new StringBuilder();
        temp.append(this.getDayOfWeek().toString());
        temp.append(" ");
        temp.append(this.getFlightTime().toString());

        return temp.toString();
    }

    public LocalTime getFlightTime() {
        return this.flightTime;
    }

    public void setFlightTime(LocalTime flightTime) {
        this.flightTime = flightTime;
    }

    public weekDay getDayOfWeek() {
        return this.dayOfWeek;
    }

    public void setDayOfWeek(weekDay dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

}
