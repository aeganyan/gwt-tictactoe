/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class Message implements java.io.Serializable
{
    private static final long serialVersionUID = -304342760842975972L;

    public enum Type 
    {
        PlayerEnter,    // Player entered the waiting room
        PlayerExit,     // Player exited the waiting room (by starting a game or closing the browser tab)
        GameStart,      // New game was started
        GameEnd,        // Game was ended
        Move,           // New move was done in the game
        Null
    }
    
    public Type type = Type.Null;
}
