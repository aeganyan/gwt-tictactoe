/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.client;

import java.util.ArrayList;

import tictactoe.shared.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerAsync 
{
    void login( String name, AsyncCallback<Integer> id ) throws RuntimeException;
    void makeMove( int gameId, int row, int column, int value, AsyncCallback<Void> none ) throws RuntimeException;
    void startGame( int playerX, int playerO, AsyncCallback<Void> none ) throws RuntimeException;
    void getMessages( int playerId, AsyncCallback<ArrayList<Message>> messages );
    void getPlayers( AsyncCallback<ArrayList<Player>> players );
}
