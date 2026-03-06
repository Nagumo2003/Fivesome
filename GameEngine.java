package models;
import models.observer.GameObserver;
import java.util.*;
public class GameEngine {
    private Character p1, p2;
    private boolean p1Turn = true;
    private List<GameObserver> obs = new ArrayList<>();
    public void addObserver(GameObserver o) { obs.add(o); }
    public void selectHero(String name) { p1 = CharacterFactory.createHero(name); p2 = CharacterFactory.createHero("Jason Tulfo"); notifyUI(); }
    public void handleAction(int i) {
        try {
            Character a = p1Turn ? p1 : p2; Character t = p1Turn ? p2 : p1;
            String log = a.useSkill(i, t);
            obs.forEach(o -> o.onLogUpdate(log));
            if(!t.isAlive()) obs.forEach(o -> o.onGameOver(a.getName() + " Wins!"));
            p1Turn = !p1Turn; notifyUI();
        } catch(Exception e) { obs.forEach(o -> o.onLogUpdate("Alert: " + e.getMessage())); }
    }
    private void notifyUI() { obs.forEach(GameObserver::onStateChanged); }
    public Character getP1() { return p1; } public Character getP2() { return p2; }
}