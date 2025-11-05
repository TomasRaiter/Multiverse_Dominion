package main;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Controlador de IA para Jugador 2 pensado para modo historia.
 *
 * Características:
 * - Funciona con cualquier personaje (usa las APIs públicas de Jugador).
 * - Máquina de estados simple (ACERCARSE, ATACAR, EVADIR, IDLE, SALTAR, AGACHAR).
 * - Configuración de dificultad por personaje y nivel global.
 * - Heurísticas: distancia al rival, rango de ataque, evasión si el rival ataca cerca,
 *   saltos oportunos y variación aleatoria para evitar patrones.
 */
public class BotIA {
    public enum Nivel { FACIL, NORMAL, DIFICIL, INSANO }
    private final Random rng = new Random();

    private Nivel nivel = Nivel.NORMAL;
    private final Map<String, Config> configs = new HashMap<>();
    private int cooldownDecision = 0;
    private Estado estado = Estado.IDLE;

    // Overrides por nivel (opcionales)
    private Integer overrideRangoAtaque = null;
    private Integer overrideVelAcercamiento = null;
    private Integer overrideCooldownAtaque = null;
    private Integer overrideProbSalto = null;
    private Integer overrideProbAgachar = null;
    private Integer overrideDistEvadir = null;
    private Integer overrideAgresividad = null;

    // Estados de alto nivel
    private enum Estado { ACERCARSE, ATACAR, EVADIR, IDLE, SALTAR, AGACHAR }

    // Configuración por personaje
    private static class Config {
        int rangoAtaque;        // distancia desde la que intenta atacar
        int velAcercamiento;    // frames que mantiene acercamiento antes de reevaluar
        int cooldownAtaque;     // frames mínimos entre intentos de ataque
        int probSalto;          // 0..100 probabilidad de saltar al acercarse
        int probAgachar;        // 0..100 probabilidad de agacharse defensivo
        int distEvadir;         // distancia crítica para evadir si el rival ataca
        int agresividad;        // 0..100 cuanto prioriza atacar vs esperar

        public Config(int rangoAtaque, int velAcercamiento, int cooldownAtaque,
                       int probSalto, int probAgachar, int distEvadir, int agresividad) {
            this.rangoAtaque = rangoAtaque;
            this.velAcercamiento = velAcercamiento;
            this.cooldownAtaque = cooldownAtaque;
            this.probSalto = probSalto;
            this.probAgachar = probAgachar;
            this.distEvadir = distEvadir;
            this.agresividad = agresividad;
        }
    }

    public BotIA() {
        // Configuraciones por personaje (base sobre altura normalizada ~300)
        // Ajusta valores por equilibrio deseado. Se escalan por nivel.
        configs.put("Darth_Vader",    new Config(180, 18, 20, 15, 10, 140, 70));
        configs.put("Iron_Man",       new Config(170, 16, 18, 20, 8, 130, 65));
        configs.put("Mr_Increible",   new Config(160, 14, 16, 10, 15, 120, 60));
        configs.put("Pyke",           new Config(165, 18, 16, 25, 12, 130, 75));
        configs.put("Goku",           new Config(175, 18, 16, 25, 10, 130, 80));
        configs.put("Batman",         new Config(155, 16, 18, 15, 14, 120, 60));
        configs.put("Luke_Skywalker", new Config(170, 17, 18, 18, 12, 130, 70));
        configs.put("Naruto",         new Config(165, 20, 16, 30, 8, 125, 80));
        // Pokémons (cuando se usen como personajes activos)
        configs.put("Ash",            new Config(150, 16, 20, 10, 10, 120, 55));
        configs.put("Charizard",      new Config(190, 20, 18, 20, 8, 140, 85));
        configs.put("Greninja",       new Config(170, 20, 14, 35, 8, 120, 85));
        configs.put("Pikachu",        new Config(150, 22, 12, 40, 6, 115, 85));
    }

    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

    /**
     * Aplicar overrides de parámetros específicos del nivel.
     * Cualquier parámetro con null no se modifica.
     */
    public void setOverrides(Integer rangoAtaque,
                             Integer velAcercamiento,
                             Integer cooldownAtaque,
                             Integer probSalto,
                             Integer probAgachar,
                             Integer distEvadir,
                             Integer agresividad) {
        this.overrideRangoAtaque = rangoAtaque;
        this.overrideVelAcercamiento = velAcercamiento;
        this.overrideCooldownAtaque = cooldownAtaque;
        this.overrideProbSalto = probSalto;
        this.overrideProbAgachar = probAgachar;
        this.overrideDistEvadir = distEvadir;
        this.overrideAgresividad = agresividad;
    }

