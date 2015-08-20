/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.shared;

public class Move extends Message
{
    private static final long serialVersionUID = 5465075654316710711L;
    public int gameId;
    public byte column;
    public byte row;
    public byte value;
    
    public Move() 
    {
        type = Type.Move;
    }
    
    public Move( int gameId, byte row, byte column, byte value ) 
    {
        this.type = Type.Move;
        this.gameId = gameId;
        this.column = column;
        this.row = row;
        this.value = value;
    }
}
