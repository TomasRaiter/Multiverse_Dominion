package main;

import main.BotIA.Nivel;

/**
 * Metadatos de un nivel de Modo Historia.
 */
public class Level {
    public final String oponenteId;
    public final String fondoArchivo;
    public final Nivel dificultadIA;
    public final boolean esFinalBoss;

    // Overrides opcionales por nivel (pueden ser null para no aplicar)
    public final Integer overrideRangoAtaque;
    public final Integer overrideVelAcercamiento;
    public final Integer overrideCooldownAtaque;
    public final Integer overrideProbSalto;
    public final Integer overrideProbAgachar;
    public final Integer overrideDistEvadir;
    public final Integer overrideAgresividad;

    public Level(String oponenteId,
                 String fondoArchivo,
                 Nivel dificultadIA,
                 boolean esFinalBoss,
                 Integer overrideRangoAtaque,
                 Integer overrideVelAcercamiento,
                 Integer overrideCooldownAtaque,
                 Integer overrideProbSalto,
                 Integer overrideProbAgachar,
                 Integer overrideDistEvadir,
                 Integer overrideAgresividad) {
        this.oponenteId = oponenteId;
        this.fondoArchivo = fondoArchivo;
        this.dificultadIA = dificultadIA;
        this.esFinalBoss = esFinalBoss;
        this.overrideRangoAtaque = overrideRangoAtaque;
        this.overrideVelAcercamiento = overrideVelAcercamiento;
        this.overrideCooldownAtaque = overrideCooldownAtaque;
        this.overrideProbSalto = overrideProbSalto;
        this.overrideProbAgachar = overrideProbAgachar;
        this.overrideDistEvadir = overrideDistEvadir;
        this.overrideAgresividad = overrideAgresividad;
    }

    public static Level simple(String oponenteId, String fondoArchivo, Nivel dificultadIA) {
        return new Level(oponenteId, fondoArchivo, dificultadIA, false,
                null, null, null, null, null, null, null);
    }

    public static Level finalBoss(String oponenteId, String fondoArchivo, Nivel dificultadIA) {
        return new Level(oponenteId, fondoArchivo, dificultadIA, true,
                null, null, null, null, null, null, null);
    }
}