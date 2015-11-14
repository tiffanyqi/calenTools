public class Category {

    private String categoryName; // name of the category
    private String calendarFrom; // which calendar the event came from
    private double hours; // hours the category accumulates
    private boolean isCalendar; // is this a calendar, or a user inputed category?
    private String[] contained; // the different names from the event. could be null.

	public Category(String categoryName, String calendarFrom, double hours, boolean isCalendar, String[] contained) {
        this.categoryName = categoryName;
        this.calendarFrom = calendarFrom;
        this.hours = hours;
        this.isCalendar = isCalendar;
        this.contained = contained;
	}

    public String getCategoryName() {
        return categoryName;
    }

    public String calendarFrom() {
        return calendarFrom;
    }

    public double getHours() {
        return hours;
    }

    public boolean getIsCalendar() {
        return isCalendar;
    }

    public String[] getContained() {
        return contained;
    }

}
