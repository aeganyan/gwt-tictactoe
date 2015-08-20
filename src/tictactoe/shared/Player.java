/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class Player implements java.io.Serializable
{
    private static final long serialVersionUID = -7598784253419397410L;
    public String name;
    public int id;
    
    public Player() 
    {
        // Required for serialization
    }
    
    public Player( String name, int id )
    {
        this.name = name;
        this.id = id;
    }
}
