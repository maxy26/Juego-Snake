package icom.mike.primerjuego;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import com.badlogic.gdx.Gdx;
import Clases.Snake;
import Clases.Direccion;
import icom.mike.primerjuego.Main;

public class LectorJoystick implements Runnable {

    private SerialPort puerto; //Representa la conexión física del programa con el Arduino
    private Main juego; //Referencia a la clase principal
    private boolean corriendo = true;
    private int lecturasIgnoradas = 0; //Ignora los datos corruptos que aparecen cuando justo conectamos el cable
    private volatile float ax;
    private volatile float gz;
    private volatile float ay;
    private volatile float az;
    private volatile float gx;
    private volatile float gy;

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

        /*Descarta las primeras lecturas para evitar valores inestables
        producidos durante la inicialización del puerto serial.*/

        if (lecturasIgnoradas < 10) {
            lecturasIgnoradas++;
            return;
        }

        try {

            // Formato esperado desde Arduino:
            // xVal,yVal,buttonState
            String[] partes = linea.trim().split(",");

            if (partes.length < 9) {
                return;
            }

            int xVal = Integer.parseInt(partes[0]);
            int yVal = Integer.parseInt(partes[1]);
            int boton = Integer.parseInt(partes[2]);
            ax = Float.parseFloat(partes [3]);
            ay = Float.parseFloat(partes [4]);
            az = Float.parseFloat(partes [5]);
            gx = Float.parseFloat(partes [6]);
            gy = Float.parseFloat(partes [7]);
            gz = Float.parseFloat(partes [8]);

            {
                System.out.println("Aceleración = X:" + ax + "Y:" + ay + "Z:" + az);
                System.out.println("------------------------");
                System.out.println("Velocidad = X: " + gx + "Y: " + gy + "Z:" + gz);
            }

            if (xVal < UMBRAL_BAJO) {

                Gdx.app.postRunnable(() ->
                    juego.serpiente.agregarDireccion(-1, 0)
                );

            } else if (xVal > UMBRAL_ALTO) {

                Gdx.app.postRunnable(() ->
                    juego.serpiente.agregarDireccion(1, 0)
                );
            }

            if (yVal < UMBRAL_BAJO) {

                // El joystick entrega valores invertidos respecto
                // al sistema de movimiento utilizado por la serpiente.
                Gdx.app.postRunnable(() ->
                    juego.serpiente.agregarDireccion(0, 1)
                );

            } else if (yVal > UMBRAL_ALTO)
            {

                Gdx.app.postRunnable(() ->
                    juego.serpiente.agregarDireccion(0, -1)
                );
            }

            if (boton == 0 && juego.ButonSeleccionado != 0)
            {
                Gdx.app.postRunnable(() ->
                    juego.ButonSeleccionado = 0
                );

                Gdx.app.postRunnable(() ->
                    juego.pausarJuego()
                );
            }

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            // Se ignoran paquetes corruptos o incompletos.
        }
    }

    public void detener() {
        this.corriendo = false;
        if (puerto != null && puerto.isOpen()) {
            puerto.closePort();
        }
    }
}
