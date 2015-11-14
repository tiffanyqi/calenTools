# calenTools

Purpose: run many tools on your list of calendars on Google Calendar from the command line.

Written in Java, uses Gradle utilizes Google API

## Different methods:
- summary
	- inputs: date start, date end
	- outputs: category name - hours spent (for all calendars)
- hoursSpent
	- inputs: date start, date end, category name
	- output: hours

## How to use:
- Clone this repo
- Create a new Google project, enable Google Calendar API
- Make sure you have gradle installed (brew install gradle)
- Make sure you have Java 1.7 (or higher) installed
- Cd into the repo
- $ gradle -q run
	- The first time you run the program, it will open up a new window (or copy and paste the link given) so that you can either enter in your information

## Web application interface:
- About the project
- Sync your calendars!
- Select which calendars to include / categories to add
- View graphs
- Start again (goes back to step 3)

## Future additions:
- Modifying and adding events to the calendar on the command line
- Calculate overall productivity with the statistics generated
- Predicting future workload
- Predicting future productivity
- Save information into a CSV
- Transfer as a website
- Create a RescueTime like interface that prompts users about what they've doing, and adds it to their Google Calendar

## Notes:
- This tool ignores all primary calendar events.
- Any all-day event can cause the script to error. You can change it so that the event shows up on the primary calendar instead of that individual calendar.