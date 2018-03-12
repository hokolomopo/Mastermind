
public enum MPColors {
	RED(0, "red"), BLUE(1, "blue"), YELLOW(2, "yellow"), GREEN(3, "green"), WHITE(4, "white"), BLACK(5, "black");
	
	private int value;
	private String colorName;

	private MPColors(int value, String colorName) {
		this.value = value;
		this.colorName = colorName;
	}

	public int getValue() {
		return this.value;
	}
		
	
	public static int getColorValue(String color) {
		
		for(MPColors c : MPColors.values())
			if(color.equals(c.getColorName()))
				return c.getValue();

		return -1;
		
	}

	public String getColorName() {
		return this.colorName;
	}
	
	public static String getColorName(int value) {
		for(MPColors c : MPColors.values())
			if(value == c.getValue())
				return c.getColorName();
		return "";
	}
}
