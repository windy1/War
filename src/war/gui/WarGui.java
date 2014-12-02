package war.gui;

import war.Card;
import war.Player;
import war.WarModel;
import war.WarView;

import javax.swing.*;
import java.awt.*;

/**
 * Assignment #10
 * GUI Implementation of "War" card game.
 *
 * @author Walker Crouse
 */
public class WarGui extends JPanel implements WarView, Runnable {
    public static final String FRAME_TITLE = "War!";
    private static final boolean AUTO_PLAY = true;
    private final WarModel model = new WarModel(this);
    private final HeaderPanel header = new HeaderPanel();
    private final TablePanel table = new TablePanel();
    private final ControlPanel controls = new ControlPanel(model);

    /**
     * Creates and initializes the game.
     */
    public WarGui() {
        super(new BorderLayout());
        // add message
        add(header, BorderLayout.NORTH);
        // add playing table
        add(table, BorderLayout.CENTER);
        // add control panel
        add(controls, BorderLayout.SOUTH);
    }

    private void autoPlay() {
        new Thread(() -> {
            while (!model.isGameOver()) {
                JButton drawBtn = controls.getDrawButton();
                JButton mBtn = controls.getMobilizeButton();
                if (drawBtn.isEnabled())
                    drawBtn.doClick();
                else
                    mBtn.doClick();
            }
        }).start();
    }

    @Override
    public void run() {
        // create window
        JFrame frame = new JFrame(FRAME_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // add content
        frame.setContentPane(this);

        // display window
        frame.pack();
        frame.setVisible(true);

        // automatically start a new game
        model.newGame();

        if (AUTO_PLAY)
            autoPlay();
    }

    @Override
    public void onGameStart() {
        // enable draw button and update stats
        controls.getDrawButton().setEnabled(true);
        controls.updateStats();

        // clear board
        table.reset();

        // set welcome message
        header.setMessage(HeaderPanel.MESSAGE_WELCOME);
    }

    @Override
    public void onTurnStart(Card card1, Card card2) {
        // clear "war panel" if displayed
        if (!model.isWar()) {
            table.reset();
        }

        // show drawn cards
        table.showCards(card1, card2);

        // remove deck image if player is on last card
        table.getPlayerPanel(true).getDeck().setVisible(model.getPlayer(true).hasCard());
        table.getPlayerPanel(false).getDeck().setVisible(model.getPlayer(false).hasCard());
    }

    @Override
    public void onWarStart() {
        // set title
        header.setMessage(FRAME_TITLE);
        // disable draw button
        controls.getDrawButton().setEnabled(false);
        // enable mobilize button
        controls.getMobilizeButton().setEnabled(true);
        // update stats
        controls.updateStats();
    }

    private Card mobilized1, mobilized2;

    @Override
    public void onMobilize(Card card1, Card card2) {
        // save cards for later
        mobilized1 = card1;
        mobilized2 = card2;

        // hide "battle" pane
        PlayerPanel p1 = table.getPlayerPanel(true), p2 = table.getPlayerPanel(false);
        p1.getBattlePanel().setVisible(false);
        p2.getBattlePanel().setVisible(false);

        // show card back in "war" pane
        p1.getWarPanel().showBack();
        p2.getWarPanel().showBack();

        // disable mobilize button and enable draw button
        controls.getMobilizeButton().setEnabled(false);
        controls.getDrawButton().setEnabled(true);
        controls.updateStats();
    }

    @Override
    public void onWarEnd() {
        // reveal hidden cards
        table.getPlayerPanel(true).getWarPanel().showCard(mobilized1);
        table.getPlayerPanel(false).getWarPanel().showCard(mobilized2);
    }

    @Override
    public void onTurnEnd(Card card1, Card card2, Player winner) {
        // set winner message
        header.setMessage(HeaderPanel.MESSAGE_TURN_OVER, winner.getName());
        controls.updateStats();
    }

    @Override
    public void onGameOver(Player winner) {
        header.setMessage(HeaderPanel.MESSAGE_GAME_OVER, winner.getName());
        controls.getDrawButton().setEnabled(false);
        controls.updateStats();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new WarGui());
    }
}
