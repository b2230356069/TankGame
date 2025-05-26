import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Moving {

    private static boolean isShooting = false;
    private static final double bulletSpeed = 3;
    private static final Image bulletImage = new Image(Moving.class.getResource("/bullet.png").toExternalForm());

    public static void moveTank(Scene scene, ImageView tank, Animation animation, ArrayList<ImageView> walls, Pane main, ArrayList<ImageView> bullets) {

        final Set<KeyCode> pressedKeys = new HashSet<>();

        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            if (event.getCode() == KeyCode.X && !isShooting) {
                shoot(main, tank, bullets, walls);
                isShooting = true;
            }
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());

            if (event.getCode() == KeyCode.X) {
                isShooting = false;
            }
        });

        new AnimationTimer() {
            public void handle(long now) {

                double dx = 0;
                double dy = 0;

                if (pressedKeys.contains(KeyCode.UP)) {
                    tank.setRotate(270);
                    dy = -1;
                } else if (pressedKeys.contains(KeyCode.DOWN)) {
                    tank.setRotate(90);
                    dy = 1;
                } else if (pressedKeys.contains(KeyCode.LEFT)) {
                    tank.setRotate(180);
                    dx = -1;
                } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                    tank.setRotate(0);
                    dx = 1;
                }

                if (dx != 0 || dy != 0) {
                    double nextX = tank.getX() + dx;
                    double nextY = tank.getY() + dy;

                    tank.setX(nextX);
                    tank.setY(nextY);

                    boolean blocked = false;
                    for (ImageView wall : walls) {
                        if (tank.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                            blocked = true;
                            break;
                        }
                    }

                    if (blocked) {
                        tank.setX(tank.getX() - dx);
                        tank.setY(tank.getY() - dy);
                        animation.stop();
                    } else {
                        animation.start();
                        TankGame2025.updateCamera(main, tank, scene);
                    }

                } else {
                    animation.stop();
                }
            }
        }.start();

    }

    private static void shoot(Pane main, ImageView tank, ArrayList<ImageView> bullets, ArrayList<ImageView> walls) {
        ImageView bullet = new ImageView(bulletImage);
        bullet.setFitWidth(10);
        bullet.setFitHeight(10);

        double tankCenterX = tank.getX() + tank.getFitWidth() / 2;
        double tankCenterY = tank.getY() + tank.getFitHeight() / 2;

        bullet.setX(tankCenterX - bullet.getFitWidth() / 2);
        bullet.setY(tankCenterY - bullet.getFitHeight() / 2);

        main.getChildren().add(bullet);
        bullets.add(bullet);

        double bulletAngle = tank.getRotate();

        new AnimationTimer() {
            public void handle(long now) {
                double dx = 0;
                double dy = 0;

                if (bulletAngle == 0) dx = bulletSpeed;
                else if (bulletAngle == 180) dx = -bulletSpeed;
                else if (bulletAngle == 90) dy = bulletSpeed;
                else if (bulletAngle == 270) dy = -bulletSpeed;

                bullet.setX(bullet.getX() + dx);
                bullet.setY(bullet.getY() + dy);

                for (ImageView wall : walls) {
                    if (bullet.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                        main.getChildren().remove(bullet);
                        bullets.remove(bullet);
                        stop();
                        return;
                    }
                }
            }
        }.start();
    }
}