import java.awt.*;
import javax.swing.*;

class selection {

    public selection() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(480,640);
        frame.setLocationRelativeTo(null);

        // ======================== HEADER ======================== //
        JPanel header = new JPanel();
        header.setBackground(Color.LIGHT_GRAY);
        header.setPreferredSize(new Dimension(0,50));
        JLabel title = new JLabel();
        title.setText("RSVZ Algorithm Menu");
        title.setFont(new Font("Arial", Font.PLAIN, 30));
        title.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        frame.add(header,BorderLayout.NORTH);
        header.add(title);

        // ======================== MEMBERS ======================== //
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(20));

        JLabel membersLabel = new JLabel("MEMBERS:", SwingConstants.CENTER);
        membersLabel.setFont(new Font("Arial", Font.BOLD, 16));
        membersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel Rodriguez = new JLabel();
        Rodriguez.setText("Rodriguez, Joshua");
        Rodriguez.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel Semacio = new JLabel();
        Semacio.setText("Semacio, Jouie");
        Semacio.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel Vergara = new JLabel();
        Vergara.setText("Vergara, Verald");
        Vergara.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel Zuniga = new JLabel();
        Zuniga.setText("Zuniga, Robert");
        Zuniga.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(membersLabel);
        mainPanel.add(Rodriguez);
        mainPanel.add(Semacio);
        mainPanel.add(Vergara);
        mainPanel.add(Zuniga);
        mainPanel.add(Box.createVerticalStrut(40));

        // ======================== SELECTION ======================== //
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(100, 60, 200, 60));

        JButton btnSJF = new JButton("Shortest Job First");
        btnSJF.setPreferredSize(new Dimension(100, 50));
        btnSJF.setFocusable(false);
        btnSJF.addActionListener(e -> {
            new SJF();
            frame.dispose();
        });

        JButton btnPriority = new JButton("Priority");
        btnPriority.setPreferredSize(new Dimension(100, 50));
        btnPriority.setFocusable(false);
        btnPriority.addActionListener(e -> {
            new PreemptivePriority();
            frame.dispose();
        });

        JButton btnCLOOK = new JButton("C-Look");
        btnCLOOK.setPreferredSize(new Dimension(100, 50));
        btnCLOOK.setFocusable(false);

        JButton btnCSCAN = new JButton("Circular Scan");
        btnCSCAN.setPreferredSize(new Dimension(100, 50));
        btnCSCAN.setFocusable(false);
        btnCSCAN.addActionListener(e -> {
            new CscanVisualizer();
            frame.dispose();
        });

        buttonPanel.add(btnSJF);
        buttonPanel.add(btnPriority);
        buttonPanel.add(btnCLOOK);
        buttonPanel.add(btnCSCAN);
        
        // ======================== FRAME ======================== //
        mainPanel.add(buttonPanel);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    
}