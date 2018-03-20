
public enum MPColors {
	RED(0, "red"), BLUE(1, "blue"), YELLOW(2, "yellow"), GREEN(3, "green"), WHITE(4, "white"), BLACK(5, "black");
	
	private int value;
	private String colorName;

	private MPColors(int value, String colorName) {
		this.value = value;
		this.colorName = colorName;
	}

	public byte getValue() {
		return (byte)this.value;
	}
		
	//Get the value of a color from a string
	public static byte getColorValue(String color) {
		
		for(MPColors c : MPColors.values())
			if(color.equals(c.getColorName()))
				return c.getValue();

		return -1;
		
	}

	public String getColorName() {
		return this.colorName;
	}
	
	//Get color name from it's value
	public static String getColorName(int value) {
		for(MPColors c : MPColors.values())
			if(value == c.getValue())
				return c.getColorName();
		return "";
	}
	
	//Get a string containing all the colors
	public static String getColorList() {
		String s = "";
		for(MPColors c : MPColors.values())
			s += c.getColorName()+" ";
		return s;
	}
}
