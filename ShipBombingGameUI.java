import java.awt.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

import java.awt.event.*;
import java.awt.Graphics2D;

public class ShipBombingGameUI{
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Ship Bombing Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        ShipBombingGameScene panel = new ShipBombingGameScene();
        panel.setPreferredSize(new Dimension(1200, 1000));
        panel.addMouseMotionListener(panel);
        panel.addMouseListener(panel);
        panel.addKeyListener(panel);
        panel.setFocusable(true);

        int repaintClock = 30;
        Timer timer = new Timer(repaintClock, panel);
        timer.start();
    
        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }
}

class ShipBombingGameScene extends JPanel implements MouseInputListener, ActionListener, KeyListener {
    
    ShipBombingGame game = new ShipBombingGame(9);
    {
        game.gameState = game.CHOOSING_DIFFICULTY;
    }
    
    boolean playerTurn = true;
    boolean enemyTurn = false;
    boolean displayHitMessage = false;
    boolean selectEasy = true;
    boolean selectHard = false;
    boolean selectExit = false;
    
    int mouseX = 0;
	int mouseY = 0;
    int mouseShipGridPosX = 0;
    int mouseShipGridPosY = 0;
    int mouseShotGridPosX = 0;
    int mouseShotGridPosY = 0;
    int placingDirection = 0;
    int placingShipSize = Integer.parseInt(game.SHIPS[0][1]);
    int difficultyStartX;
    int difficultyWidth;
    int difficultyHeight;
    int difficultyEasyStartY;
    int difficultyHardStartY;
    int difficultyExitStartY;

    Font currentFont;
    String currentTurn;
    String hitMessage = "";
	
    //Repaint Loop
	public void actionPerformed(ActionEvent e) {
        repaint();
    }
    
    void drawMessages(Graphics2D g2){
        g2.setFont(currentFont.deriveFont(20f));
        for(int i = 0; i < game.messages.size(); i++){
            g2.drawString(game.messages.get(i), game.shotBoardStartX + 20 + game.shipSquareSize * game.initialBoardSize, game.shotBoardStartY + 20 + 50 * i);
        }
        g2.setFont(currentFont);
    }

    void drawShipLists(Graphics2D g2){
        g2.setFont(currentFont.deriveFont(25f));
        String displayString = "Enemy Ship (Size)";
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());
        int stringWidth = metrics.stringWidth(displayString);
        g2.drawString(displayString, game.shotBoardStartX - 25 - stringWidth, game.shotBoardStartY + 20);
        for(int i = 0; i < game.maxShips; i++){
            displayString = game.SHIPS[i][0];
            Ship currentShip = game.getShip(game.enemyBoard, game.SHIPS[i][0]);
            String isDestroyed = game.isDestroyed(currentShip);
            displayString += String.format("[%s]   (%d)", isDestroyed, currentShip.size);
            stringWidth = metrics.stringWidth(displayString);
            if(isDestroyed.equals("X")) g2.setColor(Color.RED);
            else g2.setColor(Color.BLACK);
            g2.drawString(displayString, game.shotBoardStartX - 30 - stringWidth, game.shotBoardStartY + 25 * (i + 1) + 20);
        }
        
