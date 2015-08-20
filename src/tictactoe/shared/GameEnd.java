/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class GameEnd extends Message
{
    private static final long serialVersionUID = 2631521453811355257L;
    public int gameId;
    public Game.Result result;
    
    public GameEnd()
    {
        type = Type.GameEnd;
    }
    
    public GameEnd( int gameId, Game.Result result )
    {
        this.type = Type.GameEnd;
        this.gameId = gameId;
        this.result = result;
    }
}
