package softarkserver;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.kryonet.Connection;

/**
 * Manages all ongoing games
 */
public class GamesManager {
	private HashMap<Integer, GameRoom> gameRooms;
	private HashMap<Integer, Player> players;
	private HashMap<String, WordList> wordlist;
	
	private int gameRoomCounter;
	
	public GamesManager() {
		gameRooms = new HashMap<Integer, GameRoom>();
		players = new HashMap<Integer, Player>();
		
		wordlist = new HashMap<String, WordList>();
		wordlist.put("testList1", new WordList("testList1", "car:cfagrt", "fire:dffidrfeg", "apple:adsafpfperlet", "tree:astdhferretey", "firetruck:efwiereeetwdfdfdruerck", "sugar:dsfugfgeahgrh", "chips:dcfhgihphsh", "football:dfffogohhtbhalggl", "alphabet:adlffphfgeratbehht", "teenager:ftgehrternyagueyr"));
		wordlist.put("testList2", new WordList("testList2", "gaming:sgdafmgihnjhgj", "tv:jdfhtgffv", "even:rehvefghn"));
		wordlist.put("testList3", new WordList("testList3", "mouse:mkosudse", "shit:esrhtiytty"));
		wordlist.put("testList4", new WordList("testList4", "ja:ojka", "nei:enheji"));
		
		gameRoomCounter = 1;
	}
	
	public void handleGetWordListNameRequest(Connection connection){
		try {
			JSONArray words = new JSONArray();
			for(String key : this.wordlist.keySet()) {
				words.put(wordlist.get(key).getName());
			}
			
			JSONObject wordListJason = new JSONObject();
			wordListJason.put("type", "getWordListResponse");
			wordListJason.put("wordListNames", words);
			
			connection.sendTCP(wordListJason.toString());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleStartGameFromLobbyRequest(Connection connection, JSONObject jsn){
		try {
			String gamePinString = jsn.getString("gamePin");
			if(!gamePinString.isEmpty()){
				int gamePin = Integer.parseInt(gamePinString);
				gameRooms.get(gamePin).rdyToStartGameBCAst();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleRoomCreation(Connection connection, JSONObject json){
		try {
			String playerName = json.getString("playerName");
			String wordlistName = json.getString("wordListName");
			
			if(!playerName.isEmpty() && !wordlistName.isEmpty()){
				Player newPlayer = new Player(connection);
				newPlayer.setPlayerName(playerName);
				newPlayer.setGameromID(gameRoomCounter);
				players.put(connection.getID(), newPlayer);
				
				GameRoom newGameRoom = new GameRoom(this.wordlist.get(wordlistName));
				newGameRoom.setRoomowner(newPlayer);
				newGameRoom.addPlayer(newPlayer);
				gameRooms.put(gameRoomCounter, newGameRoom);
				
				JSONObject createRoomResponsJsn = new JSONObject();
				createRoomResponsJsn.put("type", "createRoomResponse");
				createRoomResponsJsn.put("gamePin", gameRoomCounter);
				
				connection.sendTCP(createRoomResponsJsn.toString());
				
				gameRoomCounter++;
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleJoineLobbyRequest(Connection con, JSONObject json){
		try {
			String gamePinString = json.getString("gamePin");
			
			if(!gamePinString.isEmpty()){
				int gamePin = Integer.parseInt(gamePinString);
				
				JSONObject joinLobbyResponse = new JSONObject();
				joinLobbyResponse.put("type", "joinLobbyResponse");
				
				if(gameRooms.containsKey(gamePin)){
					if(!gameRooms.get(gamePin).isStarted()){
						if(!gameRooms.get(gamePin).checkIfPlayerHasJoined(con)){
							Player newPlayer = new Player(con);
							newPlayer.setGameromID(gamePin);
							gameRooms.get(gamePin).addPlayer(newPlayer);
							players.put(con.getID(), newPlayer);
						}
						joinLobbyResponse.put("result", "accepted");
						joinLobbyResponse.put("gameOwner", gameRooms.get(gamePin).getRoomowner().getPlayerName());
					} else {
						joinLobbyResponse.put("result", "active");
					}
				} else {
					joinLobbyResponse.put("result", "game not found");
				}
				con.sendTCP(joinLobbyResponse.toString());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void handleSetPlayerName(Connection connection, JSONObject json){
		try {
			String name = json.getString("name");
			if(!name.isEmpty()){
				if(players.containsKey(connection.getID())){
					Player player = players.get(connection.getID());
					if(gameRooms.containsKey(player.getGameromID())){
							JSONObject setPlayerNameResponse = new JSONObject();
							setPlayerNameResponse.put("type", "setPlayerNameResponse");
							
							if(gameRooms.get(player.getGameromID()).checkNameAvailability(name)){
								player.setPlayerName(name);
								setPlayerNameResponse.put("result", "accepted");
								gameRooms.get(player.getGameromID()).notifyNewPlayerJoined(player);
							} else {
								setPlayerNameResponse.put("result", "taken");
							}
							connection.sendTCP(setPlayerNameResponse.toString());	
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void handlePlayerLeave(Connection connection, JSONObject jsn){
		if(players.containsKey(connection.getID())){
			if(gameRooms.containsKey(players.get(connection.getID()).getGameromID())){
				GameRoom gameRoom = gameRooms.get(players.get(connection.getID()).getGameromID());
				gameRoom.removePlayer(players.get(connection.getID()));
				if(gameRoom.isGameFinished()){
					gameRooms.remove(players.get(connection.getID()).getGameromID());
					gameRoom.onClose();
				}
			}
		}
	}
	
	public void handleReadyToStartGameRequest(Connection connection, JSONObject jsn){
		Player player = players.get(connection.getID());
		player.setRdyToStart(true);
		gameRooms.get(player.getGameromID()).attemptStartGame();
	}
	
	public void handleCheckAnswerRequest(Connection connection, JSONObject jsn){
		int gamePin = players.get(connection.getID()).getGameromID();
		gameRooms.get(gamePin).checkAnswer(players.get(connection.getID()), jsn);
	}

	public HashMap<Integer, GameRoom> getGameRooms() {
		return gameRooms;
	}

	public void setGameRooms(HashMap<Integer, GameRoom> gameRooms) {
		this.gameRooms = gameRooms;
	}

	public HashMap<Integer, Player> getPlayers() {
		return players;
	}

	public void setPlayers(HashMap<Integer, Player> players) {
		this.players = players;
	}

	public HashMap<String, WordList> getWordlist() {
		return wordlist;
	}

	public void setWordlist(HashMap<String, WordList> wordlist) {
		this.wordlist = wordlist;
	}

	public int getGameRoomCounter() {
		return gameRoomCounter;
	}

	public void setGameRoomCounter(int gameRoomCounter) {
		this.gameRoomCounter = gameRoomCounter;
	}
	
	public void handleNewPathDrawn(Connection connection, JSONObject jsn){
		this.gameRooms.get(players.get(connection.getID()).getGameromID()).addDrawPath(jsn);
	}
	
	
}
