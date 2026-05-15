package st.project;

import java.util.HashMap;

public class Room {
    private HashMap<String, Room> exits;
    private int posX;
    private int posY;
    private int tipo; 

    public Room(int x, int y, int tipo) {
        this.posX = x;
        this.posY = y;
        this.tipo = tipo;
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
    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }
    
    public boolean isProfessorRoom() { return tipo == 2; }
}