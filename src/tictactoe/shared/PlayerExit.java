/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class PlayerExit extends Message
{
    private static final long serialVersionUID = 4264086267024778793L;
    public int id;
    
    public PlayerExit() 
    {
        type = Type.PlayerExit;
    }
    
    public PlayerExit( int id ) 
    {
        this.type = Type.PlayerExit;
        this.id = id;
    }
}
