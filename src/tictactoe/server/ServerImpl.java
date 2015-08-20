/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tictactoe.client.Server;
import tictactoe.shared.*;

/** 
 * <b>Only one instance</b> of this servlet can be used in a web application.
 * Otherwise, the server data (players and games) can be stored via: <br>
 * 1) servletContext() for single servers <br>
 * 2) JDO, JPA, etc for distributed servers
 */
@SuppressWarnings("serial")
public class ServerImpl extends RemoteServiceServlet implements Server
{
    private class PlayerInfo
    {
        ArrayList<Message> messages = new ArrayList<Message>();
        String name;
        int id;
        int gameId;
        boolean alive;
    }
    
    private ObjectMap<PlayerInfo> players = new ObjectMap<PlayerInfo>();
    private ObjectMap<Game> games = new ObjectMap<Game>();
    
    private static final int CHECK_ALIVE_PERIOD = 5000;
    private Date date = new Date(); // To imitate a timer for GAE
    
    
    // -----------------------------------------------------------------
    //  Public API
    // -----------------------------------------------------------------
    
    public synchronized Integer login( String name ) throws RuntimeException
    {
        if (name.isEmpty()) {
            throw new RuntimeException("Incorrect name");
        }
        if (hasPlayer(name)) {
            throw new RuntimeException("This name is already in use");
        }
        PlayerInfo player = new PlayerInfo();
        player.name = name;
        player.id = players.getId();
        player.alive = true;
        players.put(player.id, player);
        sendToAll(new PlayerEnter(player.name, player.id));
        return player.id;
    }
    
    public synchronized void makeMove( int gameId, int row, int column, int value ) throws RuntimeException
    {
        Game game = games.get(gameId);
        if (game == null) {
            throw new RuntimeException("Game was lost");
        }
        if (game.grid[row][column] != Game.None) {
            throw new RuntimeException("Incorrect move");
        }
        game.grid[row][column] = value;
        int opponent = (value == Game.X ? game.playerO : game.playerX);
        sendToPlayer(opponent, new Move(gameId, (byte)row, (byte)column, (byte)value));
        if (game.getResult().winner != Game.None) {
            endGame(gameId);
        }
    }

    public synchronized void startGame( int playerX, int playerO ) throws RuntimeException
    {
        PlayerInfo infoX = players.get(playerX);
        PlayerInfo infoO = players.get(playerO);
        if (infoX == null || infoO == null) {
            throw new RuntimeException("Your opponent has exited");
        }
        if (infoX.gameId != 0 || infoO.gameId != 0) {
            throw new RuntimeException("Your opponent has started another game");
        }
        
        Game game = new Game();
        game.playerX = playerX;
        game.playerO = playerO;
        game.id = games.getId();
        games.put(game.id, game);
        
        infoX.gameId = game.id;
        infoO.gameId = game.id;
        
        GameStart message = new GameStart();
        message.playerX = new Player(infoX.name, infoX.id);
        message.playerO = new Player(infoO.name, infoO.id);
        message.gameId = game.id;
        
        sendToPlayer(playerX, message);
        sendToPlayer(playerO, message);
        sendToAll(new PlayerExit(playerX));
        sendToAll(new PlayerExit(playerO));
    }
    
    private void endGame( int gameId )
    {
        Game game = games.remove(gameId);
        if (game == null) return;
        
        PlayerInfo playerX = players.get(game.playerX);
        PlayerInfo playerO = players.get(game.playerO);
        if (playerX == null || playerO == null) return;
        playerX.gameId = 0;
        playerO.gameId = 0;
        
        GameEnd message = new GameEnd(gameId, game.getResult());
        sendToPlayer(playerX.id, message);
        sendToPlayer(playerO.id, message);
        sendToAll(new PlayerEnter(playerX.name, playerX.id));
        sendToAll(new PlayerEnter(playerO.name, playerO.id));
        games.releaseId(game.id);
    }
    
    public synchronized ArrayList<Player> getPlayers()
    {
        ArrayList<Player> result = new ArrayList<Player>();
        for (PlayerInfo info : players.values()) {
            if (info.gameId > 0) continue;
            Player player = new Player();
            player.name = info.name;
            player.id = info.id;
            result.add(player);
        } 
        return result;
    }
    
    private boolean hasPlayer( String name )
    {
        for (Integer id : players.keySet()) {
            PlayerInfo player = players.get(id);
            if (player.name.equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    
    // -----------------------------------------------------------------
    //  Messaging
    // -----------------------------------------------------------------
    
    private void sendToPlayer( int playerId, Message Message )
    {
        PlayerInfo player = players.get(playerId);
        if (player != null) {
            player.messages.add(Message);
        }
    }
    
    private void sendToAll( Message Message )
    {
        for (PlayerInfo player : players.values()) {
            player.messages.add(Message);
        }
    }
    
    public synchronized ArrayList<Message> getMessages( int playerId )
    {
        // Imitate a timer for GAE
        Date currentDate = new Date();
        long timeElapsed = currentDate.getTime() - date.getTime();
        if (timeElapsed >= CHECK_ALIVE_PERIOD) {
            checkPlayers();
            date = currentDate;
        }
        
        // Send messages to the player
        PlayerInfo player = players.get(playerId);
        if (player == null) {
            return new ArrayList<Message>();
        }
        player.alive = true;
        if (player.messages.isEmpty()) {
            return player.messages;
        }
        ArrayList<Message> messages = new ArrayList<Message>(player.messages);
        player.messages.clear();
        return messages;
    }
    
    private void checkPlayers()
    {
        Iterator<Entry<Integer, PlayerInfo>> i = players.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<Integer, PlayerInfo> entry = i.next();
            PlayerInfo player = entry.getValue();
            if (!player.alive) {
                endGame(player.gameId);
                sendToAll(new PlayerExit(player.id));
                players.releaseId(player.id);
                i.remove();
            } else {
                player.alive = false;
            } 
        }
    }
}


@SuppressWarnings("serial")
final class ObjectMap<T> extends HashMap<Integer /*id*/, T>
{
    private ArrayList<Integer> freeIds = new ArrayList<Integer>();
    
    public void releaseId( Integer id )
    {
        if (id > 0) {
            freeIds.add(id);
        }
    }
    
    public Integer getId()
    {
        if (freeIds.size() > 0) {
            return freeIds.remove(0);
        }
        return size() + 1;
    }
}
