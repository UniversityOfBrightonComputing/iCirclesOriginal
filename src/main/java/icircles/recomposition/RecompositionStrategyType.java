package icircles.recomposition;

public enum RecompositionStrategyType {
    NESTED("Recompose using zero-piercing (nesting)"),
    SINGLY_PIERCED("Recompose using single piercings"),
    DOUBLY_PIERCED("Recompose using double piercings");

    private String uiName;

    public String getUiName() {
        return uiName;
    }

    RecompositionStrategyType(String uiName) {
        this.uiName = uiName;
    }
}
