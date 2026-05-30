package Clases;

public class Comida {

    public int xPosicion;
    public int yPosicion;

    public void generate(int width, int height, int cellSize) {

        // genera posición aleatoria dentro del grid
        xPosicion = (int)(Math.random() * (width / cellSize));
        yPosicion = (int)(Math.random() * (height / cellSize));
    }
}
