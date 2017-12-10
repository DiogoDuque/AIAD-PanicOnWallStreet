package assets;

import com.google.gson.Gson;

import java.util.Random;

public class Company {

    /**
     * Company's name.
     */
    private final String name;

    /**
     * All possible values this share's companies can assume.
     */
    private final int[] values;

    /**
     * Possible flutuation values that can be applied between rounds.
     */
    private final int[] diceValues;

    /**
     * Current company's value index.
     */
    private int currIndex;

    public Company(String name, int[] values, int[] diceValues){
		this.name = name;
		this.values = values;
		this.diceValues = diceValues;
		currIndex = 3;
	}

    int getCurrentValue() {
        return values[currIndex];
    }

    int maxValue(){
        int maxIndexValue;
        int maxDiceValue = this.diceValues[diceValues.length - 1];

        if(this.currIndex + maxDiceValue > this.values.length){
            maxIndexValue = this.values.length - 1;
        } else if (this.currIndex + maxDiceValue < 0){
            maxIndexValue = 0;
        }
        else{
            maxIndexValue = this.currIndex + maxDiceValue;
        }

        return  this.values[maxIndexValue];
    }

    int minValue(){
        int minIndexValue;
        int minDiceValue = this.diceValues[0];

        if(this.currIndex + minDiceValue > this.values.length){
            minIndexValue = this.values.length - 1;
        } else if (this.currIndex + minDiceValue < 0){
            minIndexValue = 0;
        }
        else{
            minIndexValue = this.currIndex + minDiceValue;
        }

        return  this.values[minIndexValue];
    }

    /**
     * Calculates the average value that will come out when the dices are rolled.
     * @return average of the next value after dice roll.
     */
        float getAverageNextValue() {
        int averageValue=0;

        for(int diceVal: diceValues){
            int index = currIndex+diceVal;
            if(index<0)
                index = 0;
            else if(index >= values.length)
                index = values.length-1;

            averageValue += values[index];
        }

        return (float)(averageValue/6);
    }

    /**
     * Rolls the dice for this company, making its value change.
     * @return new value for this company's shares.
     */
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

    public int risk(){
        int riskLevel;
        if(this.diceValues[0] == -1 ){
            riskLevel = 0;
        }
        else if (this.diceValues[0] == -2 ){
            riskLevel = 1;
        }
        else if (this.diceValues[0] == -3 ){
            riskLevel = 2;
        }
        else riskLevel = 3;
        return riskLevel;
    };

    @Override
    public String toString() {
        return "{"+name+"("+values[currIndex]+")}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Company company = (Company) o;

        return name.equals(company.name);
    }

    public String toJsonStr(){
        return new Gson().toJson(this);
    }
}
