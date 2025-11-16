# Multiverse Dominion

Un juego de lucha 2D desarrollado en Java con personajes de diferentes universos.

## Características

- **Modo Historia**: Campaña con 8 niveles progresivos
- **Modo PvP**: Combate entre dos jugadores
- **Personajes**: Darth Vader, Iron Man, Mr. Increíble, Pyke, Goku, Batman, Luke Skywalker, Naruto, Ash
- **IA Adaptativa**: Sistema de inteligencia artificial con diferentes niveles de dificultad
- **Sistema de Ranking**: Top 3 de mejores tiempos en modo historia

## Estructura del Proyecto

```
Multiverse_Dominion/
├── src/
│   ├── main/
│   │   ├── Main.java           # Punto de entrada y menús
│   │   ├── Juego.java          # Lógica principal del juego
│   │   ├── Jugador.java        # Clase del jugador
│   │   ├── BotIA.java          # Inteligencia artificial
│   │   ├── Level.java          # Configuración de niveles
│   │   ├── Sprite.java         # Manejo de sprites
│   │   ├── AudioPlayer.java    # Reproductor de audio
│   │   ├── Input.java          # Manejo de entrada
│   │   ├── CinematicManager.java # Cinemáticas
│   │   └── DebugPika.java      # Utilidades de debug
│   └── resources/              # Recursos del juego
│       ├── BackGround/         # Fondos de escenario
│       ├── Intro/              # Imágenes de introducción
│       └── [Personajes]/       # Sprites y sonidos por personaje
└── README.md
```

## Cómo Ejecutar

1. Asegúrate de tener Java 17 o superior instalado
2. Compila el proyecto: `javac -cp src src/main/*.java`
3. Ejecuta el juego: `java -cp src main.Main`

## Controles

- **Movimiento**: Flechas izquierda/derecha
- **Salto**: Flecha arriba
- **Agacharse**: Flecha abajo
- **Ataque**: Barra espaciadora
- **Pausa**: ESC

## Modo Historia

Completa 8 niveles enfrentando diferentes oponentes:
1. Batman en Metro Ville
2. Iron Man en Batcave
3. Ash en Pokémon Stadium
4. Naruto en Konohagakure
5. Goku en Kame House
6. Pyke en Bilgewater
7. Luke Skywalker en Luke House
8. Darth Vader (Jefe Final) en Death Star

## Características Técnicas

- **Resolución**: Escalado automático para pantalla completa
- **FPS**: 60 frames por segundo
- **Audio**: Soporte para WAV con conversión automática a PCM
- **Gráficos**: Sprites PNG y GIF animados
- **Persistencia**: Guardado automático del progreso

## Bugs Corregidos

- ✅ Eliminados directorios de compilación innecesarios
- ✅ Corregido error de tipeo en ruta de sprite de Batman
- ✅ Mejorado manejo de excepciones en carga de recursos
- ✅ Eliminadas referencias a directorios bin/resources inexistentes
- ✅ Agregadas validaciones de null en parámetros críticos
- ✅ Optimizada carga de recursos desde classpath y src/resources

## Contribuir

Para contribuir al proyecto:
1. Haz fork del repositorio
2. Crea una rama para tu feature
3. Realiza tus cambios
4. Envía un pull request

## Licencia

Este proyecto es de código abierto para fines educativos.
