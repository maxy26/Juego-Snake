package icom.mike.primerjuego;

import Clases.Comida;
import Clases.Pausa;
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
import java.util.ArrayList;

public class Main extends ApplicationAdapter {

    ArrayList<Comida> comida = new ArrayList<>();
    Comida comidaReal;

    ShapeRenderer shape;
    OrthographicCamera camera;

    SpriteBatch batch;
    BitmapFont font;

    Sound sonidoError;
    Sound sonidoComer;
    Sound sonidoMuerte;
    Sound sonidoSpeed;

    public int ButonSeleccionado = 0;


    Preferences prefs;

    EstadoJuego estado = EstadoJuego.ESPERANDO;
    // tamaño de celda (cada parte de la serpiente)
    int celda = 50;
    int speed = 1;
    int record = 0;

    Snake serpiente;

    public void generarComidaValida(Comida c, ArrayList<int[]> ocupadas) {

        boolean valida = false;

        while (!valida) {

            c.generate(800, 600, celda);
            valida = true;

            for (int[] p : ocupadas) {

                if (p[0] == c.xPosicion && p[1] == c.yPosicion) {
                    valida = false;
                    break;
                }
            }
        }
    }

    public void generarComidas() {

        comida.clear();

        ArrayList<int[]> ocupadas = new ArrayList<>();

        // incluir snake
        for (int[] p : serpiente.cuerpo) {
            ocupadas.add(new int[]{p[0], p[1]});
        }

        // COMIDA REAL
        comidaReal = new Comida();
        comidaReal.esReal = true;
        generarComidaValida(comidaReal, ocupadas);
        comida.add(comidaReal);
        ocupadas.add(new int[]{comidaReal.xPosicion, comidaReal.yPosicion});

        // COMIDAS FALSAS
        for (int i = 0; i < 3; i++) {

            Comida fake = new Comida();
            fake.esReal = false;

            generarComidaValida(fake, ocupadas);

            comida.add(fake);
            ocupadas.add(new int[]{fake.xPosicion, fake.yPosicion});
        }
    }

