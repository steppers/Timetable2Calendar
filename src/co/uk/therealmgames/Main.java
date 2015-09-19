package co.uk.therealmgames;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Main {

    private static final int YEAR = 2015;
    private static final int MONTH = Calendar.SEPTEMBER;
    private static final int DAY = 21;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    private static FileReader fileReader;
    private static BufferedReader reader;

    private static FileWriter fileWriter;
    private static BufferedWriter writer;

    //Arg1 must be a valid html file from sussed in the form of https://timetable.soton.ac.uk/Home/Semester/1/
    public static void main(String[] args)
    {
        try {
            //Read the file
            fileReader = new FileReader(args[0]);
            reader = new BufferedReader(fileReader);

            //Create the csv file
            fileWriter = new FileWriter(args[0].replaceAll("\\..*", ".csv"));
            writer = new BufferedWriter(fileWriter);
            writeHeader();

            String line;
            while((line = reader.readLine()) != null)
            {
                line = line.trim();
                if(line.startsWith("<td>"))
                {
                    processLine(line);
                }
            }

            //Close file streams
            reader.close();
            fileReader.close();
            writer.close();
            fileWriter.close();

            System.out.println("OutputFile = " + args[0].replaceAll("\\..*", ".csv"));

        } catch (FileNotFoundException e) {
            System.err.println("[USAGE] tt2gc <file>");
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Cannot read line");
            System.exit(-1);
        }
    }

    private static CalendarEntry baseEntry;
    private static int dataIndex;
    private static void processLine(String line)
    {
        line = line.replaceAll("<.?td>", "");

        if(line.contains("\" title=\""))
        {
            baseEntry = new CalendarEntry(line.split("title=\".*\">")[1].split("</a>")[0]);
            dataIndex = 0;
        }

        switch(dataIndex++)
        {
            case 0:
                break;
            case 1:
                baseEntry.description = line.replaceAll("&amp;", "&");
                break;
            case 2:
                baseEntry.description += " (" + line.replaceAll(" &nbsp;", "") + ")";
                break;
            case 3:
                baseEntry.day = line;
            case 4:
                baseEntry.startTime = line.split("<span")[0];
                break;
            case 5:
                baseEntry.endTime = line.split("<span")[0];
                break;
            case 6:
                baseEntry.weeks = line;
                break;
            case 7:
                baseEntry.location = line;
                writeEntries(baseEntry);
                break;
            default:
                break;
        }
    }

    private static void writeEntries(CalendarEntry baseEntry)
    {
        Calendar date = new GregorianCalendar(YEAR, MONTH, DAY);
        //Ignore Mon because no offset is required
        if(baseEntry.day.equals("Tue"))
            date.add(Calendar.DAY_OF_YEAR, 1);
        else if(baseEntry.day.equals("Wed"))
            date.add(Calendar.DAY_OF_YEAR, 2);
        else if(baseEntry.day.equals("Thu"))
            date.add(Calendar.DAY_OF_YEAR, 3);
        else if(baseEntry.day.equals("Fri"))
            date.add(Calendar.DAY_OF_YEAR, 4);

        boolean[] weeks = getWeeks(baseEntry.weeks);
        for(int i = 0; i < weeks.length; i++)
        {
            if(weeks[i])
            {
                CalendarEntry t = new CalendarEntry(baseEntry);
                t.date = DATE_FORMAT.format(date.getTime());
                writeEntry(t);
            }
            //Increment a week
            date.add(Calendar.DAY_OF_YEAR, 7);
        }
    }

    private static boolean[] getWeeks(String weeks)
    {
        String[] ws = weeks.split(",");
        boolean[] result = new boolean[64];
        for(boolean b : result)
            b = false;

        for(String w : ws)
        {
            w = w.trim();
            if(w.contains("-"))
            {
                String[] t = w.split("-");
                short start = Short.parseShort(t[0]);
                short end = Short.parseShort(t[1]);
                for(int i = start; i <= end; i++)
                {
                    result[i] = true;
                }
            }else{
                result[Short.parseShort(w)] = true;
            }
        }

        return result;
    }

    private static void writeHeader()
    {
        try {
            writer.write("Subject,Start Date,Start Time,End Date,End Time,Description,Location\n");
        } catch (IOException e) {
            System.err.println("Cannot write header");
            System.exit(-1);
        }
    }

    public static void writeEntry(CalendarEntry c)
    {
        try {
            writer.write(c.name +","+ c.date +","+ c.startTime +","+ c.date +","+ c.endTime +","+ c.description +","+ c.location +"\n");
        } catch (IOException e) {
            System.err.println("Cannot write entry");
            System.exit(-1);
        }
    }
}

class CalendarEntry
{
    public String name = "";
    public String date = "";
    public String startTime = "";
    public String endTime = "";
    public String description = "";
    public String location = "";

    public String weeks;
    public String day;

    public CalendarEntry(String name)
    {
        this.name = name;
    }

    public CalendarEntry(CalendarEntry c)
    {
        name = c.name;
        date = c.date;
        startTime = c.startTime;
        endTime = c.endTime;
        description = c.description;
        location = c.location;
    }
}
