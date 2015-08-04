package cluedo.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cluedo.Game;
import cluedo.Player;
import cluedo.cards.Room;
import cluedo.tile.DoorTile;
import cluedo.tile.FloorTile;
import cluedo.tile.Tile;

public class Leave extends Action{
	private Room room;

	public Leave(Game game, Player player, Room room) {
		super(game, player);
		this.room = room;
	}

	public void run() {
		System.out.println("Which door would you like to leave from? ");
		displayDoors();
		Scanner input = new Scanner(System.in);
		int door = input.nextInt();
		DoorTile dt = room.getDoors().get(door-1);
		List<Tile> tiles = adjacentTiles(dt);
		for(Tile t: tiles) {
			if(t instanceof FloorTile) {
				game.getBoard().move(player.getCurrentPosition(), t.getPosition());
				player.setRoom(null);
				room.removePlayer(player);
				t.setPlayer(null);
				return;
			}
		}
	}

	private void displayDoors() {
		String toPrint = "";
		int index = 1;
		for(DoorTile d: room.getDoors()) {
			toPrint += "[Door "+index+"]";
		}
		System.out.println(toPrint);
	}

	public List<Tile> adjacentTiles(DoorTile dt) {
		Tile[][] board = game.getBoardArray();
		List<Tile> tiles = new ArrayList<Tile>();
		int x = dt.getPosition().col();
		int y = dt.getPosition().row();
		if(x < 23){tiles.add(board[y][x+1]);}
		if(x > 0){tiles.add(board[y][x-1]);}
		if(y < 24){tiles.add(board[y+1][x]);}
		if(y > 0){tiles.add(board[y-1][x]);}
		return tiles;
	}

	@Override
	public boolean isValid() {
		return false;
	}

}