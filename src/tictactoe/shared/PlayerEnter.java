/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class PlayerEnter extends Message
{
    private static final long serialVersionUID = -8667409734825432189L;
    public String name;
    public int id;
    
    public PlayerEnter()
    {
        type = Type.PlayerEnter;
    }
    
    public PlayerEnter( String name, int id ) 
    {
        this.type = Type.PlayerEnter;
        this.name = name;
        this.id = id;
    }
}
