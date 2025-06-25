import javax.swing.*;
import java.awt.*;

public class GameSetupDialog extends JDialog {
    private JComboBox<String> boardSizeBox;
    private JRadioButton vsPlayerBtn, vsComputerBtn;
    private JButton startBtn;
    private boolean confirmed = false;

    private int selectedSize = 3;
    private boolean vsComputer = false;

    public GameSetupDialog(Frame parent) {
        super(parent, "Pengaturan Permainan", true);
        setLayout(new GridLayout(4, 1, 10, 10));
        setSize(300, 200);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(245, 236, 224));

        // Board Size
        JPanel sizePanel = new JPanel();
        sizePanel.setBackground(new Color(245, 236, 224));
        sizePanel.add(new JLabel("Pilih Ukuran Papan:"));
        boardSizeBox = new JComboBox<>(new String[]{"3 x 3", "4 x 4", "5 x 5"});
        sizePanel.add(boardSizeBox);

        // Opponent
        JPanel opponentPanel = new JPanel();
        opponentPanel.setBackground(new Color(245, 236, 224));
        vsPlayerBtn = new JRadioButton("Lawan Player");
        vsComputerBtn = new JRadioButton("Lawan Komputer");
        ButtonGroup group = new ButtonGroup();
        group.add(vsPlayerBtn);
        group.add(vsComputerBtn);
        vsPlayerBtn.setSelected(true);
        opponentPanel.add(vsPlayerBtn);
        opponentPanel.add(vsComputerBtn);

        // Start Button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 236, 224));
        startBtn = new JButton("Mulai Game");
        buttonPanel.add(startBtn);

        add(sizePanel);
        add(opponentPanel);
        add(buttonPanel);

        startBtn.addActionListener(e -> {
            selectedSize = boardSizeBox.getSelectedIndex() + 3;
            vsComputer = vsComputerBtn.isSelected();
            confirmed = true;
            dispose();
        });
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getSelectedSize() {
        return selectedSize;
    }

    public boolean isVsComputer() {
        return vsComputer;
    }
}
