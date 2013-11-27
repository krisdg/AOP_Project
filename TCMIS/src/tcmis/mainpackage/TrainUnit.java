package tcmis.mainpackage;

public class TrainUnit {
	int amountOfPeople = 0;
	int unitCapacity = 80;
	public TrainUnit(){
		
	}
	
	/**
	 * The Amount of people who leave this train unit
	 * @param People the amount of people
	 */
	public void leaveUnit(int People){
		amountOfPeople -= People;
	}
	
	/**
	 * The amount of people who enter the train unit
	 * @param People the amount of people
	 */
	public void enterUnit(int People){
		amountOfPeople += People;
	}
	
	public int getPercentOfUse(){
		return (amountOfPeople/unitCapacity * 100); //TODO test this, zal wel niet werken met int, heb een double nodig
	}

}
