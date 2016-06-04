import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class CalenTools {

    // change these variables to modify program
    private String dateStart = "5/29/2016";
    private String dateEnd = "6/5/2016";
    private String productives[] = 
        new String[]{"Studying & Learning", "Job Searching", "Work", "Planning"};
    private String[] categoryTitle = new String[]{"Projects", "Language"};
    private String[][] categoryFilters = 
        new String[][]{{"productivity", "calentools", "job manager"}, 
        {"language", "korean", "japanese"}};
    
    // Instantiates a new class, CalenTools
    public CalenTools() { }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service. Not calendar.model.Calendar class.
        com.google.api.services.calendar.Calendar service =
            getCalendarService();

        // sets up the application
        CalenTools c = new CalenTools();
        HashMap<String, Double> calToHour = new HashMap<>();
        HashMap<String, Double> categoryToHour = new HashMap<>();
        HashMap<String, HashSet<String>> miscCategories; // a mapping from category name to what it contains
        
        DateTime now = new DateTime(System.currentTimeMillis());
        long startTime = now.getValue();
        int eventCount = 0;
        miscCategories = c.addCategories(c.categoryTitle, c.categoryFilters);

        String pageToken = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            // adds miscellaneous categories to the category map
            for (String cat : miscCategories.keySet()) {
                categoryToHour.put(cat, 0.0);
            }
            
            // iterates through all the calendars
            for (CalendarListEntry calendarListEntry : items) {
                    
                // sets a list of events from a particular calendar
                Events events = service.events().list(calendarListEntry.getId())
                    .setTimeMin(c.setDate(c.dateStart))
                    .setTimeMax(c.setDate(c.dateEnd))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
                List<Event> calendarItems = events.getItems();
                
                double calendarTime = 0; // total amount of time in the calendar
                double categoryTime = 0; // total amount of tine in a category
                for (Event e : calendarItems) {
                    // adds up all the durations from events
                    if (e.getStart().getDateTime() != null) {
                        long start = e.getStart().getDateTime().getValue();
                        long end = e.getEnd().getDateTime().getValue();
                        double eventDuration = ((double) end - start) / 1000 / 60 / 60;
                        calendarTime += eventDuration;

                        // if the event title is in one of the set categories, add it as well
                        if (e.getSummary() != null) {
                            for (String cat : categoryToHour.keySet()) {
                                for (String contained : miscCategories.get(cat)) {
                                    if (e.getSummary().contains( (CharSequence) contained)) {
                                        categoryToHour.put(cat, categoryToHour.get(cat) + eventDuration);
                                    }
                                }
                            }
                        } eventCount += 1;
                    }

                } calToHour.put(calendarListEntry.getSummary(), calendarTime);
            }

            // Summarize the hours within each calendar
            System.out.print("---- Here is Your Summary of Week ");
            System.out.println(c.dateStart + "-" + c.dateEnd + "! ----");
            for (String calendar : calToHour.keySet()) {
                double amount = calToHour.get(calendar);
                String productivity = new DecimalFormat("##.##").format(amount / 168 * 100);
                System.out.println(calendar + c.spaces(40, calendar) + amount + c.spaces(7, String.valueOf(amount)) + productivity + "%");
            };
            // categories, or things that are also not calendars themselves
            for (String cat : categoryToHour.keySet()) {
                double amount = categoryToHour.get(cat);
                String productivity = new DecimalFormat("##.##").format(amount / 168 * 100);
                System.out.println(cat + c.spaces(40, cat) + amount + c.spaces(7, String.valueOf(amount)) + productivity + "%");
            };

            // Summarizes productivity
            System.out.print("You were productive ");
            System.out.println(c.sumCals(c.productives, calToHour) + "% of the time.");

            // Displays how long it took for the program to run
            long endTime = System.currentTimeMillis();
            double totalTime = (double) (endTime - startTime) / 1000.0;
            int rate = (int) (eventCount / totalTime);
            System.out.print("This took " + totalTime + " seconds for ");
            System.out.println(eventCount + " events. That's ~" + rate + " events per second.");
            
            pageToken = calendarList.getNextPageToken();

        } while (pageToken != null);
    }

    /** Application name. */
    private static final String APPLICATION_NAME =
        "Summary of Hours Spent From Your Calendar";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/calendar-java-statistics");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart. */
    private static final List<String> SCOPES =
        Arrays.asList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
            CalenTools.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        // System.out.println(
        //      "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar
        getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Adds categories for further segmentation.
     * @input  a string array of category names 
     * @input  a 2d string array of possible key words
     * @return HashMap, a mapping of category title to possible key words
     */
    public HashMap<String, HashSet<String>> addCategories(String[] categories, String[][] words) {
        if (categories.length != words.length) {
            System.out.println("Please check your two inputs! Lengths are not equal.");
            return null;
        } else {
            HashMap<String, HashSet<String>> cat = new HashMap<>();
            for (int i = 0; i < categories.length; i++) {
                HashSet<String> set = new HashSet<String>(Arrays.asList(words[i]));
                cat.put(categories[i], set);
            }
            return cat;
        }
    }

    /**
     * Converts a Date object into MM DD.
     * @input  Date object
     * @return String that looks like MM DD
     */
    public String dateToMonthDay(Date d) {
        String date = d.toString();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
        String monthDay = sdf.format(d);
        return monthDay;
    }

    /** 
     * Sets the date
     * @input takes in a string parsed from "MM/DD/YYY"
     * @return DateTime object from string
     */
    public DateTime setDate(String date) {
        DateTime datetime = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy");
            Date d = sdf.parse(date);
            datetime = new DateTime(d);
        } catch (ParseException pe) {
            System.out.println("Cannot parse date");
        }
        return datetime;
    }

    /**
     * Generate the write number of spaces based on the length of a name
     * @input int ideal number of spaces, string length of the name
     * @return a new string with the ideal number of spaces
     * source: http://stackoverflow.com/questions/1235179/simple-way-to-repeat-a-string-in-java
     */
    public String spaces(int numCharacters, String name) {
        return new String(new char[numCharacters-name.length()]).replace("\0", " ");
    }

    public String sumCals(String[] calendars, HashMap<String, Double> map) {
        double sum = 0;
        for (String calendar : calendars) {
            if (map.containsKey(calendar)) {
                sum += map.get(calendar);
            }
        }
        String productivity = new DecimalFormat("##.##").format(sum / 168 * 100);
        return productivity;
    }


}
