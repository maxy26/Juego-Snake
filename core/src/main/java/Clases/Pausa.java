package Clases;

import javax.swing.JOptionPane;

public class Pausa {
    public static int mostrar() {

        String[] opciones = {
            "Reanudar",
            "Salir"
        };

        return JOptionPane.showOptionDialog(
            null,
            "PAUSA",
            "Snake",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            opciones,
            opciones[0]
        );
    }
}
