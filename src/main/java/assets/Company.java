package assets;

import java.util.HashMap;
import java.util.Random;

public class Company {

    private final String name; // company's name
    private final int[] values; // every value this share's companies can assume
    private final int[] diceValues; // possible flutuation values that can be applied between rounds
    private int currIndex; // current values' index
    
    public Company(String name, int[] values, int[] diceValues){
		this.name = name;
		this.values = values;
		this.diceValues = diceValues;
		currIndex = 3;
	}

    public int getCompanyValue() {
        return values[currIndex];
    }

    public int rollDice() {
        // roll the dice
        Random r = new Random();
        int diceIndex = r.nextInt(diceValues.length);
        int diceRollValue = diceValues[diceIndex];

        // apply dice offset to determine company's new value
        currIndex += diceRollValue;
        if(currIndex<0)
            currIndex = 0;
        else if(currIndex >= values.length)
            currIndex = values.length-1;

        return values[currIndex];
    }

    public String getName() {
        return name;
    }
}
