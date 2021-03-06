package cluedo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import cluedo.action.Enter;
import cluedo.action.Leave;
import cluedo.action.Move;
import cluedo.cards.Card;
import cluedo.cards.Character;
import cluedo.cards.Room;
import cluedo.cards.Weapon;
import cluedo.gui.Frame;
import cluedo.tile.DoorTile;
import cluedo.tile.Tile;

/**
 * 
 * Hold all the logic for playing a game of Cluedo
 *
 */
public class Game {

	

	private Board board; // the board for the game
	private List<Player> players; // all players in the game (including players who have lost
	private Player currentPlayer; // player who is having their turn
	
	private List<Card> envelope = new ArrayList<Card>(); // the cards of the murderer

	private List<Room> rooms = new ArrayList<Room>();
	private List<Weapon> weapons = new ArrayList<Weapon>();
	private List<Character> characters = new ArrayList<Character>();
	
	private Frame frame;


	public Game(Frame frame) {
		this.frame = frame;
		players = setupGame(); // start the game
		if(players == null) {
			System.exit(0);
		}
		currentPlayer = players.get(0); // the first player to play is player 1
		
		//construct cards
		//weapons
		weapons.add(new Weapon("Dagger"));
		weapons.add(new Weapon("Revolver"));
		weapons.add(new Weapon("Lead Pipe"));
		weapons.add(new Weapon("Rope"));
		weapons.add(new Weapon("Spanner"));
		weapons.add(new Weapon("Candlestick"));
		//rooms
		rooms.add(new Room("Kitchen", null, 'K', 'U'));
		rooms.add(new Room("Ball Room", null, 'B', 'T'));
		rooms.add(new Room("Conservatory", null, 'C', 'R'));
		rooms.add(new Room("Billiard Room", null, 'I', 'Q'));
		rooms.add(new Room("Library", null, 'A', 'P'));
		rooms.add(new Room("Study", null, 'S', 'Z'));
		rooms.add(new Room("Hall", null, 'H', 'Y'));
		rooms.add(new Room("Lounge", null, 'L', 'W'));
		rooms.add(new Room("Dining Room", null, 'D', 'V'));
		rooms.add(new Room ("Swimming Pool", null, 'X', 'G'));

		rooms.get(0).setConnectedTo(rooms.get(5));
		rooms.get(5).setConnectedTo(rooms.get(0));
		rooms.get(2).setConnectedTo(rooms.get(7));
		rooms.get(7).setConnectedTo(rooms.get(2));


		//characters
		characters.add(new Character("Miss Scarlett"));
		characters.add(new Character("Colonel Mustard"));
		characters.add(new Character("Mrs. White"));
		characters.add(new Character("The Reverend Green"));
		characters.add(new Character("Mrs. Peacock"));
		characters.add(new Character("Professor Plum"));
		
		// deal cards to players and create murderer
		distributeCards();
		//set up the board
		board = new Board(players,this);
	}
	
	/**
	 * get user to input amount of players and names for the players
	 * @return list of players that are in the game
	 */
	public List<Player> setupGame() {
		int amount = 0;
		// repeat until a correct number is entered  
		while(amount < 3 || amount > 6) {
			//player number input dialog
			String inputValue = (String)JOptionPane.showInputDialog(null, "Enter amount of players (3-6): ", "Cluedo", JOptionPane.QUESTION_MESSAGE, null, null, null);
			//number not entered, end game
			if(inputValue == null) {
				return null;
			}
			// check for invalid input (ie not and integer)
			try {
				amount = Integer.parseInt(inputValue);
			}catch(NumberFormatException e) {
				JOptionPane.showMessageDialog(null,"Please enter an integer.", "Error", JOptionPane.OK_OPTION);
			}
		}
		List<Player> players = new ArrayList<Player>();
		for(int i = 1; i < amount+1; i++) {
			// get name for each player via dialog
			String name = JOptionPane.showInputDialog(null,"Enter player "+i+"'s name: ", "Player "+i, JOptionPane.QUESTION_MESSAGE);
			if(name == null){return null;}
			//shouldn't be able to enter the same name as another player
			for(Player p: players) {
				if(p.getName().equals(name)) {
					while(p.getName().equals(name)) {
						JOptionPane.showMessageDialog(null, "Name already in use!\nSelect a different name.");
						name = (String)JOptionPane.showInputDialog(null, "Enter new name: ", "Cluedo", JOptionPane.QUESTION_MESSAGE, null, null, null);
						if(name == null){return null;}
					}
				}
			}
			players.add(new Player(name, this, i+""));
		}
		return players;
	}	
	
	/**
	 * ends the current players turn
	 */
	public void endTurn() {
		nextPlayer();
		if(hasWon()) {frame.gameOver(currentPlayer);}
	}
	
	
	
	
	/**
	 * roll the dice for the current player
	 */
	public void diceRoll() {
		int roll = currentPlayer.roll();
		Output.appendText("You rolled a "+roll+"\n");
	}

	/**
	 * set the current player to the next player in the list to have their turn
	 * @return the player that is currently having their turn
	 */
	public Player nextPlayer() {
		int i = players.indexOf(currentPlayer);
		do{
			i++;
			if(i >= players.size()){i = 0;}
		}
		while(players.get(i).hasLost());
		Player play = players.get(i);
		if(play.equals(currentPlayer)){
			return null;
		}
		currentPlayer = play;
		return play;
	}

	/**
	 * find and return the player to have a turn after the given player
	 * 
	 * @param player
	 * @return next player
	 */
	public Player nextPlayer(Player player) {
		int index = players.indexOf(player);
		if(index == players.size()-1) {
			return players.get(0);
		}
		else {
			return players.get(index+1);
		}
	}


