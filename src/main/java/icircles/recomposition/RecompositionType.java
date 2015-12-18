package icircles.recomposition;

public enum RecompositionType {
    NESTED("recompose using zero-piercing (nesting)", RecompositionStrategyNested.class),
    SINGLY_PIERCED("recompose using single piercings", RecompositionStrategySinglyPierced.class),
    DOUBLY_PIERCED("recompose using double piercings", RecompositionStrategyDoublyPierced.class);

    private Class<? extends RecompositionStrategy> strategy;

    public RecompositionStrategy strategy() {
        try {
            return strategy.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Strategy cannot be created: " + e.getMessage());
        }
    }

    private String uiName;

    public String getUiName() {
        return uiName;
    }

    RecompositionType(String uiName, Class<? extends RecompositionStrategy> strategy) {
        this.strategy = strategy;
        this.uiName = uiName;
    }
}
