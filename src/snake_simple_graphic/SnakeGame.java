package snake_simple_graphic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JFrame {
    private static final int TILE_SIZE = 30;
    private static final int GRID_SIZE = 30;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Direction direction;
    private ArrayList<Point> snake;
    private Point food;
    private int score;
    private boolean paused;
    boolean gameStarted;
    private Image bufferImage;
    private Graphics bufferGraphics;
    private JLayeredPane layeredPane;
    private PauseScreen pauseScreen;
    private StartScreen startScreen;

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
        gameStarted = false;

        layeredPane = new JLayeredPane();
        setContentPane(layeredPane);

        startScreen = new StartScreen(this);
        layeredPane.add(startScreen, JLayeredPane.POPUP_LAYER);
        startScreen.setBounds(0, 0, getWidth(), getHeight());
        startScreen.setVisible(true);

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
        if (gameStarted && !paused) {
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

    private void showPauseScreen() {
        pauseScreen.setBounds(0, 0, getWidth(), getHeight());
        pauseScreen.setVisible(true);
        togglePauseScreen();
        layeredPane.moveToFront(pauseScreen);
        repaint();
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
        if (!gameStarted) {
            // Start the game on space key press
            if (keyCode == KeyEvent.VK_SPACE) {
                gameStarted = true;
                startScreen.setVisible(false);
                return;
            }
        }

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
        if (paused) {
            showPauseScreen();
        } else {
            pauseScreen.setVisible(false);
            repaint();
        }
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

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if ((i + j) % 2 == 0) {
                    offScreenBuffer.setColor(new Color(200, 200, 200));
                } else {
                    offScreenBuffer.setColor(new Color(150, 150, 150));
                }

                offScreenBuffer.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        offScreenBuffer.setColor(new Color(50, 200, 50));
        for (Point point : snake) {
            offScreenBuffer.fillRect(point.x * TILE_SIZE, point.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        offScreenBuffer.setColor(new Color(200, 50, 50));
        offScreenBuffer.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        offScreenBuffer.setColor(Color.BLACK);
        Font font = new Font("Serif", Font.PLAIN, 15);
        offScreenBuffer.setFont(font);
        offScreenBuffer.drawString("Score: " + score, 10, 50);

        g.drawImage(bufferImage, 0, 0, this);

        offScreenBuffer.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame().setVisible(true));
    }

    public void startNewGame() {
        snake.clear();
        snake.add(new Point(5, 5));
        spawnFood();
        score = 0;
        paused = false;
        gameStarted = false;
        startScreen.setVisible(true);
        repaint();
    }
}

class PauseScreen extends JPanel {
    private final SnakeGame game;

    public PauseScreen(SnakeGame game) {
        this.game = game;
        setLayout(new BorderLayout());

        JButton resumeButton = new JButton("Resume");
        resumeButton.setPreferredSize(new Dimension(150, 50));
        resumeButton.addActionListener(e -> {
            game.togglePauseScreen();
            setVisible(false);
        });

        JButton newGameButton = new JButton("New Game");
        newGameButton.setPreferredSize(new Dimension(150, 50));
        newGameButton.addActionListener(e -> {
            game.togglePauseScreen();
            setVisible(false);
            game.startNewGame();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resumeButton);
        buttonPanel.add(newGameButton);
        buttonPanel.setOpaque(true);

        add(buttonPanel, BorderLayout.CENTER);

        setBackground(new Color(169, 169, 169, 150));
    }
}

class StartScreen extends JPanel {
    private final SnakeGame game;

    public StartScreen(SnakeGame game) {
        this.game = game;
        setLayout(new BorderLayout());

        JButton startButton = new JButton("Start Game");
        startButton.setPreferredSize(new Dimension(150, 50));
        startButton.addActionListener(e -> {
            game.startNewGame();
            setVisible(false);
            game.gameStarted = true;
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.setOpaque(true);

        add(buttonPanel, BorderLayout.CENTER);

        setBackground(new Color(169, 169, 169, 150));
    }
}