    public void generarComidaSegura() {

        boolean posicionValida = false;

        while (!posicionValida) {

            Comida c = new Comida();
            c.generate(800, 600, celda);

            posicionValida = true;

            for (int[] parte : serpiente.cuerpo) {

                if (parte[0] == c.xPosicion &&
                    parte[1] == c.yPosicion) {

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

    estado = EstadoJuego.ESPERANDO;

} else {

    Gdx.app.exit();
}
    }

    @Override
    public void create() {

        batch = new SpriteBatch();
        font = new BitmapFont();

        prefs = Gdx.app.getPreferences("SnakeRecord");
        record = prefs.getInteger("record", 0);

        sonidoError = Gdx.audio.newSound(Gdx.files.internal("Comida_Falsa.wav"));
        sonidoComer = Gdx.audio.newSound(Gdx.files.internal("mixkit-winning-a-coin-video-game-2069.wav"));
        sonidoMuerte = Gdx.audio.newSound(Gdx.files.internal("mixkit-8-bit-lose-2031.wav"));
        sonidoSpeed = Gdx.audio.newSound( Gdx.files.internal("Speed.wav"));

        shape = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        serpiente = new Snake();
        generarComidas();


        // Se establece la conexión del puerto donde está conectado el Arduino con el programa
        LectorJoystick lector = new LectorJoystick("COM3", this);
        Thread hiloJoystick = new Thread(lector); //Creación de un objeto de la clase Thread
        hiloJoystick.setDaemon(true); // Evita que el hilo se quede colgado en segundo plano al cerrar el juego
        hiloJoystick.start(); //Esta función hace que el hilo empieze a ejecutar su tarea
    }

    @Override
    public void render() {

        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            int respuesta = Pausa.mostrar();

            if (respuesta == 0) {

                estado = EstadoJuego.ESPERANDO;

            }
            else if (respuesta == 1) {

                Gdx.app.exit();

            }
        }

        // Detectar primera dirección para iniciar
        if (estado == EstadoJuego.ESPERANDO) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) ||
                Gdx.input.isKeyJustPressed(Input.Keys.W)) {

                serpiente.agregarDireccion(0, 1);
                estado = EstadoJuego.JUGANDO;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) ||
                Gdx.input.isKeyJustPressed(Input.Keys.S)) {

                serpiente.agregarDireccion(0, -1);
                estado = EstadoJuego.JUGANDO;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) ||
                Gdx.input.isKeyJustPressed(Input.Keys.A)) {

                serpiente.agregarDireccion(-1, 0);
                estado = EstadoJuego.JUGANDO;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) ||
                Gdx.input.isKeyJustPressed(Input.Keys.D)) {

                serpiente.agregarDireccion(1, 0);
                estado = EstadoJuego.JUGANDO;
            }
        }

        // Entradas (Input)
        if (estado == EstadoJuego.JUGANDO) {

            //Arriba
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                serpiente.agregarDireccion(0, 1);
                estado = EstadoJuego.JUGANDO;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                serpiente.agregarDireccion(0, 1);
                estado = EstadoJuego.JUGANDO;
            }
            //Abajo
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                serpiente.agregarDireccion(0, -1);
                estado = EstadoJuego.JUGANDO;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                serpiente.agregarDireccion(0, -1);
                estado = EstadoJuego.JUGANDO;
            }
            //Izquierda
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                serpiente.agregarDireccion(-1, 0);
                estado = EstadoJuego.JUGANDO;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                serpiente.agregarDireccion(-1, 0);
                estado = EstadoJuego.JUGANDO;
            }
            //Derecha
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                serpiente.agregarDireccion(1, 0);
                estado = EstadoJuego.JUGANDO;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                serpiente.agregarDireccion(1, 0);
                estado = EstadoJuego.JUGANDO;
            }

            // le pide el tiempo del frame a el metodo (movimiento)
            if (estado == EstadoJuego.JUGANDO) {
                serpiente.movimiento(delta);
            }

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
            boolean comio = false;
            boolean reiniciarComida = false;

            for (Comida c : comida) {

                if (comio) break;

                if (cabeza[0] == c.xPosicion &&
                    cabeza[1] == c.yPosicion) {

                    // COMIDA REAL
                    if (c.esReal) {
                        serpiente.crecer = true;
                        puntaje += 10;
                        sonidoComer.play();
                        comio = true;
                        reiniciarComida = true;

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

                    }
                    // COMIDA FALSA
                    else {
                        sonidoError.play();
                        comio = true;

                        ArrayList<int[]> ocupadas = new ArrayList<>();

                        // posiciones del snake
                        for (int[] p : serpiente.cuerpo) {
                            ocupadas.add(new int[]{p[0], p[1]});
                        }

                        // posiciones de las demás comidas
                        for (Comida otra : comida) {

                            if (otra != c) {
                                ocupadas.add(new int[]{otra.xPosicion, otra.yPosicion});
                            }
                        }

                        // mover la comida falsa que fue tocada
                        generarComidaValida(c, ocupadas);
                    }
                }
            }
            if (reiniciarComida){
                generarComidas();
                reiniciarComida = false;
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
                shape.setColor(0, 0.4f, 0, 1); // verde oscuro

                shape.rect(
                    parte[0] * celda,
                    parte[1] * celda,
                    celda,
                    celda
                );

                float xCabeza = parte[0] * celda;
                float yCabeza = parte[1] * celda;

                float xComida = comidaReal.xPosicion * celda;
                float yComida = comidaReal.yPosicion * celda;

                // vector hacia la comida
                float dirX = xComida - xCabeza;
                float dirY = yComida - yCabeza;

                // normalizar
                float len = (float)Math.sqrt(dirX * dirX + dirY * dirY);
                if (len != 0) {
                    dirX /= len;
                    dirY /= len;
                }

                // dibujar ojos blancos (FIJOS)
                shape.setColor(1, 1, 1, 1);

                float ojo1X = xCabeza + 15;
                float ojo1Y = yCabeza + 35;

                float ojo2X = xCabeza + 35;
                float ojo2Y = yCabeza + 15;

                shape.circle(ojo1X, ojo1Y, 6);
                shape.circle(ojo2X, ojo2Y, 6);

                // pupilas (MOVIMIENTO)
                shape.setColor(0, 0, 0, 1);

                float offset = 3;

                shape.circle(
                    ojo1X + dirX * offset,
                    ojo1Y + dirY * offset,
                    4
                );

                shape.circle(
                    ojo2X + dirX * offset,
                    ojo2Y + dirY * offset,
                    4
                );

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
        // comida (TODAS)
        for (Comida c : comida) {

            if (c.esReal) {
                shape.setColor(1, 0, 0, 1); // real rojo
            } else {
                shape.setColor(1, 0, 0, 1); // falsa amarillo
            }

            shape.rect(
                c.xPosicion * celda,
                c.yPosicion * celda,
                celda,
                celda
            );
        }

        shape.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (estado == EstadoJuego.ESPERANDO) {

            font.draw(batch,
                "PRESIONA UNA DIRECCION PARA COMENZAR",
                225,
                300
            );
        }

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
