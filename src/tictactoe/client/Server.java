/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.client;

import java.util.ArrayList;

import tictactoe.shared.*;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("server")
public interface Server extends RemoteService 
{
    Integer login( String name ) throws RuntimeException;
    void makeMove( int gameId, int row, int column, int value ) throws RuntimeException;
    void startGame( int playerX, int playerO ) throws RuntimeException;
    ArrayList<Message> getMessages( int playerId );
    ArrayList<Player> getPlayers();
}
