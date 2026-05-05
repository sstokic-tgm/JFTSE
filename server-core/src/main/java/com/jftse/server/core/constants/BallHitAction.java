package com.jftse.server.core.constants;

public enum BallHitAction {
    STROKE(0),
    SLICE(1),
    LOB(2),
    SMASH(3),
    VOLLEY(4),
    TOP_SPIN(5),
    RISING(6),
    SERVE(7),
    G_BREAK_SHOT(8),
    GUARDIAN_SERVE(9),
    ATT_SPEC_0(10),
    DEF_SPEC_0(11),
    ATT_COBRA(12),
    ATT_EAGLE(13),
    DEF_FAST_RETURN(14),
    DEF_CURVE_RETURN(15),
    DEF_ROB_RETURN(16),
    ATT_DRAGON(17),
    ATT_UNDER(18),
    ATT_SUPER_EAGLE(19),
    ATT_TORNADO(20),
    ATT_DRAGON_BLACK(21);

    private final int id;

    BallHitAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static String describe(int id) {
        for (BallHitAction action : values()) {
            if (action.getId() == id) {
                return action.name();
            }
        }
        return "UnknownHitAct_" + id;
    }

    public  static BallHitAction valueOf(int id) {
        for (BallHitAction action : values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        throw new IllegalArgumentException("No BallHitAction with id " + id);
    }
}
