import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Random;

public class Enemy {

    private ImageView enemyTankView;
    private Image enemyTankImage;
    private double x, y;
    private double dx, dy;
    private double speed = 1;
    private Random random = new Random();
    private Pane pane;
    private ArrayList<ImageView> walls;

    public Enemy(Pane pane, double x, double y, ArrayList<ImageView> walls) {
        this.pane = pane;
        this.x = x;
        this.y = y;
        this.walls = walls;
        enemyTankImage = new Image(getClass().getResource("/whiteTank1.png").toExternalForm());
        enemyTankView = new ImageView(enemyTankImage);

        enemyTankView.setFitWidth(25);
        enemyTankView.setFitHeight(25);
        enemyTankView.setX(x);
        enemyTankView.setY(y);

        pane.getChildren().add(enemyTankView);
        setRandomDirection();
        startMoving();
    }

    private void setRandomDirection() {
        int direction = random.nextInt(4);
        switch (direction) {
            case 0 -> { dx = 0; dy = -1; enemyTankView.setRotate(270); }
            case 1 -> { dx = 0; dy = 1; enemyTankView.setRotate(90); }
            case 2 -> { dx = -1; dy = 0; enemyTankView.setRotate(180); }
            case 3 -> { dx = 1; dy = 0; enemyTankView.setRotate(0); }
        }
    }

    private void startMoving() {
        AnimationTimer timer = new AnimationTimer() {

            private int directionChangeCounter = 0;

            public void handle(long now) {
                double nextX = x + dx * speed;
                double nextY = y + dy * speed;

                enemyTankView.setX(nextX);
                enemyTankView.setY(nextY);

                boolean blocked = false;
                for (ImageView wall : walls) {
                    if (enemyTankView.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                        blocked = true;
                        break;
                    }
                }

                if (blocked) {
                    enemyTankView.setX(x);
                    enemyTankView.setY(y);
                    setRandomDirection();
                } else if (nextX < 0 || nextX > pane.getWidth() - enemyTankView.getFitWidth()
                        || nextY < 0 || nextY > pane.getHeight() - enemyTankView.getFitHeight()) {
                    enemyTankView.setX(x);
                    enemyTankView.setY(y);
                    setRandomDirection();
                } else {
                    x = nextX;
                    y = nextY;
                }

                directionChangeCounter++;
                if (directionChangeCounter % 300 == 0) {
                    setRandomDirection();
                }
            }
        };
        timer.start();
    }
}


