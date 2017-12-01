package objects;

java.util.list;

public class Companies {


    private String name;

    private int numShares;
    private double compValue;

    public boolean status;

    private List<Shares> shares;
    
    	public Company (String name , int numShares){
		this.name = name;
		this.numShares = numShares;
		shares = new List<Share>(numShares);
		
	}
    
    
}