    // Factor por nivel para escalar agresividad y cooldowns
    private Config escalarPorNivel(Config base) {
        int factorAgg;
        int factorCd; // multiplicador de cooldown (menor => más ataques)
        int factorRango; // modifica rango de ataque
        switch (nivel) {
            case FACIL -> { factorAgg = 70; factorCd = 120; factorRango = 90; }
            case NORMAL -> { factorAgg = 100; factorCd = 100; factorRango = 100; }
            case DIFICIL -> { factorAgg = 120; factorCd = 80; factorRango = 105; }
            default /* INSANO */ -> { factorAgg = 140; factorCd = 65; factorRango = 110; }
        }
        Config esc = new Config(
                Math.max(100, base.rangoAtaque * factorRango / 100),
                base.velAcercamiento,
                Math.max(6, base.cooldownAtaque * factorCd / 100),
                Math.min(100, base.probSalto * factorAgg / 100),
                Math.min(100, base.probAgachar * factorAgg / 100),
                base.distEvadir,
                Math.min(100, base.agresividad * factorAgg / 100)
        );
        // Aplicar overrides si existen
        if (overrideRangoAtaque != null) esc.rangoAtaque = overrideRangoAtaque;
        if (overrideVelAcercamiento != null) esc.velAcercamiento = overrideVelAcercamiento;
        if (overrideCooldownAtaque != null) esc.cooldownAtaque = overrideCooldownAtaque;
        if (overrideProbSalto != null) esc.probSalto = overrideProbSalto;
        if (overrideProbAgachar != null) esc.probAgachar = overrideProbAgachar;
        if (overrideDistEvadir != null) esc.distEvadir = overrideDistEvadir;
        if (overrideAgresividad != null) esc.agresividad = overrideAgresividad;
        return esc;
    }

    /**
     * Llamar cada frame para que la IA mueva/ataque al bot.
     */
    public void actualizar(Jugador bot, Jugador rival) {
        if (bot == null || rival == null) return;
        String id = bot.getPersonajeId();
        Config base = configs.getOrDefault(id, new Config(160, 16, 18, 15, 10, 120, 65));
        Config cfg = escalarPorNivel(base);

        int dist = Math.abs(rival.getX() - bot.getX());
        boolean rivalAtaca = rival.atacando;

        // Enfriamiento de decisiones para evitar jitter
        if (cooldownDecision > 0) cooldownDecision--; else elegirEstado(cfg, dist, rivalAtaca);

        // Ejecutar estado actual
        switch (estado) {
            case ACERCARSE -> {
                if (rival.getX() < bot.getX()) bot.moverIzquierda(); else bot.moverDerecha();
                bot.setAgachado(false);
                if (rng.nextInt(100) < cfg.probSalto) bot.saltar();
            }
            case ATACAR -> {
                bot.detener();
                bot.setAgachado(false);
                bot.atacar();
            }
            case EVADIR -> {
                // Retirarse del rival brevemente
                if (rival.getX() < bot.getX()) bot.moverDerecha(); else bot.moverIzquierda();
                if (rng.nextInt(100) < 50) bot.saltar(); else bot.setAgachado(rng.nextInt(100) < cfg.probAgachar);
            }
            case SALTAR -> {
                bot.saltar();
                // Mantener aproximación en el aire
                if (rival.getX() < bot.getX()) bot.moverIzquierda(); else bot.moverDerecha();
            }
            case AGACHAR -> {
                bot.detener();
                bot.setAgachado(true);
            }
            default -> {
                bot.detener();
                bot.setAgachado(false);
            }
        }
    }

    private void elegirEstado(Config cfg, int dist, boolean rivalAtaca) {
        // Evasión si el rival ataca cerca
        if (rivalAtaca && dist < cfg.distEvadir) {
            estado = Estado.EVADIR;
            cooldownDecision = 6;
            return;
        }

        // Si estamos en rango, preferir atacar según agresividad
        if (dist <= cfg.rangoAtaque) {
            int roll = rng.nextInt(100);
            if (roll < cfg.agresividad) {
                estado = Estado.ATACAR;
                cooldownDecision = cfg.cooldownAtaque;
            } else {
                // Variación defensiva
                estado = (rng.nextInt(100) < cfg.probAgachar) ? Estado.AGACHAR : Estado.IDLE;
                cooldownDecision = 8;
            }
            return;
        }

        // Fuera de rango: acercarse con posibilidad de salto ofensivo
        if (rng.nextInt(100) < cfg.probSalto) {
            estado = Estado.SALTAR;
            cooldownDecision = 10;
        } else {
            estado = Estado.ACERCARSE;
            cooldownDecision = cfg.velAcercamiento;
        }
    }
}