        displayString = "Player Ship (Size)";
        stringWidth = metrics.stringWidth(displayString);
        g2.setColor(Color.BLACK);
        g2.drawString(displayString, game.shipBoardStartX - 30 - stringWidth, game.shipBoardStartY + 20);
        for(int i = 0; i < game.maxShips; i++){
            displayString = game.SHIPS[i][0];
            Ship currentShip = game.getShip(game.playerBoard, game.SHIPS[i][0]);
            String isDestroyed = game.isDestroyed(currentShip);
            displayString += String.format("[%s]   (%d)", isDestroyed, currentShip.size);
            stringWidth = metrics.stringWidth(displayString);
            if(isDestroyed.equals("X")) g2.setColor(Color.RED);
            else g2.setColor(Color.BLACK);
            g2.drawString(displayString, game.shipBoardStartX - 25 - stringWidth, game.shipBoardStartY + 25 * (i + 1) + 20);
        }
    }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if(!game.started && game.playerShips == game.maxShips){
            game.gameState = game.PLAYING_GAME;
            game.placeEnemyShips();
            game.started = true;
            playerTurn = true;
            enemyTurn = false;
        }
        
        if(game.gameState == game.CHOOSING_DIFFICULTY){
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.BLACK);
            g2.setFont(game.shipFont.deriveFont(35f));
            String displayString = "Welcome to Ship Bombing Game!";
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int stringWidth = metrics.stringWidth(displayString);
            int stringHeight = metrics.getHeight();
            g2.drawString(displayString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2);
            
            displayString = "Choose a difficulty:";
            stringWidth = metrics.stringWidth(displayString);
            g2.drawString(displayString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2 + 50);
            difficultyStartX = getWidth() / 2 - stringWidth / 2;
            difficultyWidth = stringWidth;
            difficultyHeight = stringHeight;
            difficultyEasyStartY = getHeight() / 2 + stringHeight / 2 + 110 - stringHeight;
            difficultyHardStartY = getHeight() / 2 + stringHeight / 2 + 160 - stringHeight;
            difficultyExitStartY = getHeight() / 2 + stringHeight / 2 + 210 - stringHeight;
            if(selectEasy){
                g2.setColor(new Color(128, 128, 128));
                g2.fillRect(difficultyStartX, difficultyEasyStartY, stringWidth, stringHeight);
            } else if(selectHard){
                g2.setColor(new Color(128, 128, 128));
                g2.fillRect(difficultyStartX, difficultyHardStartY, stringWidth, stringHeight);
            } else if(selectExit){
                g2.setColor(new Color(128, 128, 128));
                g2.fillRect(difficultyStartX, difficultyExitStartY, stringWidth, stringHeight);
            }
            
            g2.setColor(Color.BLACK);
            displayString = "Easy";
            stringWidth = metrics.stringWidth(displayString);
            g2.drawString(displayString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2 + 100);
            
            displayString = "Hard";
            stringWidth = metrics.stringWidth(displayString);
            g2.drawString(displayString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2 + 150);

            displayString = "Quit";
            stringWidth = metrics.stringWidth(displayString);
            g2.drawString(displayString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2 + 200);

        } else if(game.gameState == game.PLACING_SHIPS){
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            game.shipBoardStartX = getWidth() / 2 - game.initialBoardSize * game.shipSquareSize / 2;
            game.shipBoardStartY = 325;
            game.drawShips(g2);
            game.drawPlacingShip(g2, mouseShipGridPosX, mouseShipGridPosY, placingShipSize, placingDirection);

            g2.setColor(Color.BLACK);
            g2.setFont(game.shipFont.deriveFont(35f));
            String displayString = "Press R or click Right Mouse to rotate ship";
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int stringWidth = metrics.stringWidth(displayString);
            g2.drawString(displayString, getWidth() / 2 - stringWidth / 2, 100);
        } else if(game.gameState == game.PLAYING_GAME){
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            game.shipBoardStartX = getWidth() / 2 - game.initialBoardSize * game.shipSquareSize / 2;
            game.shipBoardStartY = 550;
            game.drawShips(g2);

            game.shotBoardStartX = getWidth() / 2 - game.initialBoardSize * game.shipSquareSize / 2;
            game.shotBoardStartY = 100;
            game.drawShotBoard(g2);

            if(mouseShotGridPosX != -1 && mouseShotGridPosY != -1){
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRect(game.shotBoardStartX + mouseShotGridPosX * game.shipSquareSize, game.shotBoardStartY + mouseShotGridPosY * game.shipSquareSize, game.shipSquareSize, game.shipSquareSize);
            }
            g2.setColor(Color.BLACK);
            if(playerTurn){
                currentTurn = "Where do we aim, Captain?";
            } else {
                currentTurn = "The enemy is aiming at us!";
            }
            g2.setFont(currentFont.deriveFont(50f));
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int stringWidth = metrics.stringWidth(currentTurn);
            g2.drawString(currentTurn, getWidth() / 2 - stringWidth / 2, 50);
            
            drawMessages(g2);
            drawShipLists(g2);

            g2.setFont(currentFont.deriveFont(20f));
            g2.setColor(Color.BLACK);
            g2.drawString("Press L to forfeit the game", 10, getHeight() - 10);
        } else if(game.gameState == game.GAME_WON){
            game.drawShips(g2);
            game.drawShotBoard(g2);
            drawShipLists(g2);
            
            g2.setColor(new Color(128, 128, 128, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(currentFont.deriveFont(50f));
            String loseString = "You won!";
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int stringWidth = metrics.stringWidth(loseString);
            int stringHeight = metrics.getHeight();
            g2.drawString(loseString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2);
            Timer timer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    game.gameState = game.GAME_OVER;
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else if(game.gameState == game.GAME_LOST){
            game.drawShips(g2);
            game.drawShotBoard(g2);
            drawShipLists(g2);
            
            g2.setColor(new Color(128, 128, 128, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setFont(currentFont.deriveFont(50f));
            String loseString = "You lost!";
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int stringWidth = metrics.stringWidth(loseString);
            int stringHeight = metrics.getHeight();
            g2.drawString(loseString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2);
            Timer timer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    game.gameState = game.GAME_OVER;
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else if(game.gameState == game.GAME_OVER){
            g2.setColor(new Color(128, 128, 128));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.setColor(Color.BLACK);
            g2.setFont(currentFont.deriveFont(35f));
            String loseString = "Game Over";
            FontMetrics metrics = g2.getFontMetrics(g2.getFont());
            int stringWidth = metrics.stringWidth(loseString);
            int stringHeight = metrics.getHeight();
            g2.drawString(loseString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2);

            loseString = "Press Space to Restart";
            stringWidth = metrics.stringWidth(loseString);
            g2.drawString(loseString, getWidth() / 2 - stringWidth / 2, getHeight() / 2 + stringHeight / 2 + 50);
        }
        currentFont = game.shipFont;
    }

    public void keyPressed(KeyEvent e) {
        // Code...
    }

    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP && game.gameState == game.CHOOSING_DIFFICULTY){
            if(selectEasy){
                selectEasy = false;
                selectExit = true;
            } else if(selectHard) {
                selectEasy = true;
                selectHard = false;
            } else {
                selectHard = true;
                selectExit = false;
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_DOWN && game.gameState == game.CHOOSING_DIFFICULTY){
            if(selectEasy){
                selectEasy = false;
                selectHard = true;
            } else if(selectHard) {
                selectExit = true;
                selectHard = false;
            } else {
                selectEasy = true;
                selectExit = false;
            }
        }
        if(e.getKeyCode() == KeyEvent.VK_ENTER && game.gameState == game.CHOOSING_DIFFICULTY){
            if(selectEasy) game.difficulty = 1;
            else if(selectHard) game.difficulty = 2;
            else if(selectExit) System.exit(0);
            System.out.println("Difficulty: " + game.difficulty);
            game.gameState = game.PLACING_SHIPS;
        }
        if(e.getKeyCode() == KeyEvent.VK_R && game.gameState == game.PLACING_SHIPS) placingDirection = 1 - placingDirection;
        if(e.getKeyCode() == KeyEvent.VK_L && game.gameState == game.PLAYING_GAME) game.gameState = game.GAME_LOST;
        if(e.getKeyCode() == KeyEvent.VK_SPACE && game.gameState == game.GAME_OVER){
            game.resetGame(game.CHOOSING_DIFFICULTY);
            repaint();
            placingShipSize = Integer.parseInt(game.SHIPS[0][1]);
        }
    }

    public void keyTyped(KeyEvent e) {
        // Code...
    }

    //region Mouse Functions
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();

        double squareSize = ((double) game.shipSquareSize);

        if((mouseX - game.shipBoardStartX) / squareSize < 0){
            mouseShipGridPosX = -1;
            mouseShipGridPosY = -1;
        } else if((mouseX - game.shipBoardStartX) / squareSize >= game.initialBoardSize){
            mouseShipGridPosX = -1;
            mouseShipGridPosY = -1;
        } else if ((mouseY - game.shipBoardStartY) / squareSize < 0){
            mouseShipGridPosX = -1;
            mouseShipGridPosY = -1;
        } else if((mouseY - game.shipBoardStartY) / squareSize >= game.initialBoardSize){
            mouseShipGridPosX = -1;
            mouseShipGridPosY = -1;
        } else {
            mouseShipGridPosX = (int) ((mouseX - game.shipBoardStartX) / squareSize);
            mouseShipGridPosY = (int) ((mouseY - game.shipBoardStartY) / squareSize);
        }

        if((mouseX - game.shotBoardStartX) / squareSize < 0){
            mouseShotGridPosX = -1;
            mouseShotGridPosY = -1;
        } else if((mouseX - game.shotBoardStartX) / squareSize >= game.initialBoardSize){
            mouseShotGridPosX = -1;
            mouseShotGridPosY = -1;
        } else if ((mouseY - game.shotBoardStartY) / squareSize < 0){
            mouseShotGridPosX = -1;
            mouseShotGridPosY = -1;
        } else if((mouseY - game.shotBoardStartY) / squareSize >= game.initialBoardSize){
            mouseShotGridPosX = -1;
            mouseShotGridPosY = -1;
        } else {
            mouseShotGridPosX = (int) ((mouseX - game.shotBoardStartX) / squareSize);
            mouseShotGridPosY = (int) ((mouseY - game.shotBoardStartY) / squareSize);
        }

        if(game.gameState == game.CHOOSING_DIFFICULTY){
            if(mouseX > difficultyStartX && mouseX < difficultyStartX + difficultyWidth){
                if(mouseY > difficultyEasyStartY && mouseY < difficultyEasyStartY + difficultyHeight){
                    selectEasy = true;
                    selectHard = false;
                } else if(mouseY > difficultyHardStartY && mouseY < difficultyHardStartY + difficultyHeight){
                    selectEasy = false;
                    selectHard = true;
                } else if(mouseY > difficultyExitStartY && mouseY < difficultyExitStartY + difficultyHeight){
                    selectEasy = false;
                    selectHard = false;
                    selectExit = true;
                }
            }
        }
        // repaint();
	}

    public void mouseDragged(MouseEvent e) {
        // Code...
	}

    public void mouseClicked(MouseEvent e) {
        // Code...
    }

    public void mousePressed(MouseEvent e) {
        if(e.getButton() == 1){
            if(game.gameState == game.CHOOSING_DIFFICULTY){
                if(selectEasy) game.difficulty = 1;
                else if(selectHard) game.difficulty = 2;
                else if(selectExit) System.exit(0);
                System.out.println("Difficulty: " + game.difficulty);
                game.gameState = game.PLACING_SHIPS;
            } else if(game.gameState == game.PLACING_SHIPS){
                if(mouseShipGridPosX != -1 && mouseShipGridPosY != -1){
                    String[] currentShip = game.SHIPS[game.playerShips];
                    if(placingDirection == 0){
                        if(mouseShipGridPosX + Integer.parseInt(currentShip[1]) <= game.initialBoardSize){
                            for(int i = mouseShipGridPosX; i < mouseShipGridPosX + Integer.parseInt(currentShip[1]); i++){
                                if(game.playerBoard[mouseShipGridPosY][i] != null){
                                    return;
                                }
                            }
                            game.placeShip(game.playerBoard, currentShip[0], mouseShipGridPosX, mouseShipGridPosY, 0, Integer.parseInt(currentShip[1]));
                            game.playerShips++;
                            placingShipSize = Integer.parseInt(game.SHIPS[game.playerShips][1]);
                        }
                    } else {
                        if(mouseShipGridPosY + Integer.parseInt(currentShip[1]) <= game.initialBoardSize){
                            for(int i = mouseShipGridPosY; i < mouseShipGridPosY + Integer.parseInt(currentShip[1]); i++){
                                if(game.playerBoard[i][mouseShipGridPosX] != null){
                                    return;
                                }
                            }
                            game.placeShip(game.playerBoard, currentShip[0], mouseShipGridPosX, mouseShipGridPosY, 1, Integer.parseInt(currentShip[1]));
                            game.playerShips++;
                            placingShipSize = Integer.parseInt(game.SHIPS[game.playerShips][1]);
                        }
                    }
                }
            }

            if(game.gameState == game.PLAYING_GAME && playerTurn){
                if(mouseShipGridPosX == -1 && mouseShotGridPosX != -1){
                    if(game.shots[mouseShotGridPosY][mouseShotGridPosX].equals("")){
                        game.bombEnemy(mouseShotGridPosX, mouseShotGridPosY);

                        Timer timer = new Timer(1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!game.messages.isEmpty()) game.messages.remove(0);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();

                        Timer timer2 = new Timer(1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!game.messages.isEmpty()) game.messages.remove(0);
                            }
                        });
                        timer2.setRepeats(false);
                        timer2.start();

                        playerTurn = false;
                        if(game.gameState == game.PLAYING_GAME) enemyTurn = true;
                    }
                }
            }

            if(enemyTurn){
                enemyTurn = false;
                
                Timer timer = new Timer(2250, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        game.bombPlayer(game.difficulty);
                        Timer timer = new Timer(1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!game.messages.isEmpty()) game.messages.remove(0);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();

                        Timer timer2 = new Timer(1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(!game.messages.isEmpty()) game.messages.remove(0);
                            }
                        });
                        timer2.setRepeats(false);
                        timer2.start();
                        game.checkState();
                        Timer timer3 = new Timer(1000, new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if(game.gameState == game.PLAYING_GAME) playerTurn = true;
                            }
                        });
                        timer3.setRepeats(false);
                        timer3.start();
                        
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        }
        if(e.getButton() == 3){
            if(game.gameState == game.PLACING_SHIPS) placingDirection = 1 - placingDirection;
        }
    }

    public void mouseReleased(MouseEvent e) {
        // Code...
    }

    public void mouseEntered(MouseEvent e) {
        // Code...
    }

    public void mouseExited(MouseEvent e) {
        // Code...
    }
    //endregion
}
