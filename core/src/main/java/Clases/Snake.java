package Clases;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Snake {

    public ArrayList<int[]> cuerpo;
    Queue<Direccion> bufferDireccion = new LinkedList<>();

    public int xDireccion = 0;
    public int yDireccion = 1;

    float tiempo = 0;
    float velocidad = 0.2f;

    public boolean crecer = false;

    public void agregarDireccion(int x, int y) {

        // evitar duplicados seguidos
        if (!bufferDireccion.isEmpty()) {
            Direccion last = ((LinkedList<Direccion>) bufferDireccion)
                .getLast();

            if (last.x == x && last.y == y) {
                return;
            }
        }

        // limitar tamaño del buffer a 2
        if (bufferDireccion.size() >= 2) {
            bufferDireccion.poll();
        }

        bufferDireccion.add(new Direccion(x,y));
    }

    public Snake() {
        cuerpo = new ArrayList<>();
        cuerpo.add(new int[]{5, 5});
    }

    public void movimiento(float delta) {

        tiempo += delta;

        if (tiempo >= velocidad) {

            // 1. PRIMERO aplicar dirección pendiente (si existe)
            if (!bufferDireccion.isEmpty()) {

                Direccion dir = bufferDireccion.poll();

                // bloquear reversa correctamente
                boolean esReversa =
                        (xDireccion == 1 && dir.x== -1) ||
                        (xDireccion == -1 && dir.x == 1) ||
                        (yDireccion == 1 && dir.y == -1) ||
                        (yDireccion == -1 && dir.y == 1);

                if (!esReversa) {
                    xDireccion = dir.x;
                    yDireccion = dir.y;
                }
            }

            // 2. mover snake
            int[] cabeza = cuerpo.get(0);

            int newX = cabeza[0] + xDireccion;
            int newY = cabeza[1] + yDireccion;

            cuerpo.add(0, new int[]{newX, newY});

            if (!crecer) {
                cuerpo.remove(cuerpo.size() - 1);
            } else {
                crecer = false;
            }

            tiempo = 0;
        }
    }
    public void aumentarVelocidad() {

        // límite para que no sea imposible
        if (velocidad > 0.05f) {
            velocidad -= 0.01f;
        }
    }
}
