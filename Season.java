import java.util.*;

public class Season {
	public static final int NUM_DIVISIONS = 2;

	public static void main(String[] args) {
		String[] names = {"Allan", "Becky", "Debbie", "Gretta", "Mark", "Kate"};
		LeagueRanking testRanking = new LeagueRanking(names);
		testRanking.addPlayer("Carl");
		Division testDivision = new Division(testRanking.retrieveRanking());
		testDivision.newWeek();
		testDivision.newWeek();
		testDivision.newWeek();
		testDivision.newWeek();
		System.out.println(testDivision);
//		Division[] testDivisions = createDivisions(testRanking.retrieveRanking());
//		System.out.println(testDivisions[0]);
		
	}
	
	public static Division[] createDivisions(String[] names){
		Division[] newDivisions = new Division[NUM_DIVISIONS];
		for(int i = 0; i < NUM_DIVISIONS; i++){
			String[] nameList = new String[(i+1)*names.length/NUM_DIVISIONS-i*names.length/NUM_DIVISIONS];
			for(int j = 0; j < nameList.length; j++){
				nameList[j] = names[i*names.length/NUM_DIVISIONS+j];
			}
			newDivisions[i] = new Division(nameList);
		}
		return newDivisions;
	}
}

class Division{
	ArrayList<ArrayList<Game>> weeks;
	ArrayList<Player> seeding;
	
	public Division(String[] names){
		weeks = new ArrayList<ArrayList<Game>>();
		seeding = new ArrayList<Player>();
		for (String s: names){
			seeding.add(new Player(s));
		}
	}
	
	public void newWeek(){
		weeks.add(createWeek(seeding));
	}
	
	@Override
	public String toString(){
		StringBuilder text = new StringBuilder("");
		for(Player p: seeding){
			text.append(p + ", ");
		}
		text.append("\r\n");
		for(int i = 0; i < weeks.size(); i++){
			text.append("\r\nWeek " + i);
			for(int j = 0; j < weeks.get(i).size(); j++){
				text.append("\r\n" + weeks.get(i).get(j));
			}
		}
		return text.toString();
	}
	
	private ArrayList<Game> createWeek(ArrayList<Player> seeding){
		ArrayList<Player> playerPool = new ArrayList<Player>();
		ArrayList<Game> week = new ArrayList<Game>();
		for(Player i: seeding){
			if (i.active)
				playerPool.add(i);
		}
		while(playerPool.size()>1){
			week.add(createGame(playerPool, weeks.size()));
		}
		if(playerPool.size() == 1){
			Player unpaired = playerPool.get(0);
			seeding.remove(unpaired);
			seeding.add(0, unpaired);
		}
		return week;
	}
	
	private Game createGame(ArrayList<Player> playerPool, int week){
		Player first = playerPool.get(0);
		Player second = playerPool.get(1);
		int fewestMatches = countMatches(first, second);
		for(int i = 2; i < playerPool.size(); i++){
			if (countMatches(first, playerPool.get(i)) < fewestMatches){
				second = playerPool.get(i);
				fewestMatches = countMatches(first, second);
			}
		}
		playerPool.remove(first);
		playerPool.remove(second);
		return new Game(first, second);
	}
	
	private int countMatches(Player first, Player second){
		int matches = 0;
		for(int i = 0; i < weeks.size(); i++){
			for(Game g: weeks.get(i)){
				if((g.first == first && g.second == second ) || (g.first == second && g.second == first))
					matches++;
			}
		}
		return matches;
	}
		
	class Player{
		String name;
		boolean active;
		
		public Player(String name){
			this.name = name;
			active = true;
		}
		@Override
		public String toString(){
			return name;
		}
	}
		
	class Game{
		Player first;
		Player second;
		int result;
		
		public int addResult(int result){
			return this.result = result;
		}
		
		public Game(Player first, Player second){
			this.first = first;
			this.second = second;
			this.result = 0;
		}
	
		@Override
		public String toString(){
			return first + " vs " + second + " : " + result;
		}	
	}
}

class LeagueRanking{
	private static final double NEW_PLAYER_GAMES_MULTIPLIER = 0.65;
	PriorityQueue<Player> ranking;
	
	public String[] retrieveRanking(){
		String[] returnArray = new String[ranking.size()];
		for(int i = 0; i < returnArray.length; i++){
			returnArray[i] = ranking.poll().name;
		}
		return returnArray;
	}
	
	public LeagueRanking(String names[]){
		ranking = new PriorityQueue<Player>();
		for (int i = 0; i < names.length; i++){
			ranking.add(new Player(names[i], i + 1));
		}
	}
	
	public void addPlayer(String name){
		ranking.add(new Player(name));
	}
	
	public void printRanking(){
		while(!ranking.isEmpty()){
			System.out.println(ranking.poll().name);
		}
	}
	
	class Player implements Comparable<Player>{
		String name;
		boolean newlyAdded;
		int rank;
		int gamesPlayed;
		
		public Player(String name, int rank){
			this.name = name;
			newlyAdded = false;
			this.rank = rank;
			this.gamesPlayed = PlayerHistory.SPY_PARTY.getGames(name);
		}
		
		public Player(String name){
			this.name = name;
			newlyAdded = true;
			rank = 0;
			gamesPlayed = PlayerHistory.SPY_PARTY.getGames(name);
			for (Player i: ranking){
				if((!i.newlyAdded) && i.gamesPlayed > this.gamesPlayed * NEW_PLAYER_GAMES_MULTIPLIER){
					rank++;
				}
			}
		}
		
		@Override
		public int compareTo(Player other){
			if (this == other)
				return 0;
			if (other.rank != rank)
				return rank - other.rank;
			if (other.newlyAdded != newlyAdded)
				return (newlyAdded ? 1 : -1);
			if (other.gamesPlayed != gamesPlayed)
				return gamesPlayed - other.gamesPlayed;
			if (other.name.length() != name.length())
				return name.length() - other.name.length();
			return name.compareTo(other.name);
		}
		
		@Override
		public String toString(){
			return name;
		}
	}
}

class PlayerHistory {
	public static PlayerHistory SPY_PARTY = new PlayerHistory("SpyParty");
	private final Map<String,Integer> records;
	
	public PlayerHistory(String source){
		records = new HashMap<String,Integer>();
		if (source == "SpyParty") {
			String[] names = {"Allan", "Becky", "Carl", "Debbie"};
			int[] games = {5,777,1000,333};
			for (int i = 0; i < names.length; i++){
				records.put(names[i], games[i]);
			}
		}
	}
	
	public int getGames(String name){
		if(!records.containsKey(name))
			return 0;
		return records.get(name);
	}
}
