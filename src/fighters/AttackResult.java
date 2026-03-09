package fighters;

public class AttackResult {
    public final int damage;
    public final int range;
    public final int stunMs;
    public final int knockbackX;
    public final int liftY;
    public final Fighter source;
    public final String attackName;
    public final boolean specialMove;
    public final boolean ultimateMove;
    public final int bonusManaOnHit;

    public AttackResult(int damage, int range, int stunMs, int knockbackX, int liftY, Fighter source,
                        String attackName, boolean specialMove, boolean ultimateMove, int bonusManaOnHit) {
        this.damage = damage;
        this.range = range;
        this.stunMs = stunMs;
        this.knockbackX = knockbackX;
        this.liftY = liftY;
        this.source = source;
        this.attackName = attackName;
        this.specialMove = specialMove;
        this.ultimateMove = ultimateMove;
        this.bonusManaOnHit = bonusManaOnHit;
    }
}
