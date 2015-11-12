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
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

public class CalendarTools {
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
			CalendarTools.class.getResourceAsStream("/client_secret.json");
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
	 * Converts milliseconds to hours.
	 * @input long (milliseconds)
	 * @return double (hours)
	 */
	public double milliToHours(long milliseconds) {
		double hours = (milliseconds / 1000) / 60 / 60;
		return hours;
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

	public static void main(String[] args) throws IOException {
		// Build a new authorized API client service.
		// Note: Do not confuse this class with the
		//   com.google.api.services.calendar.model.Calendar class.
		com.google.api.services.calendar.Calendar service =
			getCalendarService();
		HashMap<String, Double> calendarToHour = new HashMap<>();
		DateTime now = new DateTime(System.currentTimeMillis());

		// Summarize the hours within each calendar
		System.out.println("---- Here is Your Summary of the Past Week! -----");

		String pageToken = null;
		do {
			CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
			List<CalendarListEntry> items = calendarList.getItems();
			try {
                // creates the date at the beginning of the week
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				Date d = sdf.parse("08/11/2015");
				DateTime dateBegWeek = new DateTime(d);
                
                // iterates through all the calendars
                for (CalendarListEntry calendarListEntry : items) {
                    if (!calendarListEntry.isPrimary()) {
                        
                        // sets a list of events from a particular calendar
                        Events events = service.events().list(calendarListEntry.getId())
                            .setTimeMin(dateBegWeek)
                            .setTimeMax(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                        List<Event> calendarItems = events.getItems();
                    
                        // adds up all the durations from a particular calendar

                        // resolve double bookings here
                        double count = 0;
                        for (Event e : calendarItems) {
                            long start = e.getStart().getDateTime().getValue();
    						long end = e.getEnd().getDateTime().getValue();
    						double duration = ((double) end - start) / 1000 / 60 / 60;
    						count += duration;
    					}
    					calendarToHour.put(calendarListEntry.getSummary(), count);
    				}
    			}

            } catch(ParseException pe) {
                System.out.println("Cannot parse date");
            }
            
			pageToken = calendarList.getNextPageToken();
		} while (pageToken != null);

		// prints out the summary
		for (String calendar : calendarToHour.keySet()) {
			System.out.println(calendar + ": " + calendarToHour.get(calendar));
		}
	}

}