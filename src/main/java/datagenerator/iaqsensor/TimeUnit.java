package datagenerator.iaqsensor;

public enum TimeUnit {

    NANOS("Nano", 1),
    MICROS("Micro", 1e3),
    MILLIS("Milli", 1e6),
    SECONDS("Second", 1e9),
    MINUTES("Minute", 1e9*60);

    private final String name;
    private final double v;

    TimeUnit(String name, double v) {
        this.name = name;
        this.v = v;
    }

    public double convertFromNano(long nanoSeconds) {
        return nanoSeconds / v;
    }
}
