package icircles.experimental;

public enum WFProperty {
    BRUSHING_POINT,
    TRIPLE_POINT,
    NON_SIMPLE_CURVE,
    CONCURRENCY,
    DISCONNECTED_ZONE,
    DUPLICATE_LABEL;

    // this can hold "badness" coefficient for a property unless
    // we want a variable
    private double value = 0;
}
