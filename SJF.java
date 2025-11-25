import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class SJF {
    private JFrame frame;
    private JPanel bodyPanel;
    private JPanel ganttPanel;
    private JLabel ansCT, ansTAT, ansWT;
    private JTable table;
    private DefaultTableModel model;

    public SJF() {
        // ========================| Frame |======================== //
        frame = new JFrame("SJF Scheduling");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        bodyPanel = new JPanel();
        frame.add(bodyPanel, BorderLayout.CENTER);

        // ========================| Control Panel |======================== //
        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Add Row");
        JButton btnRemove = new JButton("Remove Row");
        JButton btnCalc = new JButton("Calculate");

        controls.add(btnAdd);
        controls.add(btnRemove);
        controls.add(btnCalc);

        frame.add(controls, BorderLayout.NORTH);

        // ========================| Table |======================== //
        String[] columns = {"Process", "Arrival", "Burst", "Completion", "Turnaround", "Waiting"};
        model = new DefaultTableModel(columns, 0) {
            // Only Arrival and Burst editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2; 
            }
        };

        addRow();
        addRow();
        table = new JTable(model);

        // Make the rest non-editable
        table.getColumnModel().getColumn(0).setCellEditor(null);
        table.getColumnModel().getColumn(3).setCellEditor(null);
        table.getColumnModel().getColumn(4).setCellEditor(null);
        table.getColumnModel().getColumn(5).setCellEditor(null);

        // Add border
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        scrollPane.setPreferredSize(new Dimension(600,200));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        // ========================| Average |======================== //
        JPanel avgPanel = new JPanel();
        avgPanel.setPreferredSize(new Dimension(1920,60));
        JPanel results = new JPanel(new GridLayout(3,2));

        JLabel lblCT = new JLabel("Average CT: ");
        JLabel lblTAT = new JLabel("Average TAT: ");
        JLabel lblWT = new JLabel("Average WT: ");
        ansCT = new JLabel("0");
        ansTAT = new JLabel("0");
        ansWT = new JLabel("0");

        avgPanel.add(results);
        results.add(lblCT);  results.add(ansCT);
        results.add(lblTAT); results.add(ansTAT);
        results.add(lblWT);  results.add(ansWT);

        bodyPanel.add(avgPanel);
        //bodyPanel.add(GanttPanel);

        // ========================| Return Button |======================== //
        JButton btnReturn = new JButton("Return");
        btnReturn.setFocusable(false);
        btnReturn.addActionListener(e -> {
            new selection();
            frame.dispose();
        });
        frame.add(btnReturn, BorderLayout.SOUTH);

        // ========================| Button Actions |======================== //
        btnAdd.addActionListener(e -> addRow());

        btnRemove.addActionListener(e -> {
            if (model.getRowCount() > 0) {
                model.removeRow(model.getRowCount() - 1);
            }
        });

        btnCalc.addActionListener(e -> calculateSJF());

        frame.setVisible(true);
    }

    // Add Row
    private void addRow() {
        int count = model.getRowCount() + 1;
        model.addRow(new Object[]{"P" + count, "", "", "", "", ""});
    }

    // ========================| Calculate SJF |======================== //
    private void calculateSJF() {
        int rows = model.getRowCount();
        int[] arrival = new int[rows];
        int[] burst = new int[rows];

        try {
            for (int i = 0; i < rows; i++) {
                arrival[i] = Integer.parseInt(model.getValueAt(i, 1).toString());
                burst[i] = Integer.parseInt(model.getValueAt(i, 2).toString());
            }
        } catch (Exception e) {
            // ADD EXCEPTION
            return;
        }

        boolean[] completed = new boolean[rows];
        int[] completion = new int[rows];
        int[] tat = new int[rows];
        int[] wt = new int[rows];

        int time = 0, completedCount = 0;

        while (completedCount < rows) {
            int shortest = -1;
            int minBurst = Integer.MAX_VALUE;

            for (int i = 0; i < rows; i++) {
                if (!completed[i] && arrival[i] <= time && burst[i] < minBurst) {
                    minBurst = burst[i];
                    shortest = i;
                }
            }

            if (shortest == -1) {
                time++;
                continue;
            }

            time += burst[shortest];
            completion[shortest] = time;
            tat[shortest] = completion[shortest] - arrival[shortest];
            wt[shortest] = tat[shortest] - burst[shortest];
            completed[shortest] = true;
            completedCount++;
        }

        // Update Table
        for (int i = 0; i < rows; i++) {
            model.setValueAt(completion[i], i, 3);
            model.setValueAt(tat[i], i, 4);
            model.setValueAt(wt[i], i, 5);
        }

        // Copute averages
        double avgCT = 0, avgTAT = 0, avgWT = 0;
        for (int i = 0; i < rows; i++) {
            avgCT += completion[i];
            avgTAT += tat[i];
            avgWT += wt[i];
        }
        avgCT /= rows;
        avgTAT /= rows;
        avgWT /= rows;

        // Update labels
        ansCT.setText(String.format("%.2f", avgCT));
        ansTAT.setText(String.format("%.2f", avgTAT));
        ansWT.setText(String.format("%.2f", avgWT));

        

        // ========================| Gantt Chart |======================== //
        if (ganttPanel != null)
            bodyPanel.remove(ganttPanel);

        String[] processNames = new String[rows];
        int[] finalBurst = new int[rows];
        for (int i = 0; i < rows; i++) {
            processNames[i] = model.getValueAt(i, 0).toString();
            finalBurst[i] = burst[i];
        }

        ganttPanel = new GanttChart(processNames, arrival, completion);
        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        ganttScroll.setPreferredSize(new Dimension(600, 100));
        ganttScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bodyPanel.add(ganttScroll);
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    // ========================| Gantt Chart Class |======================== //
    class GanttChart extends JPanel {
    private String[] processes;
    private int[] arrival;
    private int[] completion;

    public GanttChart(String[] processes, int[] arrival, int[] completion) {
        this.processes = processes;
        this.arrival = arrival;
        this.completion = completion;

        int totalTime = completion[completion.length - 1];
        int pixelsPerUnit = 40;
        setPreferredSize(new Dimension(totalTime * pixelsPerUnit + 40, 80));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x = 20;
        int height = 30;
        int currentTime = 0;

        int totalTime = completion[completion.length - 1];
        double scale = (getWidth() - 40) / (double) totalTime; // dynamic scale

        // Draw initial idle if first process arrives after 0
        if (arrival[0] > 0) {
            int idleWidth = (int) ((arrival[0] - currentTime) * scale);
            g.setColor(Color.WHITE);
            g.fillRect(x, 30, idleWidth, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, 30, idleWidth, height);
            g.drawString("Idle", x + idleWidth / 3, 50);
            g.drawString(String.valueOf(currentTime), x - 5, 25);
            x += idleWidth;
            currentTime = arrival[0];
        }

        // Draw processes
        for (int i = 0; i < processes.length; i++) {
            int width = (int) ((completion[i] - currentTime) * scale);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, 30, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, 30, width, height);

            g.drawString(processes[i], x + width / 3, 50);
            g.drawString(String.valueOf(currentTime), x - 5, 25);

            x += width;
            currentTime = completion[i];
        }

        g.drawString(String.valueOf(currentTime), x - 5, 25);
        }
    }
}