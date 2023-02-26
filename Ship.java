public class Ship {
    
    String id;
    int direction;
    int size;
    int startX;
    int startY;
    int hits = 0;
    String[] hull;
    boolean destroyed = false;
    
    public Ship(String id, int startX, int startY, int direction, int size){
        this.id = id;
        this.startX = startX;
        this.startY = startY;
        this.direction = direction;
        this.size = size;
        this.hull = new String[size];
        for(int i = 0; i < size; i++){
            hull[i] = "S";
        }
    }

    public String toString(){
        String out = "";
        for(String i : hull){
            out += i + " ";
        }
        return out;
    }

    public String getHull(int place){
        return hull[place];
    }

    public void bomb(int place){
        if(place < 0 || place >= size) return;
        if(hull[place].equals("S")){
            hull[place] = "X";
        }
        hits++;
        if(hits == size){
            destroyed = true;
        }
    }

    // public static void main(String[] args){
    //     Ship ship = new Ship("Penguin Ship",3,3,0,3);
    //     ship.bomb(2);
    //     System.out.println(ship);
    // }
}
