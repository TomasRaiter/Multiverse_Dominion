package main;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// inteligencia artificial para controlar oponentes en modo historia
public class BotIA {
    // niveles de dificultad de la ia
    public enum Nivel { FACIL, NORMAL, DIFICIL, INSANO }
    private final Random rng = new Random();

    // estado y configuracion de la ia
    private Nivel nivel = Nivel.NORMAL;
    private final Map<String, Config> configs = new HashMap<>();
    private int cooldownDecision = 0;  // frames de espera entre decisiones
    private Estado estado = Estado.IDLE;

    // overrides opcionales por nivel para ajustar comportamiento
    private Integer overrideRangoAtaque = null;
    private Double overrideVelAcercamiento = null;  // Changed from Integer to Double
    private Integer overrideCooldownAtaque = null;
    private Double overrideProbSalto = null;
    private Double overrideProbAgachar = null;
    private Integer overrideDistEvadir = null;
    private Double overrideAgresividad = null;

    // estados posibles de la ia
    private enum Estado { ACERCARSE, ATACAR, EVADIR, IDLE, SALTAR, AGACHAR }

    // configuracion de comportamiento para cada personaje
    private static class Config {
    int rangoAtaque;
    int velAcercamiento;
    int cooldownAtaque;
    double probSalto;          // Changed from int to double
    double probAgachar;        // Changed from int to double
    int distEvadir;
    double agresividad;        // Changed from int to double

    public Config(int rangoAtaque, int velAcercamiento, int cooldownAtaque,
                 double probSalto, double probAgachar, int distEvadir, double agresividad) {
        this.rangoAtaque = rangoAtaque;
        this.velAcercamiento = velAcercamiento;
        this.cooldownAtaque = cooldownAtaque;
        this.probSalto = probSalto;
        this.probAgachar = probAgachar;
        this.distEvadir = distEvadir;
        this.agresividad = agresividad;
    }
}

    // constructor: inicializa configuraciones por personaje
    public BotIA() {
        // configuraciones de personajes principales
       configs.put("Darth_Vader",    new Config(180, 18, 20, 15.0, 10.0, 140, 70.0));
configs.put("Iron_Man",       new Config(170, 16, 18, 20.0, 8.0, 130, 65.0));
        configs.put("Mr_Increible",   new Config(160, 14, 16, 10, 15, 120, 60));
        configs.put("Pyke",           new Config(165, 18, 16, 25, 12, 130, 75));
        configs.put("Goku",           new Config(175, 18, 16, 25, 10, 130, 80));
        configs.put("Batman",         new Config(155, 16, 18, 15, 14, 120, 60));
        configs.put("Luke_Skywalker", new Config(170, 17, 18, 18, 12, 130, 70));
        configs.put("Naruto",         new Config(165, 20, 16, 30, 8, 125, 80));

        // configuraciones de pokemon
        configs.put("Ash",            new Config(150, 16, 20, 10, 10, 120, 55));
        configs.put("Charizard",      new Config(190, 20, 18, 20, 8, 140, 85));
        configs.put("Greninja",       new Config(170, 20, 14, 35, 8, 120, 85));
        configs.put("Pikachu",        new Config(150, 22, 12, 40, 6, 115, 85));
    }

    // establece el nivel de dificultad de la ia
    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    // aplica overrides personalizados para ajustar comportamiento por nivel
    public void setOverrides(Integer rangoAtaque,
                           Double velAcercamiento,
                           Integer cooldownAtaque,
                           Double probSalto,
                           Double probAgachar,
                           Integer distEvadir,
                           Double agresividad) {
            this.overrideRangoAtaque = rangoAtaque;
            this.overrideVelAcercamiento = velAcercamiento;
            this.overrideCooldownAtaque = cooldownAtaque;
            this.overrideProbSalto = probSalto;
            this.overrideProbAgachar = probAgachar;
            this.overrideDistEvadir = distEvadir;
            this.overrideAgresividad = agresividad;
        }

