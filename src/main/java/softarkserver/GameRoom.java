package softarkserver;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.kryonet.Connection;


public class GameRoom {
	
	private Player roomowner;
	
	private ArrayList<Player> players;
	private WordList wordlist;
	
	private int currentWord;
	private int playerIdToDraw;
	private int timeInSeconds;
	private int score;
	
	private boolean gameStarted;
	private boolean isTimerStarted;
	private boolean gameFinished;
	
	private ArrayList<JSONArray> drawpaths;
	private ScheduledExecutorService ses;
	
	public GameRoom(WordList words) {
		players = new ArrayList<Player>();
		wordlist = words;
		currentWord = 0;
		playerIdToDraw = 0;
		drawpaths = new ArrayList<JSONArray>();
		gameStarted = false;
		ses = Executors.newSingleThreadScheduledExecutor();
		timeInSeconds = 0;
		isTimerStarted = false;
		gameFinished = false;
		score = 200;
	}
	
	public boolean checkNameAvailability(String name){
		for(int i = 0; i<players.size(); i++){
			if(players.get(i).getPlayerName().equals(name)){
				return false;
			}
		}
		return true;
	}
	
	
	public void incrementCurrentWord(){
		currentWord++;
	}

	public void notifyNewPlayerJoined(Player player){
		try {
			JSONObject playerJoinedJsn = new JSONObject();
			JSONArray playersJsn = new JSONArray();
			playerJoinedJsn.put("type", "playerJoinedNotification");
			
			for(int i = 0; i<players.size(); i++){
				if(!players.get(i).getPlayerName().equals("")){
					playersJsn.put(players.get(i).getPlayerName());
				}
			}
			
			playerJoinedJsn.put("players", playersJsn);
			bcast(playerJoinedJsn.toString());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void rdyToStartGameBCAst(){
		try {
			JSONObject jsn = new JSONObject();
			jsn.put("type", "startGameFromLobbyResponse");
			for(int i = 0; i<players.size(); i++){
				if(players.get(i).getPlayerName().equals("")){
					players.remove(i);
				}
			}
			bcast(jsn.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean checkIfPlayerHasJoined(Connection con){
		for(int i = 0; i<players.size(); i++){
			if(players.get(i).getConnection().getID() == con.getID()){
				return true;
			}
		}
		return false;
	}
	
	public void attemptStartGame() {
		for(int i = 0; i<players.size(); i++){
			if(!players.get(i).isRdyToStart() && !players.get(i).getPlayerName().equals(roomowner.getPlayerName())){
				return;
			}
		}
		onGameStarted();
	}
	
	public void onGameStarted(){
		gameStarted = true;
		bcastNewKeyboardWord();
		
		if(!isTimerStarted){
			ses.scheduleAtFixedRate(new Runnable() {
				
				@Override
				public void run() {
					try {
						if(gameFinished){
							ses.shutdown();
						} else {
							JSONObject timejsn = new JSONObject();
							timejsn.put("type", "timeUpdate");
							timejsn.put("time", timeInSeconds);
							bcast(timejsn.toString());
							timeInSeconds++;
							if(score > 0){
								score--;
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}, 0, 1, TimeUnit.SECONDS);
			isTimerStarted = true;
		}
	}
	
	public void bcastNewKeyboardWord(){
		try {
			if(currentWord < wordlist.getSize()){
				if(playerIdToDraw == players.size()){
					playerIdToDraw = 0;
				}
				
				JSONObject newKeyboardResponseJsn = new JSONObject();
				newKeyboardResponseJsn.put("type", "newKeyboard");
				newKeyboardResponseJsn.put("keyboard", getCurrentKeyboardWord());
				newKeyboardResponseJsn.put("drawer", players.get(playerIdToDraw).getPlayerName());
	
				for(int i = 0; i<players.size(); i++){
					if(i == playerIdToDraw){
						JSONObject newWord = new JSONObject();
						newWord.put("type","newWord");
						newWord.put("word", wordlist.getAnswerWord(currentWord));
						players.get(i).getConnection().sendTCP(newWord.toString());
					
					} else {
						players.get(i).getConnection().sendTCP(newKeyboardResponseJsn.toString());
					}
				}
				score = 200;
			} else {
				int winnerid = 0;
				for(int i = 0; i<players.size(); i++){
					if(players.get(winnerid).getScore() < players.get(i).getScore()){
						winnerid = i;
					}
				}
				gameFinished = true;
				bcastGameOver(players.get(winnerid).getPlayerName());
				return;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void bcastGameOver(String winner){
		try {
	 		gameStarted = false;
			JSONObject jsn = new JSONObject();
			jsn.put("type", "gameOverNotification");
			JSONArray words = new JSONArray();
			for(int i = 0; i<wordlist.getSize(); i++){
				words.put(wordlist.getAnswerWord(i));
			}
			jsn.put("wordlist", words);
			jsn.put("winner", winner);
			bcast(jsn.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void bcast(String msg){
		for(int i = 0; i<players.size(); i++){
			players.get(i).getConnection().sendTCP(msg);
		}
	}
	
	public void checkAnswer(Player player, JSONObject jsn){
		try {
			if(currentWord< wordlist.getSize()){
				String answer = jsn.getString("word");
				if(answer.equals(wordlist.getAnswerWord(currentWord ))){
					player.setScore(player.getScore() + score);
					players.get(playerIdToDraw).setScore((int)(players.get(playerIdToDraw).getScore() + score*0.5));
					
					JSONObject jsnResponse = new JSONObject();
					jsnResponse.put("type", "checkAnswerResponse");
					jsnResponse.put("result", "right");
					jsnResponse.put("correctWord", wordlist.getAnswerWord(currentWord));
					jsnResponse.put("guesser", players.get(playerIdToDraw).getPlayerName());
					
					JSONArray playerScores = new JSONArray();
					for(int i = 0; i<players.size(); i++){
						JSONObject jsnplayer = new JSONObject();
						jsnplayer.put("name", players.get(i).getPlayerName());
						jsnplayer.put("score", players.get(i).getScore());
						playerScores.put(jsnplayer);
					}
					jsnResponse.put("newPlayerScores", playerScores);
					drawpaths.clear();
					bcast(jsnResponse.toString());
					currentWord++;
					playerIdToDraw++;
					if(playerIdToDraw == players.size()){
						playerIdToDraw = 0;
					}
					bcastNewKeyboardWord();
					
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean checkIfNameIsTaken(String playername){
		for(int i = 0; i<players.size(); i++){
			if(players.get(i).getPlayerName().equals(playername)){
				return false;
			}
		}
		return true;
	}
	
	public void addDrawPath(JSONObject jsn){
		for(int i = 0; i<players.size(); i++){
			if(i != playerIdToDraw){
				players.get(i).getConnection().sendTCP(jsn.toString());
			}
		}
	}
	
	public void removePlayer(Player player){
		if(gameStarted){
			if(players.size() <= 2){
				gameFinished = true;
//				gameStarted = false;
				bcastGameOver(players.get(0).getPlayerName());
				return;
			}
			
			players.remove(player);
			if(playerIdToDraw == players.size()){
				playerIdToDraw = 0;
			}
			
			
			try {
				JSONObject playerLeaveGameRoomJsn = new JSONObject();
				playerLeaveGameRoomJsn.put("type", "playerLeftGameRoom");
				playerLeaveGameRoomJsn.put("name", player.getPlayerName());
				bcast(playerLeaveGameRoomJsn.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(players.get(playerIdToDraw) == player){
				playerIdToDraw++;
				if(playerIdToDraw == players.size()){
					playerIdToDraw = 0;
				}
				bcastNewKeyboardWord();
			}
		} else {
			try {
				players.remove(player);
				if(players.size() <= 0){
					gameFinished = true;
				}
				JSONObject playerLeaveGameRoomJsn = new JSONObject();
				playerLeaveGameRoomJsn.put("type", "playerLeftGameRoom");
				if(player.getPlayerName().equals(roomowner.getPlayerName())){
					playerLeaveGameRoomJsn.put("name", "host");
					roomowner = players.get(0);
					playerLeaveGameRoomJsn.put("newHost", roomowner.getPlayerName());
				}
				playerLeaveGameRoomJsn.put("name", player.getPlayerName());
				
				bcast(playerLeaveGameRoomJsn.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void onClose(){
		players.clear();
		roomowner = null;
	}
	
	public String getCurrentAnswerWord(){
		return wordlist.getAnswerWord(currentWord);
	}
	
	public boolean isGameFinished(){
		return gameFinished;
	}
	
	public Player getRoomowner() {
		return roomowner;
	}

	public void setRoomowner(Player roomowner) {
		this.roomowner = roomowner;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void addPlayer(Player player) {
		players.add(player);
	}
	
	public boolean isStarted(){
		return gameStarted;
	}
	
	public int getCurrentWordIndex() {
		return currentWord;
	}
	
	public String getCurrentKeyboardWord(){
		if(currentWord < wordlist.getSize()){
			return wordlist.getKeyboardWord(currentWord);
		}
		return "no more word";
	}

	public WordList getWordlist() {
		return wordlist;
	}
}
