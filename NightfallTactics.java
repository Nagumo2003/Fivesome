package models;
import models.*;
import models.observer.GameObserver;
import javax.swing.*;
import java.awt.*;

public class NightfallTactics extends JFrame implements GameObserver {
    private CardLayout cl = new CardLayout();
    private JPanel container = new JPanel(cl);
    private GameEngine engine = new GameEngine();
    private JProgressBar p1HP = new JProgressBar(), p1MP = new JProgressBar();
    private JProgressBar p2HP = new JProgressBar(), p2MP = new JProgressBar();
    private JTextArea log = new JTextArea(15, 45);
    private JButton[] btns = new JButton[3];

    public NightfallTactics() {
        engine.addObserver(this);
        setTitle("NIGHTFALL TACTICS");
        setSize(950, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        JPanel menu = new JPanel(new GridLayout(5, 2));
        String[] roster = {"Christopher", "Punch", "Jason Tulfo", "Wilson", "Earl", "Sana", "Papi Robinhood", "Lixcia", "Manny Bangay", "Marytes"};
        for(String n : roster) {
            JButton b = new JButton(n);
            b.addActionListener(e -> { engine.selectHero(n); updateUI(); cl.show(container, "ARENA"); });
            menu.add(b);
        }
        
        JPanel arena = new JPanel(new BorderLayout());
        JPanel stats = new JPanel(new GridLayout(1, 2));
        stats.add(new JLabel("P1 HP/MP")); stats.add(new JLabel("P2 HP/MP"));
        arena.add(stats, BorderLayout.NORTH);
        arena.add(new JScrollPane(log), BorderLayout.CENTER);
        JPanel skillPanel = new JPanel();
        for(int i=0; i<3; i++) { int id=i; btns[i] = new JButton("Skill"); btns[i].addActionListener(e -> engine.handleAction(id)); skillPanel.add(btns[i]); }
        arena.add(skillPanel, BorderLayout.SOUTH);
        
        container.add(menu, "MENU"); container.add(arena, "ARENA");
        add(container); setVisible(true);
    }
    private void updateUI() { for(int i=0; i<3; i++) btns[i].setText(engine.getP1().getSkillName(i)); }
    public void onStateChanged() {}
    public void onLogUpdate(String m) { log.append("> " + m + "\n"); }
    public void onGameOver(String m) { JOptionPane.showMessageDialog(this, m); cl.show(container, "MENU"); }
    public static void main(String[] args) { new NightfallTactics(); }
}