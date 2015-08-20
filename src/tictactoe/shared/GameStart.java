/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class GameStart extends Message
{
    private static final long serialVersionUID = -5990159462956951391L;
    public int gameId;
    public Player playerX;
    public Player playerO;
    
    public GameStart() 
    {
        type = Type.GameStart;
    }
    
    public GameStart( int gameId, Player playerX, Player playerO ) 
    {
        this.type = Type.GameStart;
        this.gameId = gameId;
        this.playerX = playerX;
        this.playerO = playerO;
    }
}