    /**
     * Shuffles cards randomly and distributes to all players as well as deciding the winning 3 cards.
     *
     */
	public void distributeCards(){
		ArrayList<Card> allcards = new ArrayList<Card>();

		Collections.shuffle(rooms);
		Collections.shuffle(weapons);
		Collections.shuffle(characters);

		envelope.add(rooms.get(0));
		envelope.add(weapons.get(0));
		envelope.add(characters.get(0));

		allcards.addAll(rooms.subList(1, rooms.size()));
		allcards.addAll(weapons.subList(1, weapons.size()));
		allcards.addAll(characters.subList(1, characters.size()));
		Player p = currentPlayer;
		for(Card c: allcards){
			System.out.println(c.toString());
			if(c.toString().equals("[Swimming Pool]")) {
				System.out.println("removed swimming pool");
				continue;} // dont add the swimming pool to a players hand
			p.addCard(c);
			p = nextPlayer(p);
		}
	}
	
	/**
	 * called when a door is clicked for the current player to leave the room they are in
	 * 
	 * @param tile the tile that was clicked on the board
	 */
	public void doorClicked(Tile tile) {
		DoorTile door = (DoorTile) tile;
		if(currentPlayer.getRoom()!=null && currentPlayer.getRoom().equals(door.getRoom())){
			Leave leave = new Leave(this,currentPlayer, door);
			leave.run();
		} 
		
		
	}
	
	
	/**
	 * Attempts to move player when a direction key is detected by making a
	 * new move object and checking validity of move.
	 * @param dir - direction detected by the keyListener in the Frame class
	 */
	public void moveDetected(String dir){
		if(currentPlayer.getRoll()>0){
			Move move = new Move(this, currentPlayer);
			move.setNewPosition(move.moveDirection(dir));
			if(move.isValidMove()){
				movePlayer(currentPlayer, move.getOldPosition(), move.getNewPosition());
				currentPlayer.setRoll(currentPlayer.getRoll()-1);
				if(board.getBoard()[currentPlayer.getCurrentPosition().row()][currentPlayer.getCurrentPosition().col()] instanceof DoorTile){
					Enter ent = new Enter(this, currentPlayer);
					ent.run();
				}				
			}
		} else{Output.appendText("You have no moves left!");}

	}
	
	/**
	 * move a player to a new position
	 * 
	 * @param player to move
	 * @param oldPos - old position of the player
	 * @param newPos - position that the player is moving to
	 */
	public void movePlayer(Player player, Position oldPos, Position newPos){
		board.move(oldPos, newPos);
		frame.movePlayer(player, oldPos, newPos);
		
	}

	/**
	 * do an accuse or guess
	 * @param character that was guessed
	 * @param room that was guessed
	 * @param weapon that was guesses
	 * @param isAccuse - is it an accuse
	 */
	public void guessAccuse(String character, String room, String weapon, boolean isAccuse) {
		List<Card> guess = new ArrayList<Card>();
		//get character
		for(Character c : characters) {
			if(c.getName().equals(character)) {guess.add(c);}
		}
		if(isAccuse){
			// get room
			for(Room r : rooms) {
				if(r.getName().equals(room)) {guess.add(r);}
			}
		}
		else {guess.add(currentPlayer.getRoom());}
		// get weapon
		for(Weapon w : weapons) {
			if(w.getName().equals(weapon)) {guess.add(w);}
		}
		// if accuse do accuse logic
		if(isAccuse) {
			if(checkGuess(guess)) {
				frame.gameOver(currentPlayer);
				return;
			}
			else{
				JOptionPane.showMessageDialog(null, currentPlayer.getName()+" has lost!", "Incorrect", JOptionPane.INFORMATION_MESSAGE);
				//Output.appendText("Player "+currentPlayer.getName()+" has lost\n");
				lost();
				return;
			}
		}
		// else guess logic
		boolean hasCards = false;
		for(Player p : players) {
			if(p.equals(currentPlayer)){continue;}
			for(Card c : p.getHand()) {
				for(Card card : guess) {
					if(card.equals(c)) {
						hasCards = true;
						Output.appendText("Player "+p.getName()+" has one (or more) of the cards\n");
					}
				}
			}
		}
		if(!hasCards){Output.appendText("No one has any of the cards");}
	}
	
	/**
	 * make the current player lose and end the turn
	 */
	public void lost() {
		currentPlayer.lost(true);
		frame.getBoard().removePlayer(currentPlayer);;
		frame.endTurn();
	}


	/**
	 * Check to see if a guess was correct.
	 *
	 * @param guess - the cards that the player guessed.
	 * @return
	 */
	public boolean checkGuess(List<Card> guess) {
		return envelope.containsAll(guess);
	}
	
	/**
	 * check to see if the current player has won the game
	 * 
	 * @return
	 */
	public boolean hasWon() {
		for(Player p : players) {
			if(p.equals(currentPlayer)) {continue;}
			if(!p.hasLost()){
				return false;
			}
		}
		return true;
	}
	
	
	
	/*
	 * Getters and setters
	 */

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public void setCurrentPlayer(Player currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public List<Weapon> getWeapons() {
		return weapons;
	}

	public void setWeapons(List<Weapon> weapons) {
		this.weapons = weapons;
	}

	public List<Character> getCharacters() {
		return characters;
	}

	public void setCharacters(List<Character> characters) {
		this.characters = characters;
	}

	public List<Room> getRooms() {
		return rooms;
	}

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Tile[][] getBoardArray() {
		return board.getBoard();
	}

	public Board getBoard() {
		return board;
	}

	public Player[][] getPlayerPositions() {
		return board.getPlayerPositions();
	}


	







}
