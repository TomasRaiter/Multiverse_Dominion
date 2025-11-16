package main;

// Clase que representa un nivel en el modo historia del juego
public class Level {

    
    // Campos privados
     private String oponenteId;
    private String fondoArchivo;
    private BotIA.Nivel dificultadIA;
    private boolean esFinalBoss;

    // Overrides opcionales para configuracion personalizada de IA
    private Integer overrideRangoAtaque;
    private Double overrideVelAcercamiento;
    private Integer overrideCooldownAtaque;
    private Double overrideProbSalto;
    private Double overrideProbAgachar;
    private Integer overrideDistEvadir;
    private Double overrideAgresividad;
    
    // Constructor principal
    public Level(String oponenteId, String fondoArchivo, BotIA.Nivel dificultadIA) {
        this.oponenteId = oponenteId;
        this.fondoArchivo = fondoArchivo;
        this.dificultadIA = dificultadIA;
        this.esFinalBoss = false;
    }
    
    // Getters y Setters
     public String getOponenteId() {
        return oponenteId;
    }
    
    public void setOponenteId(String oponenteId) {
        this.oponenteId = oponenteId;
    }
    
    public String getFondoArchivo() {
        return fondoArchivo;
    }

    
    
    public void setFondoArchivo(String fondoArchivo) {
        this.fondoArchivo = fondoArchivo;
    }
    
    public BotIA.Nivel getDificultadIA() {
        return dificultadIA;
    }
    
    public void setDificultadIA(BotIA.Nivel dificultadIA) {
        this.dificultadIA = dificultadIA;
    }
    
    public boolean isEsFinalBoss() {
        return esFinalBoss;
    }
    
    public void setEsFinalBoss(boolean esFinalBoss) {
        this.esFinalBoss = esFinalBoss;
    }
    
    // Getters para overrides de IA
    public Integer getOverrideRangoAtaque() {
        return overrideRangoAtaque;
    }
    
    public void setOverrideRangoAtaque(Integer overrideRangoAtaque) {
        this.overrideRangoAtaque = overrideRangoAtaque;
    }
    
    public Double getOverrideVelAcercamiento() {
        return overrideVelAcercamiento;
    }
    
    public void setOverrideVelAcercamiento(Double overrideVelAcercamiento) {
        this.overrideVelAcercamiento = overrideVelAcercamiento;
    }
    
    public Integer getOverrideCooldownAtaque() {
        return overrideCooldownAtaque;
    }
    
    public void setOverrideCooldownAtaque(Integer overrideCooldownAtaque) {
        this.overrideCooldownAtaque = overrideCooldownAtaque;
    }
    
    public Double getOverrideProbSalto() {
        return overrideProbSalto;
    }
    
    public void setOverrideProbSalto(Double overrideProbSalto) {
        this.overrideProbSalto = overrideProbSalto;
    }
    
    public Double getOverrideProbAgachar() {
        return overrideProbAgachar;
    }
    
    public void setOverrideProbAgachar(Double overrideProbAgachar) {
        this.overrideProbAgachar = overrideProbAgachar;
    }
    
    public Integer getOverrideDistEvadir() {
        return overrideDistEvadir;
    }
    
    public void setOverrideDistEvadir(Integer overrideDistEvadir) {
        this.overrideDistEvadir = overrideDistEvadir;
    }
    
    public Double getOverrideAgresividad() {
        return overrideAgresividad;
    }
    
    public void setOverrideAgresividad(Double overrideAgresividad) {
        this.overrideAgresividad = overrideAgresividad;
    }
    
    // Metodo factory para crear un nivel simple
    public static Level simple(String oponenteId, String fondoArchivo, BotIA.Nivel dificultad) {
        return new Level(oponenteId, fondoArchivo, dificultad);
    }
    
    // Metodo factory para crear un nivel de jefe final
    public static Level finalBoss(String oponenteId, String fondoArchivo, BotIA.Nivel dificultad) {
        Level level = new Level(oponenteId, fondoArchivo, dificultad);
        level.setEsFinalBoss(true);
        return level;
    }

    
    
    // Metodo para configurar todos los overrides de una vez
    public void setOverrides(Integer rangoAtaque, Double velAcercamiento, 
                            Integer cooldownAtaque, Double probSalto,
                            Double probAgachar, Integer distEvadir, 
                            Double agresividad) {
        this.overrideRangoAtaque = rangoAtaque;
        this.overrideVelAcercamiento = velAcercamiento;
        this.overrideCooldownAtaque = cooldownAtaque;
        this.overrideProbSalto = probSalto;
        this.overrideProbAgachar = probAgachar;
        this.overrideDistEvadir = distEvadir;
        this.overrideAgresividad = agresividad;
    }
    
    @Override
    public String toString() {
        return "Level{" +
                "oponenteId='" + oponenteId + '\'' +
                ", fondoArchivo='" + fondoArchivo + '\'' +
                ", dificultadIA=" + dificultadIA +
                ", esFinalBoss=" + esFinalBoss +
                '}';
    }
}
