package softarkserver;

import org.json.JSONObject;

import com.esotericsoftware.kryonet.Connection;

public class ServerController {
	GamesManager gamesManager;
	
	public ServerController() {
		gamesManager = new GamesManager();
	}
	
	public void onGetWordListNameRequest(Connection connection){
		gamesManager.handleGetWordListNameRequest(connection);
	}
	
	public void onRoomCreation(Connection connection, JSONObject jsn){
		gamesManager.handleRoomCreation(connection, jsn);
	}
	
	public void onJoineLobbyRequest(Connection connection, JSONObject jsn){
		gamesManager.handleJoineLobbyRequest(connection, jsn);
	}
	
	public void onSetPlayerName(Connection connection, JSONObject jsn){
		gamesManager.handleSetPlayerName(connection, jsn);
	}
	
	public void onStartGameFromLobbyRequest(Connection connection, JSONObject jsn){
		gamesManager.handleStartGameFromLobbyRequest(connection, jsn);
	}
	
	public void onReadyToStartGameRequest(Connection connection, JSONObject jsn){
		gamesManager.handleReadyToStartGameRequest(connection, jsn);
	}
	
	public void onCheckAnswerRequest(Connection connection, JSONObject jsn){
		gamesManager.handleCheckAnswerRequest(connection, jsn);
	}
	
	public void onNewPathDrawn(Connection connection, JSONObject jsn){
		gamesManager.handleNewPathDrawn(connection, jsn);
	}
	
	public void onPlayerLeave(Connection connection, JSONObject jsn){
		gamesManager.handlePlayerLeave(connection, jsn);
	}
}
