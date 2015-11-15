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
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class CalenTools {
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
		System.out.println(
				"Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
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
     * @input  a 2d string array of possible contained names
     * @return treemap, a mapping of category title to possible contained names
     */
    public HashMap<String, HashSet<String>> addCategories(String[] categories, String[][] contains) {
        if (categories.length != contains.length) {
            System.out.println("Please check your two inputs! Lengths are not equal.");
            return null;
        } else {
            HashMap<String, HashSet<String>> cat = new HashMap<>();
            for (int i = 0; i < categories.length; i++) {
                HashSet<String> set = new HashSet<String>(Arrays.asList(contains[i]));
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

    // Instantiates a new class, CalenTools
    public CalenTools() { }

	public static void main(String[] args) throws IOException {
		// Build a new authorized API client service.
		// Note: Do not confuse this class with the
		//   com.google.api.services.calendar.model.Calendar class.
		com.google.api.services.calendar.Calendar service =
			getCalendarService();

        // sets up the application
        CalenTools calentools = new CalenTools();
        TreeMap<String, Double> calendarToHour = new TreeMap<>(); // a mapping of calendar name to time spent
        TreeMap<String, Double> categoryToHour = new TreeMap<>(); // a mapping of category name to time spent
        HashMap<String, HashSet<String>> miscCategories; // a mapping from category name to what it contains
		
        DateTime now = new DateTime(System.currentTimeMillis());
        long startTime = now.getValue();
        // Date current = new Date(now.getValue());
        int eventCount = 0;

        // adds various categories to miscCategories
        String[] cat1 = new String[]{"CS 160", "EE 375", "IEOR 186", "UGBA 103", "UGBA 107", "UGBA 167", "CSM", "CS 61A TA", "Projects"};
        String[][] cat2 = new String[][]{{"160"}, {"375"}, {"IEOR", "ieor", "186"}, {"103", "finance"}, {"107", "ethics"}, {"167"}, {"csm", "CSM"}, {"61a", "staff", "TA"}, {"calendar", "hack", "project"}};
        // String[][] cat1 = new String[][]{{"Homework"}, {"Homework & Studying"}, { {"CS 160", "160"}, {"EE 375", "375"}, {"IEOR 186", "ieor, IEOR, 186"}, {"UGBA 103", "103, finance"}, {"UGBA 107", "107, ethics"}, {"UGBA 167", "167, branding"}};
        // String[][] cat2 = new String[][]{{"Projects"}, {"Jobs, Internships & Activities"}, { {"CSM", "csm, CSM, exec"}, {"CS 61A TA", "61a, staff"}, {"Projects", "calendar, hack, project"}}};
        miscCategories = calentools.addCategories(cat1, cat2);
		
        String pageToken = null;
		do {
			CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
			List<CalendarListEntry> items = calendarList.getItems();
			try {
                // creates the date at the beginning of the week
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyy");
				Date dBegin = sdf.parse("11/8/2015"); // be able to set a time here
				DateTime dateBegWeek = new DateTime(dBegin);

                Date dEnd = sdf.parse("11/15/2015");
                DateTime dateEndWeek = new DateTime(dEnd);

                // adds miscellaneous categories to the calendar map
                for (String cat : miscCategories.keySet()) {
                    categoryToHour.put(cat, 0.0);
                }
                
                // iterates through all the calendars
                for (CalendarListEntry calendarListEntry : items) {
                    if (!calendarListEntry.isPrimary()) {
                        
                        // sets a list of events from a particular calendar
                        Events events = service.events().list(calendarListEntry.getId())
                            .setTimeMin(dateBegWeek)
                            .setTimeMax(dateEndWeek)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                        List<Event> calendarItems = events.getItems();
                        
                        double calendarTime = 0; // total amount of time in the calendar
                        double categoryTime = 0; // total amount of tine in a category
                        for (Event e : calendarItems) {
                            // adds up all the durations from events
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
                            }
                            eventCount += 1;
    					}
    					calendarToHour.put(calendarListEntry.getSummary(), calendarTime);
    				}
    			}

                // Summarize the hours within each calendar
                System.out.println("---- Here is Your Calendar Summary of the Past Week! ----");
                for (String calendar : calendarToHour.keySet()) {
                    System.out.println(calendar + ": " + calendarToHour.get(calendar));
                }
                System.out.println("");

                // Summarize the hours within new categories given by the user
                System.out.println("---- Here is Your Category Summary of the Past Week! ----");
                for (String cat : categoryToHour.keySet()) {
                    System.out.println(cat + ": " + categoryToHour.get(cat));
                }
                System.out.println("");

                // // Summarize the hours within a particular calendar
                // System.out.println("---- Here is the Summary for Sleep from the Past Week! ----");
                // for (String calendar : calendarToHour.keySet()) {
                //     // later implement calendar checking
                //     if (calendar.equals("Sleep")) {
                //         System.out.println(calendarToHour.get(calendar) + " hours spent last week starting " + calentools.dateToMonthDay(d) + " to " + calentools.dateToMonthDay(current) + " on " + calendar);
                //     }
                // }
                // System.out.println("");

                // Displays how long it took for the program to run
                System.out.println("---- Here is Some Statistics about the Program! ----");
                long endTime = System.currentTimeMillis();
                double totalTime = (double) (endTime - startTime) / 1000.0; // add decimals afterwards
                int rate = (int) (eventCount / totalTime);
                System.out.println("Was " + totalTime + " seconds.");
                System.out.println("Counted " + eventCount + " events.");
                System.out.println("That's ~" + rate + " events per second.");

            } catch(ParseException pe) {
                System.out.println("Cannot parse date");
            }
            
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);


    }

}
