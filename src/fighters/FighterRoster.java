package fighters;

import java.util.ArrayList;
import java.util.List;

public class FighterRoster {
    public static List<FighterStats> getRoster() {
        List<FighterStats> list = new ArrayList<>();
        list.add(new FighterStats("Christopher", 240, 4.5, 8, 15, 12, 18, 28, 100, 25, 60,
                "Power Jab", "People's Uppercut", "boxer rush", "launcher stun",
                74, 88, 12, 22, 2, 12, 0, 1800, 1800, 5200, 4, 8));
        list.add(new FighterStats("Punch", 270, 3.7, 10, 19, 10, 20, 31, 95, 24, 58,
                "Iron Body Rush", "Treasury Breaker", "armor shove", "wall breaker",
                72, 82, 16, 26, 0, 6, 250, 1400, 2100, 5600, 3, 6));
        list.add(new FighterStats("Jason Tulfo", 245, 4.2, 9, 14, 13, 19, 29, 105, 24, 58,
                "Whistle Chain", "Expose Barrage", "counter whip", "long stun",
                82, 92, 10, 18, 4, 8, 700, 2200, 2000, 5400, 5, 10));
        list.add(new FighterStats("Wilson", 230, 5.0, 8, 13, 14, 19, 27, 110, 22, 56,
                "Cyclone Kick", "Senate Splitter", "air starter", "launcher combo",
                78, 90, 14, 20, 10, 16, 350, 1700, 1600, 5000, 4, 7));
        list.add(new FighterStats("Earl", 250, 4.1, 9, 16, 12, 20, 29, 100, 25, 60,
                "Street Rush", "Midnight Rebellion", "pressure dash", "balanced burst",
                76, 88, 13, 19, 5, 10, 300, 1800, 1800, 5200, 4, 8));
        list.add(new FighterStats("Sana", 220, 5.3, 7, 12, 13, 20, 26, 115, 22, 54,
                "Shadow Blink", "Eclipse Prison", "teleport slice", "trap stun",
                96, 100, 9, 16, 6, 10, 900, 2600, 1500, 4800, 6, 12));
        list.add(new FighterStats("Papi Robinhood", 235, 4.3, 8, 13, 12, 21, 28, 108, 23, 56,
                "Tax Arrow", "Golden Arrow Rain", "long range", "zoning ult",
                102, 114, 12, 18, 0, 6, 550, 1700, 1700, 5100, 5, 9));
        list.add(new FighterStats("Lixcia", 225, 5.2, 7, 12, 14, 20, 27, 112, 22, 55,
                "Backdoor Slash", "Phantom Audit", "combo refund", "burst finisher",
                80, 92, 11, 17, 7, 11, 500, 1900, 1450, 4950, 9, 14));
        list.add(new FighterStats("Manny Bangay", 280, 3.5, 11, 20, 11, 21, 32, 90, 26, 62,
                "Budget Slam", "Pork Barrel Crusher", "grappler shove", "huge knockback",
                70, 84, 20, 30, 0, 4, 300, 1500, 2200, 5700, 3, 5));
        list.add(new FighterStats("Marytes", 240, 4.4, 8, 14, 12, 22, 30, 110, 24, 58,
                "Mana Burst", "Arcane Ombudsman", "spell siphon", "mana storm",
                92, 100, 12, 18, 5, 12, 800, 2200, 1750, 5300, 10, 16));
        return list;
    }
}
