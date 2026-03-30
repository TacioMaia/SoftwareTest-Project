package st.project;

import java.util.HashMap;

public class Room {
    private HashMap<String, Room> exits;
    private int posX;
    private int posY;
    private boolean isProfessor;

    public Room(int x, int y, boolean professor) {
        this.posX = x;
        this.posY = y;
        this.isProfessor = professor;
        exits = new HashMap<String, Room>();
    }

    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    public Room getExit(String direction) {
        return exits.get(direction);
    }

    public int getX() { return posX; }
    public int getY() { return posY; }
    public boolean isProfessorRoom() { return isProfessor; }
}