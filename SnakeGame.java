import java.awt.*; // For GUI components
import java.awt.event.*; // For event handling
import javax.swing.*; // For Swing components like JPanel and JButton
import java.util.ArrayList; // For the snake's body representation
import java.util.Random; // For food placement

// Main class for the Snake Game
public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    // Inner class to represent a tile in the game grid (snake parts, food, etc.)
    private class Tile {
        int x, y; // Coordinates of the tile

        Tile(int x, int y) { // Constructor
            this.x = x;
            this.y = y;
        }
    }

    // Game configuration variables
    int boardWidth, boardHeight, tileSize = 25; // Board dimensions and tile size
    Tile snakeHead; // Represents the head of the snake
    ArrayList<Tile> snakeBody; // List to store snake body parts
    Tile food; // Represents the food
    Random random; // Random generator for food placement
    int velocityX, velocityY; // Velocity of the snake (movement direction)
    Timer gameLoop; // Timer to control game updates
    boolean gameOver = false; // Game over state
    boolean gamePaused = false; // Pause state
    int score = 0; // Current game score
    int highestScore = 0; // Track the highest score achieved

    // Countdown timer variables
    int timeLeft = 60; // Time left in seconds
    Timer countdownTimer; // Timer for counting down

    // Game colors for customization
    Color backgroundColor = new Color(30, 30, 30); // Dark gray background
    Color snakeColor = new Color(0, 255, 0); // Green snake head
    Color bodyColor = new Color(255, 255, 0); // Yellow snake body
    Color foodColor = new Color(255, 0, 0); // Red food

    // Buttons for pause and restart functionality
    JButton pauseButton, restartButton;

    // Constructor to initialize the game
    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth; // Set board width
        this.boardHeight = boardHeight; // Set board height
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight)); // Set panel size
        setBackground(backgroundColor); // Set background color
        addKeyListener(this); // Add key listener for keyboard controls
        setFocusable(true); // Make the panel focusable to receive key inputs

        // Initialize snake head and body
        snakeHead = new Tile(5, 5); // Start snake at (5, 5)
        snakeBody = new ArrayList<>(); // Initialize empty body
        food = new Tile(10, 10); // Initial food position
        random = new Random(); // Random generator instance
        placeFood(); // Place initial food

        // Initial snake velocity (moving right)
        velocityX = 1;
        velocityY = 0;

        // Initialize game loop timer (100ms per update)
        gameLoop = new Timer(100, this);
        gameLoop.start(); // Start the game loop

        // Initialize countdown timer (1 second interval)
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameOver && !gamePaused) { // Decrease time only when the game is active
                    timeLeft--; // Reduce timer by 1 second
                    if (timeLeft <= 0) { // End game if time runs out
                        gameOver = true;
                    }
                }
            }
        });
        countdownTimer.start(); // Start the countdown timer

        // Create pause and restart buttons
        pauseButton = new JButton("Pause");
        restartButton = new JButton("Restart");

        // Set button positions and sizes
        pauseButton.setBounds(boardWidth - 100, 10, 80, 30);
        restartButton.setBounds(boardWidth - 100, 50, 80, 30);

        // Set button colors
        pauseButton.setBackground(new Color(0, 255, 255)); // Cyan
        restartButton.setBackground(new Color(255, 255, 0)); // Yellow

        // Add action listener for pause button
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause(); // Toggle pause state
            }
        });

        // Add action listener for restart button
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restartGame(); // Restart the game
            }
        });

        // Add buttons to the panel
        add(pauseButton);
        add(restartButton);
    }

    // Override the paint method to draw game elements
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Clear the panel
        draw(g); // Draw the game
    }

    // Draw all game elements
    public void draw(Graphics g) {
        // Draw grid lines
        g.setColor(new Color(50, 50, 50)); // Light gray grid lines
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight); // Vertical lines
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize); // Horizontal lines
        }

        // Draw the food
        g.setColor(foodColor);
        g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);

        // Draw the snake head
        g.setColor(snakeColor);
        g.fillRoundRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, 10, 10);

        // Draw the snake body
        for (Tile snakePart : snakeBody) {
            g.setColor(bodyColor);
            g.fillRoundRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, 10, 10);
        }

        // Draw score and timer
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.WHITE);
        if (gameOver) { // Display game over message
            g.setColor(Color.RED);
            String gameOverText = "Game Over! Score: " + score;
            FontMetrics fm = g.getFontMetrics();
            int x = (boardWidth - fm.stringWidth(gameOverText)) / 2;
            int y = boardHeight / 2;
            g.drawString(gameOverText, x, y);
            g.drawString("Press 'R' to Restart", x, y + 30);
        } else if (gamePaused) { // Display paused message
            g.setColor(Color.YELLOW);
            String pauseText = "Game Paused. Press 'P' to Resume";
            FontMetrics fm = g.getFontMetrics();
            int x = (boardWidth - fm.stringWidth(pauseText)) / 2;
            int y = boardHeight / 2;
            g.drawString(pauseText, x, y);
        } else { // Display current score and timer
            g.drawString("Score: " + score, 10, 30);
            g.drawString("Highest Score: " + highestScore, 10, 50);
        }

        // Display time left
        g.setColor(Color.YELLOW);
        g.drawString("Time Left: " + timeLeft + "s", boardWidth - 120, 30);
    }

    // Place food at a random location not occupied by the snake
    public void placeFood() {
        int foodX, foodY;
        do {
            foodX = random.nextInt(boardWidth / tileSize);
            foodY = random.nextInt(boardHeight / tileSize);
        } while (isFoodOnSnake(foodX, foodY)); // Ensure food doesn't overlap with snake
        food.x = foodX;
        food.y = foodY;
    }

    // Check if food overlaps with the snake
    public boolean isFoodOnSnake(int foodX, int foodY) {
        for (Tile snakePart : snakeBody) {
            if (snakePart.x == foodX && snakePart.y == foodY) {
                return true;
            }
        }
        return false;
    }

    // Move the snake and handle game logic
    public void move() {
        if (gameOver || gamePaused) return; // Stop movement if game is over or paused

        // Move the snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        // Check for collisions with the snake body
        for (Tile snakePart : snakeBody) {
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        // Check for out-of-bounds collision
        if (snakeHead.x * tileSize < 0 || snakeHead.x * tileSize >= boardWidth ||
            snakeHead.y * tileSize < 0 || snakeHead.y * tileSize >= boardHeight) {
            gameOver = true;
        }

        // Check if the snake eats the food
        if (collision(snakeHead, food)) {
            if (snakeBody.size() > 0) {
                snakeBody.add(new Tile(snakeBody.get(snakeBody.size() - 1).x, snakeBody.get(snakeBody.size() - 1).y));
            } else {
                snakeBody.add(new Tile(snakeHead.x, snakeHead.y));
            }
            placeFood(); // Place new food
            score++; // Increase score
            if (score > highestScore) highestScore = score; // Update highest score
        }

        // Move the snake body
        for (int i = snakeBody.size() - 1; i > 0; i--) {
            Tile snakePart = snakeBody.get(i);
            Tile prevSnakePart = snakeBody.get(i - 1);
            snakePart.x = prevSnakePart.x;
            snakePart.y = prevSnakePart.y;
        }

        // Move the first body part to the previous head position
        if (snakeBody.size() > 0) {
            snakeBody.set(0, new Tile(snakeHead.x, snakeHead.y));
        }
    }

    // Check if two tiles are at the same position
    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gamePaused) {
            move(); // Update game state
        }
        repaint(); // Repaint the game panel
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) { // Move up
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) { // Move down
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) { // Move left
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) { // Move right
            velocityX = 1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_P) { // Pause game
            togglePause();
        } else if (e.getKeyCode() == KeyEvent.VK_R && gameOver) { // Restart game
            restartGame();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {} // Not used

    @Override
    public void keyReleased(KeyEvent e) {} // Not used

    // Toggle the game's pause state
    public void togglePause() {
        gamePaused = !gamePaused;
    }

    // Restart the game
    public void restartGame() {
        snakeHead = new Tile(5, 5); // Reset snake head position
        snakeBody.clear(); // Clear snake body
        score = 0; // Reset score
        timeLeft = 60; // Reset timer
        velocityX = 1; // Reset velocity
        velocityY = 0;
        gameOver = false; // Reset game over state
        gamePaused = false; // Reset pause state
        placeFood(); // Place a new food
        countdownTimer.start(); // Restart countdown timer
        gameLoop.start(); // Restart game loop
    }

    // Main method to run the game
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game"); // Create the game window
        SnakeGame game = new SnakeGame(500, 500); // Create game instance
        frame.add(game); // Add the game panel to the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close app on exit
        frame.setResizable(false); // Make the window non-resizable
        frame.pack(); // Pack the frame to fit the game panel
        frame.setVisible(true); // Make the frame visible
    }
}
