package icom.mike.primerjuego;

import Clases.Comida;
import Clases.GameOver;
import Clases.Snake;
import Enums.EstadoJuego;
import Enums.TipoMuerte;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.Preferences;
import com.fazecast.jSerialComm.SerialPort;


public class Main extends ApplicationAdapter {

    ShapeRenderer shape;
    OrthographicCamera camera;

    SpriteBatch batch;
    BitmapFont font;
    Sound sonidoComer;
    Sound sonidoMuerte;
    Sound sonidoSpeed;
    public int ButonSeleccionado = 0;

    Preferences prefs;

    EstadoJuego estado = EstadoJuego.JUGANDO;

    // tamaño de celda (cada parte de la serpiente)
    int celda = 50;
    int speed = 1;
    int record = 0;

    Snake serpiente;
    Comida comida;

    public void generarComidaSegura() {

        boolean posicionValida = false;

        while (!posicionValida) {

            comida.generate(800, 600, celda);

            posicionValida = true;

            for (int[] parte : serpiente.cuerpo) {

                if (parte[0] == comida.xPosicion &&
                    parte[1] == comida.yPosicion) {

                    posicionValida = false;
                    break;
                }
            }
        }
    }

    int puntaje = 0;

    public void gameOverMenu(TipoMuerte tipo) {

        System.out.println("Game Over por: " + tipo);
        sonidoMuerte.play();

        int respuesta = GameOver.mostrar();

        if (respuesta == 0) {
            serpiente = new Snake();
            serpiente.xDireccion = 0;
            serpiente.yDireccion = 1;
            generarComidaSegura();
            puntaje = 0;
            speed = 1;
            estado = EstadoJuego.JUGANDO;
        }
        else {

            Gdx.app.exit();
        }
    }

    @Override
    public void create() {

        batch = new SpriteBatch();
        font = new BitmapFont();

        prefs = Gdx.app.getPreferences("SnakeRecord");
        record = prefs.getInteger("record", 0);

        sonidoComer = Gdx.audio.newSound(Gdx.files.internal("mixkit-winning-a-coin-video-game-2069.wav"));
        sonidoMuerte = Gdx.audio.newSound(Gdx.files.internal("mixkit-8-bit-lose-2031.wav"));
        sonidoSpeed = Gdx.audio.newSound( Gdx.files.internal("Speed.wav"));

        shape = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        serpiente = new Snake();
        comida = new Comida();

        generarComidaSegura();

        // Se establece la conexión del puerto donde está conectado el Arduino con el programa
        LectorJoystick lector = new LectorJoystick("COM3", this);
        Thread hiloJoystick = new Thread(lector); //Creación de un objeto de la clase Thread
        hiloJoystick.setDaemon(true); // Evita que el hilo se quede colgado en segundo plano al cerrar el juego
        hiloJoystick.start(); //Esta función hace que el hilo empieze a ejecutar su tarea
    }

    @Override
    public void render() {

        float delta = Gdx.graphics.getDeltaTime();

        // Entradas (Input)
        if (estado == EstadoJuego.JUGANDO) {

            //Arriba
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                serpiente.agregarDireccion(0, 1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                serpiente.agregarDireccion(0, 1);
            }
            //Abajo
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                serpiente.agregarDireccion(0, -1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                serpiente.agregarDireccion(0, -1);
            }
            //Izquierda
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                serpiente.agregarDireccion(-1, 0);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                serpiente.agregarDireccion(-1, 0);
            }
            //Derecha
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                serpiente.agregarDireccion(1, 0);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                serpiente.agregarDireccion(1, 0);
            }

            // le pide el tiempo del frame a el metodo (movimiento)
            serpiente.movimiento(delta);

            // cabeza de la serpiente
            int[] cabeza = serpiente.cuerpo.get(0);

