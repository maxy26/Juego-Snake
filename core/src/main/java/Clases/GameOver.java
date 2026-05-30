package Clases;

import javax.swing.JOptionPane;

public class GameOver {

    public static int mostrar() {

        String[] opciones = {"Reiniciar", "Salir"};

        return JOptionPane.showOptionDialog(
            null,
            "Game Over",
            "Fin del juego",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );
    }
}
