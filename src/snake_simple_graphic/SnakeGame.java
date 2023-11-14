package snake_simple_graphic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JFrame {
    private static final int TILE_SIZE = 30;
    private static final int GRID_SIZE = 20;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Direction direction;
    private ArrayList<Point> snake;
    private Point food;
    private int score;
    private boolean paused;
    private Image bufferImage;
    private Graphics bufferGraphics;
    private JLayeredPane layeredPane;
    private PauseScreen pauseScreen;

    public SnakeGame() {
        setTitle("Snake Game");
        setSize(GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        direction = Direction.RIGHT;
        snake = new ArrayList<>();
        snake.add(new Point(5, 5)); // Initial snake position

        spawnFood();

        score = 0;
        paused = false;

        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);

        pauseScreen = new PauseScreen(this);
        layeredPane.add(pauseScreen, JLayeredPane.POPUP_LAYER);
        pauseScreen.setBounds(0, 0, getWidth(), getHeight());
        pauseScreen.setVisible(false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }
        });

        setFocusable(true);

        Timer timer = new Timer(100, e -> gameLoop());
        timer.start();
    }

    private void gameLoop() {
        if (!paused) {
            moveSnake();
            checkCollisions();
            repaint();
        }
    }

    private void moveSnake() {
        Point head = snake.get(0);
        Point newHead;

        switch (direction) {
            case UP:
                newHead = new Point(head.x, (head.y - 1 + GRID_SIZE) % GRID_SIZE);
                break;
            case DOWN:
                newHead = new Point(head.x, (head.y + 1) % GRID_SIZE);
                break;
            case LEFT:
                newHead = new Point((head.x - 1 + GRID_SIZE) % GRID_SIZE, head.y);
                break;
            case RIGHT:
                newHead = new Point((head.x + 1) % GRID_SIZE, head.y);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            spawnFood();
            score++;
        } else {
            snake.remove(snake.size() - 1);
        }
    }
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void showPauseScreen() {
        pauseScreen.setBounds(0, 0, getWidth(), getHeight());
        pauseScreen.setVisible(true);
        setPaused(true);
        layeredPane.moveToFront(pauseScreen); // Move the pause screen to the front
        repaint(); // Redraw the game after setting the pause screen visible
    }

    private void checkCollisions() {
        Point head = snake.get(0);

        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                gameOver();
                return;
            }
        }

        if (head.x < 0 || head.x >= GRID_SIZE || head.y < 0 || head.y >= GRID_SIZE) {
            gameOver();
        }
    }

    private void spawnFood() {
        Random random = new Random();
        int x, y;

        do {
            x = random.nextInt(GRID_SIZE);
            y = random.nextInt(GRID_SIZE);
        } while (isFoodOnSnake(x, y));

        food = new Point(x, y);
    }

    private boolean isFoodOnSnake(int x, int y) {
        for (Point point : snake) {
            if (point.x == x && point.y == y) {
                return true;
            }
        }
        return false;
    }

    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:
                if (direction != Direction.DOWN) {
                    direction = Direction.UP;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (direction != Direction.UP) {
                    direction = Direction.DOWN;
                }
                break;
            case KeyEvent.VK_LEFT:
                if (direction != Direction.RIGHT) {
                    direction = Direction.LEFT;
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (direction != Direction.LEFT) {
                    direction = Direction.RIGHT;
                }
                break;
            case KeyEvent.VK_SPACE:
                togglePauseScreen();
                break;
        }
    }

    void togglePauseScreen() {
        paused = !paused;
        pauseScreen.setVisible(paused);
        repaint(); // Redraw the game after showing/hiding the pause screen
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    @Override
    public void paint(Graphics g) {
        if (bufferImage == null) {
            bufferImage = createImage(getWidth(), getHeight());
            bufferGraphics = bufferImage.getGraphics();
        }

        Graphics offScreenBuffer = bufferImage.getGraphics();
        offScreenBuffer.clearRect(0, 0, getWidth(), getHeight());

        // Draw chessboard pattern with less intense colors
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                // Alternate the color of the squares
                if ((i + j) % 2 == 0) {
                    offScreenBuffer.setColor(new Color(200, 200, 200)); // Light gray
                } else {
                    offScreenBuffer.setColor(new Color(150, 150, 150)); // Dark gray
                }

                offScreenBuffer.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        offScreenBuffer.setColor(new Color(50, 200, 50)); // Less intense green
        for (Point point : snake) {
            offScreenBuffer.fillRect(point.x * TILE_SIZE, point.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        offScreenBuffer.setColor(new Color(200, 50, 50)); // Less intense red
        offScreenBuffer.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Display the score on the screen
        offScreenBuffer.setColor(Color.BLACK);
        Font font = new Font("Serif", Font.PLAIN, 15);
        offScreenBuffer.setFont(font);
        offScreenBuffer.drawString("Score: " + score, 10, 50);
        // Draw the off-screen buffer to the screen
        g.drawImage(bufferImage, 0, 0, this);

        // Dispose of the off-screen buffer to release resources
        offScreenBuffer.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame().setVisible(true));
    }
    public void startNewGame() {
        snake.clear();
        snake.add(new Point(5, 5)); // Initial snake position
        spawnFood();
        score = 0;
        paused = false;
        repaint();
    }
}

class PauseScreen extends JPanel {
    private final SnakeGame game;

    public PauseScreen(SnakeGame game) {
        this.game = game;
        setLayout(new BorderLayout());

        JButton resumeButton = new JButton("Resume");
        resumeButton.setPreferredSize(new Dimension(100, 50)); // Set the preferred size
        resumeButton.addActionListener(e -> {
            game.setPaused(false);
            setVisible(false);
        });

        JButton newGameButton = new JButton("New Game");
        newGameButton.setPreferredSize(new Dimension(100, 50)); // Set the preferred size
        newGameButton.addActionListener(e -> {
            game.setPaused(false);
            setVisible(false);
            game.startNewGame();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resumeButton);
        buttonPanel.add(newGameButton);
        buttonPanel.setOpaque(false); // Make the panel transparent

        add(buttonPanel, BorderLayout.CENTER);

        // Set background color with alpha for transparency
        setBackground(new Color(0, 0, 0, 100)); // Adjust the alpha value as needed
    }
}



