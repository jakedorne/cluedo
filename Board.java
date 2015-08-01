package cluedo;

import java.io.File;
import java.util.*;

import cluedo.action.Accuse;
import cluedo.action.Guess;
import cluedo.action.Move;
import cluedo.cards.Card;
import cluedo.cards.Character;
import cluedo.cards.Room;
import cluedo.cards.Weapon;

public class Board {

	private List<Card> win = new ArrayList<Card>(); // the cards 
	
	private List<Player> players = new ArrayList<Player>(); // players in the current game
	
	private List<Room> rooms = new ArrayList<Room>();
	private List<Weapon> weapons = new ArrayList<Weapon>();
	private List<Character> characters = new ArrayList<Character>();
	
	private char[][] board; // hard code board please
	private Player[][] playerPositions;
	
	private Player currentPlayer;

	
	/**
	 * Construct a board from a list of players
	 * @param players - list of players in the game
	 */
	public Board(List<Player> players) {
		this.players = players;
		currentPlayer = players.get(0);
		
		setBoard(createBoard());
		playerPositions = setPlayerPositions();
		
		//weapons
		getWeapons().add(new Weapon("Dagger"));
		getWeapons().add(new Weapon("Revolver"));
		getWeapons().add(new Weapon("Lead Pipe"));
		getWeapons().add(new Weapon("Rope"));
		getWeapons().add(new Weapon("Spanner"));
		getWeapons().add(new Weapon("Candlestick"));
		//rooms
		getRooms().add(new Room("Kitchen", null));
		getRooms().add(new Room("Ball Room", null));
		getRooms().add(new Room("Conservatory", null));
		getRooms().add(new Room("Billiard Room", null));
		getRooms().add(new Room("Library", null));
		getRooms().add(new Room("Study", null));
		getRooms().add(new Room("Hall", null));
		getRooms().add(new Room("Lounge", null));
		getRooms().add(new Room("Dining Room", null));
		
		getRooms().get(0).setConnectedTo(getRooms().get(5));
		getRooms().get(5).setConnectedTo(getRooms().get(0));
		getRooms().get(2).setConnectedTo(getRooms().get(7));
		getRooms().get(7).setConnectedTo(getRooms().get(2));
		
		
		//characters
		getCharacters().add(new Character("Miss Scarlett"));
		getCharacters().add(new Character("Colonel Mustard"));
		getCharacters().add(new Character("Mrs. White"));
		getCharacters().add(new Character("The Reverand Green"));
		getCharacters().add(new Character("Mrs. Peacock"));
		getCharacters().add(new Character("Professor Plum"));
		
		distributeCards();
		setPlayerPositions();
	}
	
	/**
	 * Creates the board array by reading from the board text file
	 * 
	 * @return the array for the board
	 */
	private char[][] createBoard() {
		try {
			Scanner scan = new Scanner(new File("board.txt"));
			char[][] board = new char[25][25];
			int index = 0;
			while(scan.hasNextLine()) {
				String s = scan.nextLine();
				if(s.startsWith("#")){continue;}
				char[] line = s.toCharArray();
				board[index] = line;
				index++;
			}
			scan.close();
			return board;
		}catch(Exception e) {
			System.out.println("Error reading file:  "+ e);
		}
		return null;
	}
	
	
	private void redraw(){
		for(int i = 0; i < getBoard().length; i++){
			for(int j = 0; j < getBoard()[0].length; j++){
				if(playerPositions[i][j]==null){
					System.out.print(getBoard()[i][j]+" ");
				} else{System.out.print(players.indexOf(playerPositions[i][j])+1 +" ");}
			}
			System.out.print("\n");
		}
	}
	
    /**
     * Shuffles cards randomly and distributes to all players as well as deciding the winning 3 cards.
     * 
     */
	public void distributeCards(){
		ArrayList<Card> allcards = new ArrayList<Card>();
		
		Collections.shuffle(getRooms());
		Collections.shuffle(getWeapons());
		Collections.shuffle(getCharacters());
		
		win.add(getRooms().get(0));	
		win.add(getWeapons().get(0));
		win.add(getCharacters().get(0));
		
		for(Card c: win){
			System.out.println(c);
		}
		
		allcards.addAll(getRooms().subList(1, getRooms().size()-1));
		allcards.addAll(getWeapons().subList(1, getWeapons().size()-1));
		allcards.addAll(getCharacters().subList(1, getCharacters().size()-1));
		
		for(Card c: allcards){
			Player p = nextPlayer();
			p.addCard(c);
		}
	}
	
	
	public Player[][] setPlayerPositions(){
		Player[][] board = new Player[25][25];
		int index = 0;
		for(Player p: players){
			board[index][index] = p;
			p.move(new Position(index, index));
			index++;
		}
		return board;
	}
	
