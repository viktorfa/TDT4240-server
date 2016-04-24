package softarkserver;

import com.esotericsoftware.kryonet.Connection;

public class Player {
	private Connection connection;
	
	private String playerName;
	
	private int gameromID;
	private int score;	
	
	private boolean rdyToStart;
	private boolean leftGame;
	
	public Player(Connection con){
		this.connection = con;
		score = 0;
		playerName = "";
		rdyToStart = false;
		leftGame = false;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getGameromID() {
		return gameromID;
	}

	public void setGameromID(int gameromID) {
		this.gameromID = gameromID;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public boolean isRdyToStart() {
		return rdyToStart;
	}

	public void setRdyToStart(boolean rdyToStart) {
		this.rdyToStart = rdyToStart;
	}
}
