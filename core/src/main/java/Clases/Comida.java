package Clases;

public class Comida {

    public boolean esReal;

    public int xPosicion;
    public int yPosicion;

    public void generate(int width, int height, int cellSize) {

        xPosicion = (int)(Math.random() * (width / cellSize));
        yPosicion = (int)(Math.random() * (height / cellSize));
    }
}