	/**
	 * takes two positions and moves player at old position to new position.
	 * @param oldPos & newPos - the positions to move player from.
	 */
	public void move(Position oldPos, Position newPos){
		Player p = playerPositions[oldPos.row()][oldPos.col()];
		playerPositions[newPos.row()][newPos.col()] = p;
		playerPositions[oldPos.row()][oldPos.col()] = null;
		p.move(newPos);
	}
	
	/**
	 * Check to see if a guess was correct.
	 * 
	 * @param guess - the cards that the player guessed.
	 * @return
	 */
	public boolean checkGuess(List<Card> guess) {
		return win.containsAll(guess);
	}
	
	/**
	 * Have turn for a player
	 * 
	 * @param player - player that is having a turn
	 * @return
	 */
	public String haveNextTurn(Player player) {
		redraw();
		
		
		System.out.println(currentPlayer.getName()+"'s turn.");
		//Dice rolling
		Random rand = new Random();
		int roll = rand.nextInt(5)+1;
		currentPlayer.setRoll(roll);
		System.out.println("You rolled a "+roll);
		
		String[] options = player.getOptions();
		Scanner input = new Scanner(System.in);
		String toPrint = "Please select an option: ";
		for(String option : options) {
			
			toPrint += "["+option+"] ";
		}
		System.out.println(toPrint);
		String option; 
		option = input.next();
		int valid = calculatePlay(option);
		while(valid == 0 || valid == 2) {
			if(valid == 0){
			System.out.println("Invalid option");
			System.out.println("Please enter a new option");
			option = input.next();
			valid = calculatePlay(option);
			}
			else {
				option = input.next();
				valid = calculatePlay(option);
			}
		}
		return option;
		
	}

	
	public Player playerAt(Position p){
		return playerPositions[p.row()][p.col()];
	}
	
	
	/**
	 * Determine what happens for a given option.
	 * 
	 * @param option - the option selected by the player
	 */
	private int calculatePlay(String option) {
		option.toLowerCase();
		switch(option) {
		case "move":
			System.out.println("Player pos: "+currentPlayer.getCurrentPosition());
			Move playerMove = new Move(this, currentPlayer);
			while(!playerMove.isValid()){
				System.out.println("Unsuccessful Movement.");
				playerMove = new Move(this, currentPlayer);
			}
			move(playerMove.getOldPosition(), playerMove.getNewPosition());
			break;
		case "guess":
			currentPlayer.setRoom(rooms.get(0));
			if(currentPlayer.getRoom() != null) {
				Guess guess = new Guess(this, currentPlayer);
				for(Player p : players) {
					if(p.equals(currentPlayer)){continue;}
					for(Card c : p.getHand()) {
						for(Card card : guess.getCards()) {
							if(card.equals(c)) {
								System.out.println(p.getName()+" has one of the cards\n");
								return 1;
							}
						}
						
					}
				}
				break;
			}
			else {System.out.println("Must be in a room to make a suggestion!");}
			break;
		case "accuse":
			Accuse playerAccusation = new Accuse(this, currentPlayer);
			if(playerAccusation.isValid()){
				System.out.println("Correct accusation! "+currentPlayer.getName()+" wins");
				Main.gameFinished = true;
			} else{System.out.println("Incorrect accusation! "+currentPlayer.getName()+" loses");} 
			break;
		case "hand":
			currentPlayer.displayHand();
			return 2;
		default:
			return 0;
		}
		return 1;
	}
	


	/**
	 * Find next player to have a turn.
	 * 
	 * @return the next player
	 */
	public Player nextPlayer() {
		int index = players.indexOf(currentPlayer);
		if(index == players.size()-1) {
			currentPlayer = players.get(0);
		}
		else {
			currentPlayer = players.get(index+1);
		}
		return currentPlayer;
	}
	
	public Player getCurrentPlayer() {
		return currentPlayer;
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

	public char[][] getBoard() {
		return board;
	}

	public void setBoard(char[][] board) {
		this.board = board;
	}

}
