package icom.mike.primerjuego;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import Clases.Snake;
import Clases.Direccion;
import icom.mike.primerjuego.Main;

public class LectorJoystick implements Runnable {

    private SerialPort puerto; //Representa la conexión física del programa con el Arduino
    private Main juego; //Referencia a la clase principal
    private boolean corriendo = true;
    private int lecturasIgnoradas = 0; //Ignora los datos corruptos que aparecen cuando justo conectamos el cable

    // Umbrales para el Joystick
    /*
    * Estos dos atributos se crearon porque el joystick enviará
    * datos analógicos, lo cual haría que la serpiente se mueva
    * con cualquier roce, por lo cual damos un rango de valores
    * muertos para tratar con este fenómeno.
    * */

    private final int UMBRAL_BAJO = 450;
    private final int UMBRAL_ALTO = 900;

    public LectorJoystick(String nombrePuerto, Main juego) {
        this.puerto = SerialPort.getCommPort(nombrePuerto);
        this.juego = juego;

        // Configuramos la misma velocidad con la que el Arduino trabaja: Serial.begin(9600)
        this.puerto.setComPortParameters(9600, 8, 1, 0);
        this.puerto.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        //El Time Out Scanner le indica a Java cuanto tiempo tiene que esperar para recibir datos del Arduino
    }

    /*
    * Estas son las operaciones que el hilo estará
    * ejecutando constantemente, por esta razón se hace
    * uso del override, puesto que sus resultados de operación
    * no serán fijos
    * */

    @Override
    public void run() {
        if (!puerto.openPort()) {
            System.err.println("ERROR: No se pudo abrir el puerto serie: " + puerto.getSystemPortName());
            return;
        }
        System.out.println("CONECTADO: Puerto serie abierto en " + puerto.getSystemPortName());

        try (Scanner scanner = new Scanner(puerto.getInputStream())) {
            while (corriendo && scanner.hasNextLine()) {
                String linea = scanner.nextLine();
                procesarDatos(linea);
            }
        } catch (Exception e) {
            System.err.println("Error leyendo datos del puerto: " + e.getMessage());
        } finally {
            puerto.closePort();
        }
    }

    private void procesarDatos(String linea) {
        // Escudo: Ignoramos las primeras 10 lecturas para limpiar el ruido del cable al conectar
        if (lecturasIgnoradas < 10) {
            lecturasIgnoradas++;
            return;
        }

        try {
            //Arduino envía: "xVal,yVal,buttonState"
            String[] partes = linea.trim().split(",");
            if (partes.length < 3) return;

            int xVal = Integer.parseInt(partes[0]);
            int yVal = Integer.parseInt(partes[1]);
            int boton = Integer.parseInt(partes[2]);

            // Evaluación del Eje X (Izquierda / Derecha)
            if (xVal < UMBRAL_BAJO) {
                System.out.println("¡Detectado: Izquierda!");
                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    System.out.println("-> Ejecutando Izquierda en el hilo gráfico");
                    juego.serpiente.agregarDireccion(-1, 0);
                });
            } else if (xVal > UMBRAL_ALTO) {
                System.out.println("¡Detectado: Derecha!");
                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    System.out.println("-> Ejecutando Derecha en el hilo gráfico");
                    juego.serpiente.agregarDireccion(1, 0);
                });
            }

            // Evaluación del Eje Y (Abajo / Arriba)
            if (yVal < UMBRAL_BAJO) {
                /* Si el valor es BAJO, la serpiente estaría yendo hacia arriba, pero como
                * el joystick trabaja estos valores al revés, tenemos que invertir
                * el resultado*/
                com.badlogic.gdx.Gdx.app.postRunnable(() -> juego.serpiente.agregarDireccion(0, 1));
            } else if (yVal > UMBRAL_ALTO) {
                /*Si el valor es ALTO, la serpiente iría hacia abajo, pero como
                 * el joystick trabaja estos valores al revés, tenemos que invertir
                 * el resultado*/
                com.badlogic.gdx.Gdx.app.postRunnable(() -> juego.serpiente.agregarDireccion(0, -1));
            }

            // Acción del botón
            if (boton == 0) {

            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            // Ignorar ruidos
        }
    }

    public void detener() {
        this.corriendo = false;
        if (puerto != null && puerto.isOpen()) {
            puerto.closePort();
        }
    }
}
