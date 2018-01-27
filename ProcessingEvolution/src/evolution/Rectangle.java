package evolution;

public class Rectangle {
    public final float x1, y1, x2, y2;

    Rectangle(float tx1, float ty1, float tx2, float ty2) {
        x1 = tx1;
        y1 = ty1;
        x2 = tx2;
        y2 = ty2;
    }

    public Rectangle(double tx1, double d, double tx2, double ty2) {
        this((float)tx1, (float)d, (float)tx2, (float)ty2);
    }
}