    // escala la configuracion base segun el nivel de dificultad
    private Config escalarPorNivel(Config base) {
    int factorAgg;    // factor de agresividad
    int factorCd;     // factor de cooldown
    int factorRango;  // factor de rango de ataque
    switch (nivel) {
        case FACIL -> { factorAgg = 70; factorCd = 120; factorRango = 90; }
        case NORMAL -> { factorAgg = 100; factorCd = 100; factorRango = 100; }
        case DIFICIL -> { factorAgg = 120; factorCd = 80; factorRango = 105; }
        default  -> { factorAgg = 140; factorCd = 65; factorRango = 110; }  // INSANO
    }
    
    // crear configuracion escalada
    Config esc = new Config(
            Math.max(100, base.rangoAtaque * factorRango / 100),
            base.velAcercamiento,
            Math.max(6, base.cooldownAtaque * factorCd / 100),
            Math.min(100.0, base.probSalto * factorAgg / 100.0),
            Math.min(100.0, base.probAgachar * factorAgg / 100.0),
            base.distEvadir,
            Math.min(100.0, base.agresividad * factorAgg / 100.0)
    );

    // aplicar overrides si estan definidos
    if (overrideRangoAtaque != null) esc.rangoAtaque = overrideRangoAtaque;
    if (overrideVelAcercamiento != null) esc.velAcercamiento = overrideVelAcercamiento.intValue();
    if (overrideCooldownAtaque != null) esc.cooldownAtaque = overrideCooldownAtaque;
    if (overrideProbSalto != null) esc.probSalto = overrideProbSalto.doubleValue();
    if (overrideProbAgachar != null) esc.probAgachar = overrideProbAgachar.doubleValue();
    if (overrideDistEvadir != null) esc.distEvadir = overrideDistEvadir;
    if (overrideAgresividad != null) esc.agresividad = overrideAgresividad.doubleValue();
    return esc;
}

    // metodo principal: actualiza el comportamiento de la ia cada frame
    public void actualizar(Jugador bot, Jugador rival) {
        if (bot == null || rival == null) return;
        
        // obtener configuracion del personaje
        String id = bot.getPersonajeId();
        if (id == null) {
            System.err.println("[botia] id de personaje nulo, usando configuracion por defecto");
            id = "default";
        }
        Config base = configs.getOrDefault(id, new Config(160, 16, 18, 15.0, 10.0, 120, 65.0));
        Config cfg = escalarPorNivel(base);

        // calcular distancia y estado del rival
        int dist = Math.abs(rival.getX() - bot.getX());
        boolean rivalAtaca = rival.atacando;

        // tomar decisiones segun cooldown
        if (cooldownDecision > 0) cooldownDecision--; 
        else elegirEstado(cfg, dist, rivalAtaca);

        // ejecutar accion segun estado actual
        switch (estado) {
            case ACERCARSE -> {
                // moverse hacia el rival
                if (rival.getX() < bot.getX()) bot.moverIzquierda(); 
                else bot.moverDerecha();
                bot.setAgachado(false);
                if (rng.nextInt(100) < cfg.probSalto) bot.saltar();
            }
            case ATACAR -> {
                // atacar al rival
                bot.detener();
                bot.setAgachado(false);
                bot.atacar();
            }
            case EVADIR -> {
                // alejarse del rival
                if (rival.getX() < bot.getX()) bot.moverDerecha(); 
                else bot.moverIzquierda();
                if (rng.nextInt(100) < 50) bot.saltar(); 
                else bot.setAgachado(rng.nextInt(100) < cfg.probAgachar);
            }
            case SALTAR -> {
                // saltar mientras se acerca
                bot.saltar();
                if (rival.getX() < bot.getX()) bot.moverIzquierda(); 
                else bot.moverDerecha();
            }
            case AGACHAR -> {
                // agacharse defensivamente
                bot.detener();
                bot.setAgachado(true);
            }
            default -> {
                // estado idle: no hacer nada
                bot.detener();
                bot.setAgachado(false);
            }
        }
    }

    // decide que accion tomar segun la situacion actual
    private void elegirEstado(Config cfg, int dist, boolean rivalAtaca) {
        // prioridad 1: evadir si el rival ataca y esta cerca
        if (rivalAtaca && dist < cfg.distEvadir) {
            estado = Estado.EVADIR;
            cooldownDecision = 6;
            return;
        }

        // prioridad 2: atacar o defenderse si esta en rango
        if (dist <= cfg.rangoAtaque) {
            int roll = rng.nextInt(100);
            if (roll < cfg.agresividad) {
                estado = Estado.ATACAR;
                cooldownDecision = cfg.cooldownAtaque;
            } else {
                // decidir entre agacharse o quedarse idle
                estado = (rng.nextInt(100) < cfg.probAgachar) ? Estado.AGACHAR : Estado.IDLE;
                cooldownDecision = 8;
            }
            return;
        }

        // prioridad 3: acercarse al rival (con o sin salto)
        if (rng.nextInt(100) < cfg.probSalto) {
            estado = Estado.SALTAR;
            cooldownDecision = 10;
        } else {
            estado = Estado.ACERCARSE;
            cooldownDecision = cfg.velAcercamiento;
        }
    }
}