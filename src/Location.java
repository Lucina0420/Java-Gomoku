public class Location {
	private int x; //x-coordinate
	private int y; // y=coordinate
	private int player; // the player (1: player 1, -1: player 2)
	
	public Location(int x, int y, int owner) {
		this.x = x;
		this.y = y;
		this.player = owner;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
    public int getPlayer() {
        return player;
    }
}