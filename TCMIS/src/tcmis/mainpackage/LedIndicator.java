package tcmis.mainpackage;

public class LedIndicator {
	boolean isInUse = false;
	int percentFull = 0;
	
	LedIndicator(){
		
	}

	/**
	 * Get the value of the indicator
	 * @return
	 */
	public int getIndicator() {
		return percentFull;
	}

	/**
	 * Set the indicator
	 * @param percent
	 */
	public void setPercentFull(int percent) {
		if (percent <= 100 || percent >= 0)
			percentFull = percent;
	}

	/**
	 * If the led indicator isn't used, set used to false, otherwise set used to
	 * true
	 * 
	 * @param used change the state of the led
	 */
	public void setLedUsage(boolean used) {
		if (!used)
			setPercentFull(100);
		else if (getIndicator() == 100)
			setPercentFull(0);

		isInUse = used;
	}
	
	public boolean getLedUsage(){
		return isInUse;
	}

}
