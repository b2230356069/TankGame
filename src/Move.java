import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import java.util.ArrayList;

public class Move {
    public static void moveTank(Scene scene, ImageView tank, Animation animation, ArrayList<ImageView> walls) {

        scene.setOnKeyPressed(event -> {

            KeyCode key = event.getCode();

            double dx = 0;
            double dy = 0;

            if (key == KeyCode.UP) {
                tank.setRotate(270);
                dy = -5;
            } else if (key == KeyCode.DOWN) {
                tank.setRotate(90);
                dy = 5;
            } else if (key == KeyCode.LEFT) {
                tank.setRotate(180);
                dx = -5;
            } else if (key == KeyCode.RIGHT) {
                tank.setRotate(0);
                dx = 5;
            } else {
                animation.stop();
            }

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

            tank.setX(tank.getX() - dx);
            tank.setY(tank.getY() - dy);

            if (!blocked) {
                tank.setX(tank.getX() + dx);
                tank.setY(tank.getY() + dy);
                animation.start();
            } else {
                animation.stop();
            }
        });

        scene.setOnKeyReleased(event -> {
            animation.stop();
        });
    }
}
