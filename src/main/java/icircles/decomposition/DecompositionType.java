package icircles.decomposition;

public enum DecompositionType {
    ALPHABETICAL("Decompose in alphabetic order", DecompositionStrategyUseSortOrder.class),
    REVERSE_ALPHABETICAL("Decompose in reverse alphabetic order", DecompositionStrategyUseSortOrder.class),
    INNERMOST("Decompose using fewest-zone contours first", DecompositionStrategyInnermost.class),
    PIERCED_FIRST("Decompose using piercing curves first", DecompositionStrategyPiercing.class);

    private Class<? extends DecompositionStrategy> strategy;

    public DecompositionStrategy strategy() {
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

    DecompositionType(String uiName, Class<? extends DecompositionStrategy> strategy) {
        this.strategy = strategy;
        this.uiName = uiName;
    }
}
