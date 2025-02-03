package net.celloscope.mraims.loanportfolio.features.migration;

import java.math.RoundingMode;

public enum RoundingLogic {
    HALFUP(RoundingMode.HALF_UP),
    HALFDOWN(RoundingMode.HALF_DOWN),

    UP(RoundingMode.UP),
    DOWN(RoundingMode.DOWN),
    NO_ROUNDING(RoundingMode.DOWN),
    ;

    private final RoundingMode roundingMode;

    RoundingLogic(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public static RoundingMode fromString(String roundingLogic) {
        try {
            return RoundingLogic.valueOf(roundingLogic.toUpperCase()).getRoundingMode();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}