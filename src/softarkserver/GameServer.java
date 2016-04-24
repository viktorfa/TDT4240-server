package softarkserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

/**
 * 	Sends and receives messages as String JSON
 */
public class GameServer extends Listener {
	private Server server;
	private final int TCP_PORT =  54555;
	private final int UDP_PORT = 54777;
	private Kryo kryo;
	
	private ServerController serverController;
	
	public GameServer() {
		try {
			serverController = new ServerController();
		
			server = new Server();
			server.start();
			server.bind(TCP_PORT, UDP_PORT);
		
			kryo = server.getKryo();
			kryo.register(String.class);
			server.addListener(this);
		
			System.out.println("Server started.");
			getCmdInput();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void received(Connection connection, Object object) {
		super.received(connection, object);
	
		if(object instanceof String){
			try {
				JSONObject requestJson = new JSONObject((String)object);
				String requestType = requestJson.getString("type");
				if(!requestType.isEmpty()){
					switch (requestType) {
					case "getWordListNamesRequest":
						serverController.onGetWordListNameRequest(connection);
						break;
					case "createRoomRequest":
						serverController.onRoomCreation(connection, requestJson);
						break;
					case "joinLobbyRequest":
						serverController.onJoineLobbyRequest(connection, requestJson);
						break;
					case "setPlayerNameRequest":
						serverController.onSetPlayerName(connection, requestJson);
						break;
					case "startGameFromLobbyRequest":
						serverController.onStartGameFromLobbyRequest(connection, requestJson);
						break;
					case "readyToStartGame":
						serverController.onReadyToStartGameRequest(connection, requestJson);
						break;
					case "checkAnswerRequest":
						serverController.onCheckAnswerRequest(connection, requestJson);
						break;
					case "newPathDrawn":
						serverController.onNewPathDrawn(connection, requestJson);
						break;
					case "leavingGame":
						serverController.onPlayerLeave(connection, requestJson);
						break;
						
					default:
						break;
					}
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void getCmdInput(){
		Scanner scanner = new Scanner(System.in);
		
		String nextLine = scanner.nextLine();
		while(!nextLine.equals("exit")){
			switch (nextLine) {
			case "lol":
				System.out.println("lol indeed.");
				break;

			default:
				System.out.println("Could not recognize command.");
				break;
			}
			
			nextLine = scanner.nextLine();
		}
		System.out.println("Server closed.");
		server.close();
	}
}
