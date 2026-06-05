# 🐍 Snake Game - LibGDX

Un juego clásico de Snake desarrollado en Java utilizando el framework LibGDX.  
El proyecto incluye mecánicas modernas como aumento de velocidad progresiva, sistema de récord, efectos de sonido y control de estado del juego.

---

## 🎮 Características

- 🐍 Movimiento clásico de Snake en grid
- 🍎 Sistema de comida aleatoria
- 📈 Puntuación en incrementos de 10 puntos
- 🏆 Sistema de récord persistente (Preferences)
- ⚡ Aumento de velocidad progresiva cada cierto puntaje
- 🔊 Efectos de sonido:
  - Comer comida
  - Muerte
  - Aumento de velocidad
- 💀 Sistema de Game Over con reinicio o salida
- ⏸️ Estado de inicio (el juego espera input para comenzar)
- 🎨 Diseño visual con cuadrícula estilo arcade

---

## 🕹️ Controles

- ⬆️⬇️⬅️➡️ Flechas del teclado
- WASD para movimiento alternativo

El juego inicia solo cuando se presiona una dirección.

---

## 🧠 Mecánicas del juego

- La serpiente crece al comer comida
- Cada comida suma +10 puntos
- Cada cierto puntaje aumenta la velocidad
- Si la serpiente choca contra:
  - su propio cuerpo 💀
  - los bordes del mapa 💀  
  → Game Over

---

## 💾 Sistema de récord

El juego guarda el mayor puntaje alcanzado usando:

- `Preferences` de LibGDX
- Se mantiene incluso después de cerrar el juego

---

## 🚀 Tecnologías usadas

- Java ☕
- LibGDX 🎮
- Gradle ⚙️

---

## 📁 Estructura del proyecto

- `Main.java` → lógica principal del juego
- `Snake.java` → movimiento y cuerpo de la serpiente
- `Comida.java` → generación de comida
- `GameOver.java` → menú de reinicio
- `Enums/` → estados del juego y tipos de muerte

---

## 🔊 Assets

- Sonidos `.wav` para:
  - comer
  - muerte
  - speed up

---

## 📌 Estado del proyecto

✔ Jugable  
✔ Funcional  
✔ En mejora visual y mecánica  

---

## 👨‍💻 Autor

Autor: Michael Ruiz