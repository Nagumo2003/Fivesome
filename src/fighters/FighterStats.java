package fighters;

public class FighterStats {
    public final String name;
    public final int maxHealth;
    public final double speed;
    public final int lightDamage;
    public final int heavyDamage;
    public final int kickDamage;
    public final int specialDamage;
    public final int ultimateDamage;
    public final int maxMana;
    public final int specialCost;
    public final int ultimateCost;

    public final String specialName;
    public final String ultimateName;
    public final String specialTag;
    public final String ultimateTag;
    public final int specialRange;
    public final int ultimateRange;
    public final int specialKnockback;
    public final int ultimateKnockback;
    public final int specialLift;
    public final int ultimateLift;
    public final int specialStunMs;
    public final int ultimateStunMs;
    public final int specialCooldownMs;
    public final int ultimateCooldownMs;
    public final int specialManaRefundOnHit;
    public final int ultimateManaRefundOnHit;

    public FighterStats(
            String name,
            int maxHealth,
            double speed,
            int lightDamage,
            int heavyDamage,
            int kickDamage,
            int specialDamage,
            int ultimateDamage,
            int maxMana,
            int specialCost,
            int ultimateCost,
            String specialName,
            String ultimateName,
            String specialTag,
            String ultimateTag,
            int specialRange,
            int ultimateRange,
            int specialKnockback,
            int ultimateKnockback,
            int specialLift,
            int ultimateLift,
            int specialStunMs,
            int ultimateStunMs,
            int specialCooldownMs,
            int ultimateCooldownMs,
            int specialManaRefundOnHit,
            int ultimateManaRefundOnHit) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.lightDamage = lightDamage;
        this.heavyDamage = heavyDamage;
        this.kickDamage = kickDamage;
        this.specialDamage = specialDamage;
        this.ultimateDamage = ultimateDamage;
        this.maxMana = maxMana;
        this.specialCost = specialCost;
        this.ultimateCost = ultimateCost;
        this.specialName = specialName;
        this.ultimateName = ultimateName;
        this.specialTag = specialTag;
        this.ultimateTag = ultimateTag;
        this.specialRange = specialRange;
        this.ultimateRange = ultimateRange;
        this.specialKnockback = specialKnockback;
        this.ultimateKnockback = ultimateKnockback;
        this.specialLift = specialLift;
        this.ultimateLift = ultimateLift;
        this.specialStunMs = specialStunMs;
        this.ultimateStunMs = ultimateStunMs;
        this.specialCooldownMs = specialCooldownMs;
        this.ultimateCooldownMs = ultimateCooldownMs;
        this.specialManaRefundOnHit = specialManaRefundOnHit;
        this.ultimateManaRefundOnHit = ultimateManaRefundOnHit;
    }
}
