/******************************************************************************
 * Copyright (c) 2015 Artur Eganyan
 *
 * This software is provided "AS IS", WITHOUT ANY WARRANTY, express or implied.
 ******************************************************************************/

package tictactoe.client;

import java.util.ArrayList;

import tictactoe.shared.*;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Client implements EntryPoint 
{
    private final ServerAsync server = GWT.create(Server.class);
    private final Callback<Void> defaultCallback = new Callback<Void>();
    private static final int UPDATE_PERIOD = 1000;
    
    private Player player = new Player();
    private int playerType = Game.None;
    private Game game = new Game();
    private boolean canMove = false;
    
    private final Grid grid = new Grid(Game.GRID_SIZE, Game.GRID_SIZE);
    private final ListBox playerList = new ListBox();
    private final HTML messageText = new HTML();
    private final HTML titleText = new HTML();
    private final HTML errorText = new HTML();
    private final HTML loginText = new HTML();
    
    private Panel loginWindow;
    private Panel mainWindow;
    
    
    // -----------------------------------------------------------------
    //  User interface
    // -----------------------------------------------------------------
    
    public void onModuleLoad() 
    {
        // Setup UI
        // ... Main window
        playerList.setWidth("140px");
        playerList.setVisibleItemCount(9);
        
        final HTML playerListTitle = new HTML("Players available:");
        playerListTitle.addStyleName("text");
        
        final VerticalPanel playerListPanel = new VerticalPanel();
        playerListPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        playerListPanel.setSpacing(4);
        playerListPanel.add(playerListTitle);
        playerListPanel.add(playerList);
        
        grid.setStylePrimaryName("grid");
        for (int r = 0; r < Game.GRID_SIZE; ++ r) {
            for (int c = 0; c < Game.GRID_SIZE; ++ c) {
                grid.setHTML(r, c, "<div></div>");
            }
        }
        
        final VerticalPanel gridPanel= new VerticalPanel();
        gridPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        gridPanel.setSpacing(12);
        gridPanel.add(titleText);
        gridPanel.add(grid);
        gridPanel.add(messageText);
        
        titleText.setWordWrap(false);
        titleText.addStyleName("text");
        messageText.addStyleName("text");
        showTitle("<< Double-click an opponent");
        showMessage("");
        
        final HorizontalPanel mainLayout = new HorizontalPanel();
        mainLayout.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        mainLayout.setSpacing(24);
        mainLayout.add(playerListPanel);
        mainLayout.add(gridPanel);
        
        mainWindow = RootPanel.get("mainWindow");
        addToCenter(mainLayout, mainWindow);
        
        errorText.setStyleName("error");
        mainWindow.add(errorText);
        
        // ... Login window
        final Button enterButton = new Button("Enter");
        final TextBox nameField = new TextBox();
        
        final VerticalPanel loginPanel = new VerticalPanel();
        loginPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        loginPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        loginPanel.setSpacing(4);
        loginPanel.add(loginText);
        loginPanel.add(nameField);
        loginPanel.add(enterButton);
        loginText.setText("Enter your name:");
        
        final DecoratorPanel loginFrame = new DecoratorPanel();
        loginFrame.add(loginPanel);
        
        final VerticalPanel loginLayout = new VerticalPanel();
        loginLayout.add(new HTML("<h1 style='margin: 14px'>Tic-Tac-Toe</h1>"));
        loginLayout.add(loginFrame);
        
        loginWindow = RootPanel.get("loginWindow");
        addToCenter(loginLayout, loginWindow);
        nameField.setFocus(true);

        // Setup handlers
        // ... Main window
        playerList.addDoubleClickHandler(new DoubleClickHandler()
        {
            public void onDoubleClick( DoubleClickEvent event )
            {
                if (game.id > 0) return;
                int index = playerList.getSelectedIndex();
                int opponentId = Integer.parseInt(playerList.getValue(index));
                startGame(player.id, opponentId);
            }
        });
        
        grid.addClickHandler(new ClickHandler() 
        {
            public void onClick( ClickEvent event )
            {
                Cell cell = grid.getCellForEvent(event);
                int row = cell.getRowIndex();
                int column = cell.getCellIndex();
                makeMove(row, column, playerType);
            }
        });
        
        // ... Login window
        enterButton.addClickHandler(new ClickHandler() 
        {
            public void onClick( ClickEvent event )
            {
                login(nameField.getText());
            }
        });
        
        nameField.addKeyUpHandler(new KeyUpHandler()
        {
            public void onKeyUp( KeyUpEvent event ) 
            {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    enterButton.click();
                }
            }
        });

        // ... Messaging
        Timer timer = new Timer() 
        {
            public void run() 
            {
                getMessages();
            }
        };
        timer.scheduleRepeating(UPDATE_PERIOD);
    }
    
    /** The container must have "centered" class and fixed size */
    private void addToCenter( Widget widget, Panel container )
    {
        VerticalPanel layout = new VerticalPanel();
        layout.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        layout.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        layout.addStyleName("fullsize");
        layout.add(widget);
        container.add(layout);
    }
    
    
    // -----------------------------------------------------------------
    //  Message handling
    // -----------------------------------------------------------------
    
    public void onMessage( Message m )
    {
        switch (m.type) {
            case Move: 
                onMove((Move)m); 
                break;
                
            case GameStart: 
                onGameStart((GameStart)m); 
                break;
                
            case GameEnd: 
                onGameEnd((GameEnd)m); 
                break;
                
            case PlayerEnter: 
                onPlayerEnter((PlayerEnter)m); 
                break;    
                
            case PlayerExit: 
                onPlayerExit((PlayerExit)m); 
                break;
                
            default: 
                // Skip this message
        }
    }
    
    private void onMove( Move m )
    {
        drawMove(m.row, m.column, m.value);
        canMove = true;
    }
    
    private void onGameStart( GameStart m )
    {
        Player playerX = m.playerX;
        Player playerO = m.playerO;
        
        game = new Game();
        game.playerX = playerX.id;
        game.playerO = playerO.id;
        game.id = m.gameId;
        
        canMove = (playerX.id == player.id);
        playerType = (playerX.id == player.id ? Game.X : Game.O);
        playerList.setEnabled(false);
        playerList.setSelectedIndex(-1);
        showTitle("<b>" + playerX.name + "</b> vs <b>" + playerO.name + "</b>");
        showMessage(canMove ? "Your turn (playing for X)" : "Opponent moves...");
        clearGrid();
    }
    
    private void onGameEnd( GameEnd m )
    {
        if (m.result.winner == Game.None) {
            showMessage("<font color='darkred'>Your opponent has left the game</font>");
        } else {
            showResult(m.result);
        }
        game = new Game();
        canMove = false;
        showTitle("<< Select an opponent");
        playerList.setEnabled(true);
    }
    
    private void onPlayerEnter( PlayerEnter m )
    {
        if (!player.name.equals(m.name) && findPlayerInList(m.id) < 0) {
            playerList.addItem(m.name, String.valueOf(m.id));
        }
    }
    
    private void onPlayerExit( PlayerExit m )
    {
        int index = findPlayerInList(m.id);
        if (index >= 0) {
            playerList.removeItem(index);
        }
    }
    
    private void getMessages()
    {
        server.getMessages(player.id, new Callback<ArrayList<Message>>() 
        {    
            public void onSuccess( ArrayList<Message> messages ) 
            {
                for (Message m : messages) {
                    onMessage(m);
                }
            }
        });        
    }
    
    
    // -----------------------------------------------------------------
    //  Game actions
    // -----------------------------------------------------------------
    
    private void login( final String name )
    {
        loginText.setText("Please wait...");
        server.login(name, new Callback<Integer>() 
        {
            public void onFailure( Throwable error ) 
            {
                loginText.setHTML("<font color='darkred'>" + error.getMessage() + "</font>");
            }
            
            public void onSuccess( Integer id ) 
            {
                player.name = name;
                player.id = id;
                loginWindow.setVisible(false);
                mainWindow.setVisible(true);
                updatePlayerList();
            }
        });            
    }
    
    private void startGame( int playerX, int playerO )
    {
        showTitle("Please wait...");
        showError("");
        server.startGame(playerX, playerO, defaultCallback);
    }
    
    private void makeMove( final int row, final int column, int value ) 
    { 
        if (!canMove || game.grid[row][column] != Game.None) return;
        drawMove(row, column, value);
        canMove = false;
        server.makeMove(game.id, row, column, value, defaultCallback);
    }
    
    private void drawMove( int row, int column, int value ) 
    {
        game.grid[row][column] = value;
        grid.getCellFormatter().setStyleName(row, column, value == Game.X ? "x" : "o");
        showMessage(value == playerType ? "Opponent moves..." : "Your turn");
        showResult(game.getResult());
    }
    
    private void showResult( Game.Result result )
    {
        int winner = result.winner;
        if (winner != Game.None) {
            if (winner == playerType) {
                showMessage("You won!");
            } else if (winner != Game.Draw) {
                showMessage("You lose");
            } else {
                showMessage("Draw. Game over.");
            }
            showCombo(result.combo);
        }
    }
    
    private void showCombo( int index )
    {
        if (index < 0 || index > Game.combo.length) return;
        CellFormatter formatter = grid.getCellFormatter();
        int[][] combo = Game.combo[index];
        for (int[] cell : combo) {
            int row = cell[0];
            int column = cell[1];
            formatter.addStyleName(row, column, "win");
        }
    }
    
    private void clearGrid()
    {
        for (int r = 0; r < Game.GRID_SIZE; ++ r) {
            for (int c = 0; c < Game.GRID_SIZE; ++ c) {
                grid.getCellFormatter().setStyleName(r, c, "");
            }
        }
    }
    
    private void clearAll()
    {
        game = new Game();
        clearGrid();
        showTitle("Select an opponent");
        showMessage("");
        showError("");
        updatePlayerList();
        playerList.setEnabled(true);
    }
    
    private void updatePlayerList()
    {
        server.getPlayers(new Callback<ArrayList<Player>>() 
        {
            public void onSuccess( ArrayList<Player> players ) 
            {
                playerList.clear();
                for (Player p : players) {
                    if (!p.name.equals(player.name)) {
                        playerList.addItem(p.name, String.valueOf(p.id));
                    }
                }
            }
        });
    }
    
    public void onError( Throwable error )
    {
        clearAll();
        showError("Error: " + error.getMessage());
    }
    

    // -----------------------------------------------------------------
    //  Helpers
    // -----------------------------------------------------------------
    
    private void showMessage( String message )
    {
        messageText.setHTML(message.isEmpty() ? "&nbsp": message);
    }

    private void showTitle( String title )
    {
        titleText.setHTML(title.isEmpty() ? "&nbsp" : title);
    }
    
    private void showError( String error )
    {
        errorText.setHTML(error);
    }
    
    private int findPlayerInList( int playerId )
    {
        int count = playerList.getItemCount();
        for (int i = 0; i < count; ++ i) {
            int id = Integer.parseInt(playerList.getValue(i));
            if (id == playerId) {
                return i;
            }
        }
        return -1;
    }
    
    class Callback<T> implements AsyncCallback<T>
    {
        public void onFailure( Throwable error ) 
        {
            onError(error);
        }
        
        public void onSuccess( T result ) 
        {
            // Does nothing, reimplement it
        }    
    }
}

