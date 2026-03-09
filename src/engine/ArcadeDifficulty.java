package engine;

public enum ArcadeDifficulty {
    BEGINNER("Beginner", 0.90, 0.92, 2, "More healing, extra continue, slower bosses"),
    NORMAL("Normal", 1.00, 1.00, 1, "Standard arcade run"),
    HARD("Hard", 1.12, 1.12, 1, "Stronger AI and tighter healing"),
    NIGHTMARE("Nightmare", 1.26, 1.22, 0, "Bosses hit harder and you get no continues");

    public final String label;
    public final double enemyHealthMultiplier;
    public final double aiMultiplier;
    public final int continues;
    public final String description;

    ArcadeDifficulty(String label, double enemyHealthMultiplier, double aiMultiplier, int continues, String description) {
        this.label = label;
        this.enemyHealthMultiplier = enemyHealthMultiplier;
        this.aiMultiplier = aiMultiplier;
        this.continues = continues;
        this.description = description;
    }

    @Override
    public String toString() {
        return label;
    }
}
