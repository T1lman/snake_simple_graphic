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
        snake.add(new Point(5, 5)); // Anfangsposition der Schlange

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

    // Die Hauptschleife des Spiels, die den Spielzustand aktualisiert und neu zeichnet
    private void gameLoop() {
        if (!paused) {
            moveSnake();
            checkCollisions();
            repaint();
        }
    }

    // Bewegt die Schlange basierend auf der aktuellen Richtung
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
                throw new IllegalStateException("Unerwarteter Wert: " + direction);
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            spawnFood();
            score++;
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    // Zeigt den Pause-Bildschirm an
    private void showPauseScreen() {
        pauseScreen.setBounds(0, 0, getWidth(), getHeight());
        pauseScreen.setVisible(true);
        togglePauseScreen();
        layeredPane.moveToFront(pauseScreen); // Pause-Bildschirm in den Vordergrund bringen
        repaint(); // Das Spiel neu zeichnen, nachdem der Pause-Bildschirm sichtbar gemacht wurde
    }

    // Überprüft auf Kollisionen, wie z.B. ob die Schlange mit sich selbst oder mit der Wand kollidiert
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

    // Platziert zufällig einen neuen Futterpunkt auf dem Spielfeld
    private void spawnFood() {
        Random random = new Random();
        int x, y;

        do {
            x = random.nextInt(GRID_SIZE);
            y = random.nextInt(GRID_SIZE);
        } while (isFoodOnSnake(x, y));

        food = new Point(x, y);
    }

    // Überprüft, ob das Futter auf der Schlange liegt
    private boolean isFoodOnSnake(int x, int y) {
        for (Point point : snake) {
            if (point.x == x && point.y == y) {
                return true;
            }
        }
        return false;
    }

    // Verarbeitet Tastenanschläge des Spielers
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

    // Zeigt oder verbirgt den Pause-Bildschirm
    void togglePauseScreen() {
        paused = !paused;
        pauseScreen.setVisible(paused);
        repaint(); // Das Spiel neu zeichnen, nachdem der Pause-Bildschirm angezeigt/ausgeblendet wurde
    }

    // Zeigt das Spiel-Ende-Fenster und beendet das Spiel
    private void gameOver() {
        JOptionPane.showMessageDialog(this, "Spiel vorbei! Dein Punktestand: " + score, "Spiel vorbei", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    // Zeichnet das Spielfeld und seine Elemente
    @Override
    public void paint(Graphics g) {
        if (bufferImage == null) {
            bufferImage = createImage(getWidth(), getHeight());
            bufferGraphics = bufferImage.getGraphics();
        }

        Graphics offScreenBuffer = bufferImage.getGraphics();
        offScreenBuffer.clearRect(0, 0, getWidth(), getHeight());

        // Schachbrettmuster mit weniger intensiven Farben zeichnen
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                // Farbe der Quadrate abwechseln
                if ((i + j) % 2 == 0) {
                    offScreenBuffer.setColor(new Color(200, 200, 200)); // Hellgrau
                } else {
                    offScreenBuffer.setColor(new Color(150, 150, 150)); // Dunkelgrau
                }

                offScreenBuffer.fillRect(i * TILE_SIZE, j * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        offScreenBuffer.setColor(new Color(50, 200, 50)); // Weniger intensives Grün
        for (Point point : snake) {
            offScreenBuffer.fillRect(point.x * TILE_SIZE, point.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        offScreenBuffer.setColor(new Color(200, 50, 50)); // Weniger intensives Rot
        offScreenBuffer.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Punktestand auf dem Bildschirm anzeigen
        offScreenBuffer.setColor(Color.BLACK);
        Font font = new Font("Serif", Font.PLAIN, 15);
        offScreenBuffer.setFont(font);
        offScreenBuffer.drawString("Punktestand: " + score, 10, 50);

        // Den Offscreen-Buffer auf den Bildschirm zeichnen
        g.drawImage(bufferImage, 0, 0, this);

        // Den Offscreen-Buffer freigeben, um Ressourcen freizugeben
        offScreenBuffer.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame().setVisible(true));
    }

    // Startet ein neues Spiel, indem der Schlangen-Array gelöscht und die Initialposition der Schlange festgelegt wird
    public void startNewGame() {
        snake.clear();
        snake.add(new Point(5, 5)); // Anfangsposition der Schlange
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

        JButton resumeButton = new JButton("Fortsetzen");
        resumeButton.setPreferredSize(new Dimension(150, 50)); // Gewünschte Größe setzen
        resumeButton.addActionListener(e -> {
            game.togglePauseScreen();
            setVisible(false);
        });

        JButton newGameButton = new JButton("Neues Spiel");
        newGameButton.setPreferredSize(new Dimension(150, 50)); // Gewünschte Größe setzen
        newGameButton.addActionListener(e -> {
            game.togglePauseScreen();
            setVisible(false);
            game.startNewGame();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(resumeButton);
        buttonPanel.add(newGameButton);
        buttonPanel.setOpaque(true); // Das Panel undurchsichtig machen

        add(buttonPanel, BorderLayout.CENTER);

        // Hintergrundfarbe mit Alpha für Transparenz setzen
        setBackground(new Color(169, 169, 169, 150)); // Alpha-Wert anpassen
    }
}
