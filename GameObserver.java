package models.observer;

public interface GameObserver {
    void onStateChanged();
    void onLogUpdate(String message);
    void onGameOver(String message);
}