import java.util.Random;
import java.awt.*;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ShipBombingGame {
    
    //Debug
    private boolean autoPlace;
    private boolean autoPlay;
    private boolean seeEnemyBoards;

    //region Constants
    final String CLEARSCREEN = "\033[H\033[2J";
    final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    final int CHOOSING_DIFFICULTY = 0;
    final int PLACING_SHIPS = 1;
    final int PLAYING_GAME = 2;
    final int GAME_LOST = 3;
    final int GAME_WON = 4;
    final int GAME_OVER = 5;
    final String[][] SHIPS = {{"Carrier", "5"},
                              {"Battleship", "4"},
                              {"Destroyer", "3"},
                              {"Submarine", "3"},
                              {"Patrol Boat", "2"}};
    //endregion

    //region Game Variables
    Ship[][] playerBoard;
    Ship[][] enemyBoard;
    String[][] shots;
    String[][] enemyShots;
    int gameState = PLACING_SHIPS;
    int maxShips = 5;
    int playerShips = 0;
    int enemyShips = 0;
    int difficulty = 0;
    int initialBoardSize;
    boolean started = false;
    //endregion

    //region Private Variables
    private int autoPlayX = 0;
    private int autoPlayY = 0;
    
    //AI Variables
    private boolean enemyLastShotHit = false;
    private int enemyLastShotX;
    private int enemyLastShotY;
    private int enemyLastShotDirection;
    private int enemyCurrentShotX;
    private int enemyCurrentShotY;
    //endregion
    
    //region UI
    int shipSquareSize = 50;
    int shipBoardStartX;
    int shipBoardStartY;

    int shotSquareSize = 35;
    int shotBoardStartX;
    int shotBoardStartY;
    int shotSquareOffset;

    Font shipFont;
    float shipFontSize = shipSquareSize * 0.70f;
    int shipTextOffsetX = shipSquareSize / 3;
    int shipTextOffsetY;

    ArrayList<String> messages = new ArrayList<String>();
    //endregion

    //Constructor
    public ShipBombingGame(int boardSize){
        if(boardSize > 26) boardSize = 26;
        else if (boardSize < 1) boardSize = 1;
        initialBoardSize = boardSize;
        playerBoard = new Ship[boardSize][boardSize];
        enemyBoard = new Ship[boardSize][boardSize];

        shots = new String[boardSize][boardSize];
        for(int j = 0; j < shots.length; j++){
            for(int i = 0; i < shots[j].length; i++){
                shots[j][i] = "";
            }
        }

        enemyShots = new String[boardSize][boardSize];
        for(int j = 0; j < enemyShots.length; j++){
            for(int i = 0; i < enemyShots[j].length; i++){
                enemyShots[j][i] = "";
            }
        }
        
        shotSquareOffset = Math.round((shipSquareSize - shotSquareSize) / 2f);
        
        shipTextOffsetY = Math.round(shipFontSize) + 2;
        
        try {
            shipFont = Font.createFont(Font.TRUETYPE_FONT, new File("ShareTechMono-Regular.ttf")).deriveFont(shipFontSize);
        } catch(IOException e){
            System.out.println("Error loading font");
            shipFont = new Font("Arial", Font.PLAIN, Math.round(shipFontSize));
        } catch(FontFormatException e){
            System.out.println("Error loading font");
            shipFont = new Font("Arial", Font.PLAIN, Math.round(shipFontSize));
        }
    }

    //region Private Game Methods
    private boolean isLetter(Character c){
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
    
    private boolean isDigit(Character c){
        return (c >= '0' && c <= '9');
    }

    private String shipBoardToString(){
        String out = "";
        String bar = "   ";
        for(int i = 0; i < playerBoard.length; i++){
            bar += "+---";
        }
        bar += "+\n";

        out += "Your Ships\n   ";
        for(int i = 0; i < playerBoard.length; i++){
            out += "  " + ALPHABET.charAt(i) + " ";
        }
        out += "\n";

        out += bar;
        for(int j = 0; j < playerBoard.length; j++){
            if(j < 10) out += j + "  |";
            else out += j + " |";
            for(int i = 0; i < playerBoard[j].length; i++){
                Ship ship = playerBoard[j][i];
                if(ship == null) out += "   |";
                else {
                    if(ship.direction == 0 && ship.getHull(i - ship.startX) == "X") out += "[X]|";
                    else if(ship.direction == 1 && ship.getHull(j - ship.startY) == "X") out += "[X]|";
                    else out += String.format(" %s |", ship.id.charAt(0));
                }
            }
            out += "\n" + bar;
        }
        return out;
    }

    private String enemyToString(){
        //Enemy's shot board
        String out = "Shot Board\n   ";
        
        for(int i = 0; i < enemyBoard.length; i++){
            out += "  " + ALPHABET.charAt(i) + " ";
        }
        out += "\n";

        String bar = "   ";
        for(int i = 0; i < enemyBoard.length; i++){
            bar += "+---";
        }
        bar += "+\n";

        out += bar;
        for(int j = 0; j < enemyShots.length; j++){
            if(j < 10) out += j + "  |";
            else out += j + " |";
            for(int i = 0; i < enemyShots[j].length; i++){
                String shot = enemyShots[j][i];
                if(shot.equals("")) out += "   |";
                else {
                    if(shot.equals("H")) out += "[H]|";
                    else out += " M |";
                }
            }
            out += "\n" + bar;
        }

        //Enemy's board
        out += "\nEnemy Ships\n   ";
        for(int i = 0; i < enemyBoard.length; i++){
            out += "  " + ALPHABET.charAt(i) + " ";
        }
        out += "\n";

        out += bar;
        for(int j = 0; j < enemyBoard.length; j++){
            if(j < 10) out += j + "  |";
            else out += j + " |";
            for(int i = 0; i < enemyBoard[j].length; i++){
                Ship ship = enemyBoard[j][i];
                if(ship == null) out += "   |";
                else {
                    if(ship.direction == 0 && ship.getHull(i - ship.startX) == "X") out += "[X]|";
                    else if(ship.direction == 1 && ship.getHull(j - ship.startY) == "X") out += "[X]|";
                    else out += " S |";
                }
            }
            out += "\n" + bar;
        }

        return out;
    }

    public void placeShip(Ship[][] board, String id, int x, int y, int direction, int size){
        if(direction == 0){
            Ship ship = new Ship(id, x, y, direction, size);
            for(int i = 0; i < size; i++){
                board[y][x + i] = ship;
            }
        } else {
            Ship ship = new Ship(id, x, y, direction, size);
            for(int i = 0; i < size; i++){
                board[y + i][x] = ship;
            }
        }
    }

    private Ship getShip(Ship[][] board, int x, int y){
        if(board[y][x] == null) return null;
        else return board[y][x];
    }

    
    
    private void placePhase(String name, int size){
        while(true){
            System.out.print(CLEARSCREEN);  
            System.out.println(shipBoardToString());
            System.out.printf("Enter coordinates to place the size %d %s (e.g. A3): \n", size, name);
            String input = System.console().readLine().toUpperCase();

            if(input.length() != 2 || !isLetter(input.charAt(0)) || !isDigit(input.charAt(1))){
                System.out.println("Please enter a valid coordinate.");
                System.out.println("Press enter to continue...");
                System.console().readLine();
                continue;
            }

            int x = ALPHABET.indexOf(input.charAt(0));
            int y = Integer.parseInt(input.substring(1));

            if(x > playerBoard.length || y > playerBoard.length){
                System.out.println("Please enter a valid coordinate.");
                System.out.println("Press enter to continue...");
                System.console().readLine();
                continue;
            }

            if(getShip(playerBoard, x, y) != null){
                System.out.println("Please enter a valid position.");
                System.out.println("Press enter to continue...");
                System.console().readLine();
                continue;
            }

            System.out.println("Enter direction (0 = horizontal, 1 = vertical): ");
            input = System.console().readLine();

            if(!input.equals("0") && !input.equals("1")){
                System.out.println("Please enter a valid direction.");
                System.out.println("Press enter to continue...");
                System.console().readLine();
                continue;
            }

            int direction = Integer.parseInt(input);

            if(direction == 0 && x + size > playerBoard.length){
                System.out.println("Please enter a valid position.");
                System.out.println("Press enter to continue...");
                System.console().readLine();
                continue;
            } else if(direction == 1 && y + size > playerBoard.length){
                System.out.println("Please enter a valid position.");
                System.out.println("Press enter to continue...");
                System.console().readLine();
                continue;
            }

            if(direction == 0){
                boolean invalid = false;
                for(int i = x; i < x + size; i++){
                    if(getShip(playerBoard, i, y) != null){
                        System.out.println("Please enter a valid position.");
                        System.out.println("Press enter to continue...");
                        System.console().readLine();
                        invalid = true;
                    }
                }
                if(invalid) continue;
            } else {
                boolean invalid = false;
                for(int i = y; i < y + size; i++){
                    if(getShip(playerBoard, x, i) != null){
                        System.out.println("Please enter a valid position.");
                        System.out.println("Press enter to continue...");
                        System.console().readLine();
                        invalid = true;
                    }
                }
                if(invalid) continue;
            }

            placeShip(playerBoard, name, x, y, direction, size);
            playerShips++;
            break;
        }
    }
    //endregion

    //region Public Game Methods
    public void checkState(){
        if(playerShips == 0){
            gameState = GAME_LOST;
        } else if(enemyShips == 0){
            gameState = GAME_WON;
        }
    }
    
    public Ship getShip(Ship[][] board, String id){
        for(int j = 0; j < board.length; j++){
            for(int i = 0; i < board[j].length; i++){
                if(board[j][i] != null && board[j][i].id.equals(id)) return board[j][i];
            }
        }
        return null;
    }
    
    public String isDestroyed(Ship ship){
        if(ship != null && ship.destroyed) return "X";
        else return " ";
    }

    public String toString(){
        String out = "";

        //Ship List
        out += "Ship List\n";
        out +=                "+-----------------+-----------------+\n"  ;
        out +=                "|     Player      |      Enemy      |\n"  ;
        out +=                "+-----------------+-----------------+\n"  ;
        out += String.format("| [%s] Carrier     | [%s] Carrier     |\n", isDestroyed(getShip(playerBoard, "Carrier")), isDestroyed(getShip(enemyBoard, "Carrier")));
        out += String.format("| [%s] Battleship  | [%s] Battleship  |\n", isDestroyed(getShip(playerBoard, "Battleship")), isDestroyed(getShip(enemyBoard, "Battleship")));
        out += String.format("| [%s] Destroyer   | [%s] Destroyer   |\n", isDestroyed(getShip(playerBoard, "Destroyer")), isDestroyed(getShip(enemyBoard, "Destroyer")));
        out += String.format("| [%s] Submarine   | [%s] Submarine   |\n", isDestroyed(getShip(playerBoard, "Submarine")), isDestroyed(getShip(enemyBoard, "Submarine")));
        out += String.format("| [%s] Patrol Boat | [%s] Patrol Boat |\n", isDestroyed(getShip(playerBoard, "Patrol Boat")), isDestroyed(getShip(enemyBoard, "Patrol Boat")));
        out +=                "+-----------------+-----------------+\n\n";
        
        //Player's shot board
        out += "Shot Board\n   ";
        
        for(int i = 0; i < playerBoard.length; i++){
            out += "  " + ALPHABET.charAt(i) + " ";
        }
        out += "\n";

        String bar = "   ";
        for(int i = 0; i < playerBoard.length; i++){
            bar += "+---";
        }
        bar += "+\n";

        out += bar;
        for(int j = 0; j < shots.length; j++){
            if(j < 10) out += j + "  |";
            else out += j + " |";
            for(int i = 0; i < shots[j].length; i++){
                String shot = shots[j][i];
                if(shot.equals("")) out += "   |";
                else {
                    if(shot.equals("H")) out += "[H]|";
                    else out += " M |";
                }
            }
            out += "\n" + bar;
        }

        //Player's board
        out += "\nYour Ships\n   ";
        for(int i = 0; i < playerBoard.length; i++){
            out += "  " + ALPHABET.charAt(i) + " ";
        }
        out += "\n";

        out += bar;
        for(int j = 0; j < playerBoard.length; j++){
            if(j < 10) out += j + "  |";
            else out += j + " |";
            for(int i = 0; i < playerBoard[j].length; i++){
                Ship ship = playerBoard[j][i];
                if(ship == null) out += "   |";
                else {
                    if(ship.direction == 0 && ship.getHull(i - ship.startX) == "X") out += "[X]|";
                    else if(ship.direction == 1 && ship.getHull(j - ship.startY) == "X") out += "[X]|";
                    else out += String.format(" %s |", ship.id.charAt(0));
                }
            }
            out += "\n" + bar;
        }

        return out;
    }

    public void startGame(Graphics2D g2){
        System.out.println(CLEARSCREEN);
        System.out.println("Welcome to Ship Bombing Game!");

        while(difficulty == 0){
            System.out.println("Select a difficulty:\n1. Easy\n2. Hard");
            String input = System.console().readLine();

            if(!input.equals("1") && !input.equals("2")){
                System.out.println("Please enter a valid difficulty.");
            } else {
                difficulty = Integer.parseInt(input);
            }
        }

        System.out.println("Press enter to continue...");
        String setDebug = System.console().readLine();
        if(setDebug.equals("debug")){
            System.out.println("Enter debug options (separated by commas):");
            String options = System.console().readLine();
            String[] debugOptions = options.split(",");
            for(String option : debugOptions){
                option = option.trim().toLowerCase();
                String value = "";
                for(int i = 0; i < option.length(); i++){
                    if(option.charAt(i) == ' ') continue;
                    value = value + option.charAt(i);
                }
                System.out.println(value);
                if(value.equals("autoplace")) autoPlace = true;
                else if(value.equals("autoplay")) autoPlay = true;
                else if(value.equals("seeenemyboards") || value.equals("seeenemyboard")) seeEnemyBoards = true;
            }
        }

        if(autoPlace){
            Random r = new Random();
            for(int i = 0; i < maxShips; i++){
                while(true){
                    int x = r.nextInt(playerBoard.length);
                    int y = r.nextInt(playerBoard.length);
                    int direction = r.nextInt(2);
                    int size = Integer.parseInt(SHIPS[i][1]);

                    if(direction == 0 && x + size > playerBoard.length - 1) continue;
                    else if(direction == 1 && y + size > playerBoard.length - 1) continue;

                    if(direction == 0){
                        boolean invalid = false;
                        for(int j = x; j < x + size; j++){
                            if(getShip(playerBoard, j, y) != null){
                                invalid = true;
                            }
                        }
                        if(invalid) continue;
                    } else {
                        boolean invalid = false;
                        for(int j = y; j < y + size; j++){
                            if(getShip(playerBoard, x, j) != null){
                                invalid = true;
                            }
                        }
                        if(invalid) continue;
                    }

                    placeShip(playerBoard, SHIPS[i][0], x, y, direction, size);
                    playerShips++;
                    break;
                }
            }
        } else {
            placePhase("Carrier", 5);
            placePhase("Battleship", 4);
            placePhase("Destroyer", 3);
            placePhase("Submarine", 3);
            placePhase("Patrol Boat", 2);
        }

        placeEnemyShips();

        gameState = PLAYING_GAME;
        started = true;
    }

    public void takeTurn(){
        System.out.println(CLEARSCREEN);
        System.out.println(this);
        if(seeEnemyBoards) System.out.println(enemyToString());

        String input = "";
        int x = 0;
        int y = 0;
        boolean isInputValid = false;

        if(autoPlay){
            input = ALPHABET.substring(autoPlayX, autoPlayX + 1) + autoPlayY;
            x = ALPHABET.indexOf(input.charAt(0));
            y = Integer.parseInt(input.substring(1));
            isInputValid = true;
            autoPlayX++;
            if(autoPlayX > enemyBoard.length - 1){
                autoPlayX = 0;
                autoPlayY++;
            }
        }
        
        while(!isInputValid){
            System.out.println("Enter coordinates to bomb (e.g. A3): ");
            input = System.console().readLine().toUpperCase();

            if(input.length() != 2 || !isLetter(input.charAt(0)) || !isDigit(input.charAt(1))){
                System.out.println("Please enter a valid coordinate.");
                continue;
            }

            x = ALPHABET.indexOf(input.charAt(0));
            y = Integer.parseInt(input.substring(1));

            if(x > playerBoard.length - 1 || y > playerBoard.length - 1){
                System.out.println("Please enter a valid coordinate.");
                continue;
            }

            if(!shots[y][x].equals("")){
                System.out.println("You have already shot at this coordinate.");
                continue;
            }
            
            isInputValid = true;
        }

        System.out.println("Aiming at " + input + "...");
        bombEnemy(x, y);
        System.out.println("Press enter to continue...");
        System.console().readLine();

        System.out.println(CLEARSCREEN);
        System.out.println(this);
        if(seeEnemyBoards) System.out.println(enemyToString());
        checkState();
        if(gameState != PLAYING_GAME) return;

        //Enemy turn
        System.out.println("The enemy is aiming!");
        bombPlayer(difficulty);
        System.out.println("Press enter to continue...");
        System.console().readLine();
        checkState();
    }
    
    public void bombPlayer(int x, int y){
        if(playerBoard[y][x] != null){
            enemyShots[y][x] = "H";
            Ship ship = playerBoard[y][x];
            System.out.printf("Your %s got Hit!\n", ship.id);
            messages.add(String.format("Your %s got Hit!\n", ship.id));
            if(ship.direction == 0){
                ship.bomb(x - ship.startX);
            } else {
                ship.bomb(y - ship.startY);
            }
            if(ship.destroyed){
                System.out.printf("The enemy sunk your %s!\n", ship.id);
                messages.add(String.format("Your %s got Hit!\n", ship.id));
                playerShips--;
            }
        } else {
            System.out.println("The enemy missed!");
            messages.add("The enemy missed!");
            enemyShots[y][x] = "M";
        }
    }
    
    public void bombEnemy(int x, int y){
        if(enemyBoard[y][x] != null){
            System.out.println("Hit!");
            messages.add("Hit!");
            shots[y][x] = "H";
            Ship ship = enemyBoard[y][x];
            if(ship.direction == 0){
                ship.bomb(x - ship.startX);
            } else {
                ship.bomb(y - ship.startY);
            }
            if(ship.destroyed){
                System.out.println("You sunk the " + ship.id + "!");
                messages.add("You sunk the " + ship.id + "!");
                enemyShips--;
            }
        } else {
            System.out.println("Missed!");
            messages.add("Missed!");
            shots[y][x] = "M";
        }
    }
    
    public void placeEnemyShips(){
        Random r = new Random();
        for(int i = 0; i < maxShips; i++){
            while(true){
                int x = r.nextInt(enemyBoard.length);
                int y = r.nextInt(enemyBoard.length);
                int direction = r.nextInt(2);
                int size = Integer.parseInt(SHIPS[i][1]);

                if(direction == 0 && x + size > enemyBoard.length - 1) continue;
                else if(direction == 1 && y + size > enemyBoard.length - 1) continue;

                if(direction == 0){
                    boolean invalid = false;
                    for(int j = x; j < x + size; j++){
                        if(getShip(enemyBoard, j, y) != null){
                            invalid = true;
                        }
                    }
                    if(invalid) continue;
                } else {
                    boolean invalid = false;
                    for(int j = y; j < y + size; j++){
                        if(getShip(enemyBoard, x, j) != null){
                            invalid = true;
                        }
                    }
                    if(invalid) continue;
                }

                placeShip(enemyBoard, SHIPS[i][0], x, y, direction, size);
                enemyShips++;
                break;
            }
        }
    }
    
    public void bombPlayer(int difficulty){
        Random r = new Random();
        
        if(difficulty == 1){
            while(true){
                int x = r.nextInt(playerBoard.length);
                int y = r.nextInt(playerBoard[0].length);

                if(!enemyShots[y][x].equals("")){
                    continue;
                }
                
                bombPlayer(x, y);
                break;
            }
        } else if(difficulty == 2){        
            
            while(!enemyLastShotHit){
                int x = r.nextInt(playerBoard.length);
                int y = r.nextInt(playerBoard[0].length);

                if(!enemyShots[y][x].equals("")){
                    continue;
                }

                if(playerBoard[y][x] != null){
                    enemyShots[y][x] = "H";
                    enemyLastShotHit = true;
                    enemyCurrentShotX = x;
                    enemyCurrentShotY = y;
                    enemyLastShotX = x;
                    enemyLastShotY = y;
                    enemyLastShotDirection = r.nextInt(4);

                    Ship ship = playerBoard[y][x];
                    System.out.printf("Your %s got Hit!\n", ship.id);
                    messages.add(String.format("Your %s got Hit!\n", ship.id));
                    if(ship.direction == 0){
                        ship.bomb(x - ship.startX);
                    } else {
                        ship.bomb(y - ship.startY);
                    }
                    if(ship.destroyed){
                        System.out.printf("The enemy sunk your %s!\n", ship.id);
                        messages.add(String.format("The enemy sunk your %s!\n", ship.id));
                        playerShips--;
                    }
                } else {
                    System.out.println("The enemy missed!");
                    messages.add("The enemy missed!");
                    enemyShots[y][x] = "M";
                }
                return;
            }

            if(enemyLastShotHit){
                int switches = 0;
                while(true){
                    if(switches > 3){
                        enemyLastShotDirection++;
                        if(enemyLastShotDirection > 3) enemyLastShotDirection = 0;
                    }
                    if(switches > 9){
                        enemyLastShotHit = false;
                        break;
                    }
                    
                    if(enemyLastShotDirection == 0){
                        if(enemyCurrentShotY - 1 < 0 || enemyShots[enemyCurrentShotY - 1][enemyCurrentShotX].length() > 0){
                            enemyLastShotDirection = 2;
                            switches++;
                            enemyCurrentShotY = enemyLastShotY;
                        } else {
                            int x = enemyCurrentShotX;
                            int y = enemyCurrentShotY - 1;
                            if(playerBoard[y][x] != null){
                                enemyShots[y][x] = "H";
                                enemyCurrentShotX = x;
                                enemyCurrentShotY = y;
                                Ship ship = playerBoard[y][x];
                                System.out.printf("Your %s got Hit!\n", ship.id);
                                messages.add(String.format("Your %s got Hit!\n", ship.id));
                                if(ship.direction == 0){
                                    ship.bomb(x - ship.startX);
                                } else {
                                    ship.bomb(y - ship.startY);
                                }
                                if(ship.destroyed){
                                    System.out.printf("The enemy sunk your %s!\n", ship.id);
                                    messages.add(String.format("The enemy sunk your %s!\n", ship.id));
                                    playerShips--;
                                    enemyLastShotHit = false;
                                }
                            } else {
                                System.out.println("The enemy missed!");
                                messages.add("The enemy missed!");
                                enemyShots[y][x] = "M";
                                enemyLastShotDirection = 1;
                            }
                            return;
                        }
                    }
                    if(enemyLastShotDirection == 1){
                        if(enemyCurrentShotX + 1 >= playerBoard.length || enemyShots[enemyCurrentShotY][enemyCurrentShotX + 1].length() > 0){
                            enemyLastShotDirection = 3;
                            switches++;
                            enemyCurrentShotX = enemyLastShotX;
                        } else {
                            int x = enemyCurrentShotX + 1;
                            int y = enemyCurrentShotY;
                            if(playerBoard[y][x] != null){
                                enemyShots[y][x] = "H";
                                enemyCurrentShotX = x;
                                enemyCurrentShotY = y;
                                Ship ship = playerBoard[y][x];
                                System.out.printf("Your %s got Hit!\n", ship.id);
                                messages.add(String.format("Your %s got Hit!\n", ship.id));
                                if(ship.direction == 0){
                                    ship.bomb(x - ship.startX);
                                } else {
                                    ship.bomb(y - ship.startY);
                                }
                                if(ship.destroyed){
                                    System.out.printf("The enemy sunk your %s!\n", ship.id);
                                    messages.add(String.format("The enemy sunk your %s!\n", ship.id));
                                    playerShips--;
                                    enemyLastShotHit = false;
                                }
                            } else {
                                System.out.println("The enemy missed!");
                                messages.add("The enemy missed!");
                                enemyShots[y][x] = "M";
                                enemyLastShotDirection = 2;
                            }
                            return;
                        }
                    }
                    if(enemyLastShotDirection == 2){
                        if(enemyCurrentShotY + 1 >= playerBoard[0].length || enemyShots[enemyCurrentShotY + 1][enemyCurrentShotX].length() > 0){
                            enemyLastShotDirection = 0;
                            switches++;
                            enemyCurrentShotY = enemyLastShotY;
                        } else {
                            int x = enemyCurrentShotX;
                            int y = enemyCurrentShotY + 1;
                            if(playerBoard[y][x] != null){
                                enemyShots[y][x] = "H";
                                enemyCurrentShotX = x;
                                enemyCurrentShotY = y;
                                Ship ship = playerBoard[y][x];
                                System.out.printf("Your %s got Hit!\n", ship.id);
                                messages.add(String.format("Your %s got Hit!\n", ship.id));
                                if(ship.direction == 0){
                                    ship.bomb(x - ship.startX);
                                } else {
                                    ship.bomb(y - ship.startY);
                                }
                                if(ship.destroyed){
                                    System.out.printf("The enemy sunk your %s!\n", ship.id);
                                    messages.add(String.format("The enemy sunk your %s!\n", ship.id));
                                    playerShips--;
                                    enemyLastShotHit = false;
                                }
                            } else {
                                System.out.println("The enemy missed!");
                                messages.add("The enemy missed!");
                                enemyShots[y][x] = "M";
                                enemyLastShotDirection = 3;
                            }
                            return;
                        }
                    }
                    if(enemyLastShotDirection == 3){
                        if(enemyCurrentShotX - 1 < 0 || enemyShots[enemyCurrentShotY][enemyCurrentShotX - 1].length() > 0){
                            enemyLastShotDirection = 1;
                            switches++;
                            enemyCurrentShotX = enemyLastShotX;
                        } else {
                            int x = enemyCurrentShotX - 1;
                            int y = enemyCurrentShotY;
                            if(playerBoard[y][x] != null){
                                enemyShots[y][x] = "H";
                                enemyCurrentShotX = x;
                                enemyCurrentShotY = y;
                                Ship ship = playerBoard[y][x];
                                System.out.printf("Your %s got Hit!\n", ship.id);
                                messages.add(String.format("Your %s got Hit!\n", ship.id));
                                if(ship.direction == 0){
                                    ship.bomb(x - ship.startX);
                                } else {
                                    ship.bomb(y - ship.startY);
                                }
                                if(ship.destroyed){
                                    System.out.printf("The enemy sunk your %s!\n", ship.id);
                                    messages.add(String.format("The enemy sunk your %s!\n", ship.id));
                                    playerShips--;
                                    enemyLastShotHit = false;
                                }
                            } else {
                                System.out.println("The enemy missed!");
                                messages.add("The enemy missed!");
                                enemyShots[y][x] = "M";
                                enemyLastShotDirection = 0;
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
    
    public void resetGame(int gameStateToResetTo){
        if(initialBoardSize > 26) initialBoardSize = 26;
        else if (initialBoardSize < 1) initialBoardSize = 1;
        playerBoard = new Ship[initialBoardSize][initialBoardSize];
        enemyBoard = new Ship[initialBoardSize][initialBoardSize];

        shots = new String[initialBoardSize][initialBoardSize];
        for(int j = 0; j < shots.length; j++){
            for(int i = 0; i < shots[j].length; i++){
                shots[j][i] = "";
            }
        }

        enemyShots = new String[initialBoardSize][initialBoardSize];
        for(int j = 0; j < enemyShots.length; j++){
            for(int i = 0; i < enemyShots[j].length; i++){
                enemyShots[j][i] = "";
            }
        }
        playerShips = 0;
        enemyShips = 0;
        started = false;
        gameState = gameStateToResetTo;
    }
    //endregion

    //region Game UI
    public void drawShips(Graphics2D g2){
        int boardWidth = playerBoard.length * shipSquareSize;
        int boardHeight = playerBoard.length * shipSquareSize;
        
        g2.setColor(Color.BLUE);
        g2.fillRect(shipBoardStartX, shipBoardStartY, boardWidth, boardHeight);

        g2.setColor(Color.GRAY);
        for(int j = 0; j < playerBoard.length; j++){
            for(int i = 0; i < playerBoard[j].length; i++){
                Ship ship = playerBoard[j][i];
                if(ship != null){
                    g2.setColor(Color.GRAY);
                    g2.fillRect(shipBoardStartX + i * shipSquareSize, shipBoardStartY + j * shipSquareSize, shipSquareSize, shipSquareSize);
                    
                    if(enemyShots[j][i].equals("H")){
                        g2.setColor(Color.RED);
                        g2.fillRect(shipBoardStartX + i * shipSquareSize + shotSquareOffset, shipBoardStartY + j * shipSquareSize + shotSquareOffset, shotSquareSize, shotSquareSize);
                    } else if(enemyShots[j][i].equals("M")){
                        g2.setColor(Color.WHITE);
                        g2.fillRect(shipBoardStartX + i * shipSquareSize + shotSquareOffset, shipBoardStartY + j * shipSquareSize + shotSquareOffset, shotSquareSize, shotSquareSize);
                    }

                    g2.setFont(shipFont);
                    g2.setColor(Color.BLACK);
                    g2.drawString(ship.id.substring(0, 1), shipBoardStartX + i * shipSquareSize + shipTextOffsetX, shipBoardStartY + j * shipSquareSize + shipTextOffsetY);
                }
            }
        }

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(5));
        for(int i = 0; i < playerBoard.length + 1; i++){
            g2.drawLine(shipBoardStartX + i * shipSquareSize, shipBoardStartY, shipBoardStartX + i * shipSquareSize, shipBoardStartY + boardHeight);
            g2.drawLine(shipBoardStartX, shipBoardStartY + i * shipSquareSize, shipBoardStartX + boardWidth, shipBoardStartY + i * shipSquareSize);
        }
    }

    public void drawPlacingShip(Graphics2D g2, int mouseGridPosX, int mouseGridPosY, int placingShipSize, int placingDirection){
        if(mouseGridPosX != -1 && mouseGridPosY != -1){
            if(placingDirection == 0){
                int newPlacingShipSize = 0;
                if(mouseGridPosX + placingShipSize > 7){
                    g2.setColor(new Color(255, 0, 0, 100));
                    newPlacingShipSize = shipSquareSize * (7 - mouseGridPosX);
                } else {
                    g2.setColor(new Color(0, 0, 0, 100));
                    newPlacingShipSize = shipSquareSize * placingShipSize;
                }
                g2.fillRect(mouseGridPosX * shipSquareSize + shipBoardStartX, mouseGridPosY * shipSquareSize + shipBoardStartY, newPlacingShipSize, shipSquareSize);
            }
            else if(placingDirection == 1){
                int newPlacingShipSize = 0;
                if(mouseGridPosY + placingShipSize > 7){
                    g2.setColor(new Color(255, 0, 0, 100));
                    newPlacingShipSize = shipSquareSize * (7 - mouseGridPosY);
                } else {
                    g2.setColor(new Color(0, 0, 0, 100));
                    newPlacingShipSize = shipSquareSize * placingShipSize;
                }
                g2.fillRect(mouseGridPosX * shipSquareSize + shipBoardStartX, mouseGridPosY * shipSquareSize + shipBoardStartY, shipSquareSize, newPlacingShipSize);
            }
        }
    }
    
    public void drawShotBoard(Graphics2D g2){
        int boardWidth = shots.length * shipSquareSize;
        int boardHeight = shots.length * shipSquareSize;
        
        g2.setColor(Color.BLUE);
        g2.fillRect(shotBoardStartX, shotBoardStartY, boardWidth, boardHeight);

        g2.setColor(Color.GRAY);
        for(int j = 0; j < shots.length; j++){
            for(int i = 0; i < shots[j].length; i++){
                String shot = shots[j][i];
                if(shot.equals("H")){
                    g2.setColor(Color.RED);
                    g2.fillRect(shotBoardStartX + i * shipSquareSize + shotSquareOffset, shotBoardStartY + j * shipSquareSize + shotSquareOffset, shotSquareSize, shotSquareSize);
                    g2.setFont(shipFont);
                    g2.setColor(Color.BLACK);
                    g2.drawString("H", shotBoardStartX + i * shipSquareSize + shipTextOffsetX, shotBoardStartY + j * shipSquareSize + shipTextOffsetY);
                } else if(shot.equals("M")){
                    g2.setColor(Color.WHITE);
                    g2.fillRect(shotBoardStartX + i * shipSquareSize + shotSquareOffset, shotBoardStartY + j * shipSquareSize + shotSquareOffset, shotSquareSize, shotSquareSize);
                    g2.setFont(shipFont);
                    g2.setColor(Color.BLACK);
                    g2.drawString("M", shotBoardStartX + i * shipSquareSize + shipTextOffsetX, shotBoardStartY + j * shipSquareSize + shipTextOffsetY);
                }
            }
        }

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(5));
        for(int i = 0; i < shots.length + 1; i++){
            g2.drawLine(shotBoardStartX + i * shipSquareSize, shotBoardStartY, shotBoardStartX + i * shipSquareSize, shotBoardStartY + boardHeight);
            g2.drawLine(shotBoardStartX, shotBoardStartY + i * shipSquareSize, shotBoardStartX + boardWidth, shotBoardStartY + i * shipSquareSize);
        }
    }
    //endregion

    // public static void main(String[] args){
    //     while(true){
    //         ShipBombingGame game = new ShipBombingGame(7);
    //         game.startGame(null);
    //         while(game.gameState == game.PLAYING_GAME){
    //             game.takeTurn();
    //             if(game.gameState == game.GAME_WON){
    //                 System.out.println(game.CLEARSCREEN);
    //                 System.out.println(game);
    //                 System.out.println("You won!");
    //                 game.gameState = game.GAME_OVER;
    //             } else if(game.gameState == game.GAME_LOST){
    //                 System.out.println(game.CLEARSCREEN);
    //                 System.out.println(game);
    //                 System.out.println("You lost!");
    //                 game.gameState = game.GAME_OVER;
    //             }
    //         }

    //         if(game.gameState == game.GAME_OVER){
    //             String input = "";
    //             while(input.isEmpty()){
    //                 System.out.println("Play again? (y/n)");
    //                 input = System.console().readLine().toLowerCase();
    //             }
    //             if(input.charAt(0) == 'n') break;
    //         }
    //     }
    // }
}