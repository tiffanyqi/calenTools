public class Category {

    private String categoryName; // name of the category
    private String typeName; // the type of category
    private String calendarFrom; // which calendar the event came from
    private double hours; // hours the category accumulates
    private boolean isCalendar; // is this a calendar, or a user inputed category?
    private String[] contained; // the different names from the event. could be null.

    // instatiates a category class
	public Category(String categoryName, String typeName, String calendarFrom, double hours, boolean isCalendar, String[] contained) {
        this.categoryName = categoryName;
        this.typeName = typeName;
        this.calendarFrom = calendarFrom;
        this.hours = hours;
        this.isCalendar = isCalendar;
        this.contained = contained;
	}

    /**
     * Gets the category name from the category.
     * @return String
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Gets the type name from the category.
     * @return String
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Gets the calendar name in which the events came from.
     * @return String
     */
    public String getCalendarFrom() {
        return calendarFrom;
    }

    /**
     * Gets the accumulated hours spent in that category.
     * @return double
     */
    public double getHours() {
        return hours;
    }

    /**
     * Gets whether the category was itself a calendar.
     * @return boolean
     */
    public boolean getIsCalendar() {
        return isCalendar;
    }

    /**
     * Gets which queries also count towards that category name.
     * @return String[], can also be null
     */
    public String[] getContained() {
        return contained;
    }

}