            //Colision por Cuerpo
            for (int i = 1; i < serpiente.cuerpo.size(); i++) {

                int[] parte = serpiente.cuerpo.get(i);

                if (cabeza[0] == parte[0] &&
                    cabeza[1] == parte[1]) {

                    estado = EstadoJuego.GAMEOVER;
                    gameOverMenu(TipoMuerte.CUERPO);
                }
            }
            // Colision Por Borde (Mapa)
            if (cabeza[0] < 0 || cabeza[0] >= 800 / celda ||
                cabeza[1] < 0 || cabeza[1] >= 600 / celda) {

                estado = EstadoJuego.GAMEOVER;
                gameOverMenu(TipoMuerte.BORDE);
            }

            // Si la serpiente toca la comida, la comida aparece en otro lugar
            if (cabeza[0] == comida.xPosicion &&
                cabeza[1] == comida.yPosicion) {

                generarComidaSegura();

                // crecer serpiente
                serpiente.crecer = true;
                puntaje+=10;

                if (puntaje > record) {
                    record = puntaje;
                    prefs.putInteger("record", record);
                    prefs.flush();
                }

                if (puntaje % 100 == 0) {

                    serpiente.aumentarVelocidad();
                    speed++;
                    sonidoSpeed.play();
                }

                sonidoComer.play();
            }
        }

        // Limpiar pantalla
        Gdx.gl.glClearColor(0.25f, 0.20f, 0.05f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // actualizar camara
        camera.update();
        shape.setProjectionMatrix(camera.combined);

        //Dibujar mapa
        shape.begin(ShapeRenderer.ShapeType.Line);

        shape.setColor(0.12f, 0.10f, 0.02f, 1);

        for (int x = 0; x <= 800; x += celda)
            shape.line(x, 0, x, 600);

        for (int y = 0; y <= 600; y += celda)
            shape.line(0, y, 800, y);

        shape.setColor(0.25f, 0.20f, 0.05f, 1);
        shape.rect(0, 0, 800, 600);

        shape.end();

        // panel superior
        shape.begin(ShapeRenderer.ShapeType.Filled);

// snake
        for (int i = 0; i < serpiente.cuerpo.size(); i++) {

            int[] parte = serpiente.cuerpo.get(i);

            if (i == 0) {

                // cabeza
                shape.setColor(0, 0.6f, 0, 1);

                shape.rect(
                    parte[0] * celda,
                    parte[1] * celda,
                    celda,
                    celda
                );

                // ojos
                shape.setColor(1, 1, 1, 1);

                float x = parte[0] * celda;
                float y = parte[1] * celda;

                // mirando derecha
                if (serpiente.xDireccion == 1) {

                    shape.circle(x + 35, y + 35, 4);
                    shape.circle(x + 35, y + 15, 4);
                }

                // mirando izquierda
                else if (serpiente.xDireccion == -1) {

                    shape.circle(x + 15, y + 35, 4);
                    shape.circle(x + 15, y + 15, 4);
                }

                // mirando arriba
                else if (serpiente.yDireccion == 1) {

                    shape.circle(x + 15, y + 35, 4);
                    shape.circle(x + 35, y + 35, 4);
                }

                // mirando abajo
                else if (serpiente.yDireccion == -1) {

                    shape.circle(x + 15, y + 15, 4);
                    shape.circle(x + 35, y + 15, 4);
                }

            } else {

                // cuerpo
                shape.setColor(0, 1, 0, 1);

                shape.rect(
                    parte[0] * celda,
                    parte[1] * celda,
                    celda,
                    celda
                );
            }
        }

// comida
        shape.setColor(1, 0, 0, 1);
        shape.rect(
            comida.xPosicion * celda,
            comida.yPosicion * celda,
            celda,
            celda
        );

        shape.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.draw(batch,
            "SCORE: " + String.format("%06d", puntaje),
            10,
            580
        );

        font.draw(batch,
            "RECORD: " + String.format("%06d", record),
            10,
            550
        );

        font.draw(batch,
            "SPEED: " + speed,
            10,
            520
        );

        batch.end();
    }

    @Override
    public void dispose() {

        batch.dispose();
        font.dispose();

        sonidoComer.dispose();
        sonidoMuerte.dispose();
        sonidoSpeed.dispose();

        shape.dispose();
    }
}
