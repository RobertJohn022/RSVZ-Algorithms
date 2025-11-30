import java.awt.*;
import java.util.ArrayList;
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
        // ========================| Frame Setup |======================== //
        frame = new JFrame("Shortest Job First Scheduling - Robert Zuñiga");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        bodyPanel = new JPanel();
        frame.add(bodyPanel, BorderLayout.CENTER);

        // ========================| Control Panel GUI |======================== //
        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Add Row");
        JButton btnRemove = new JButton("Remove Row");
        JButton btnCalc = new JButton("Simulate");

        controls.add(btnAdd);
        controls.add(btnRemove);
        controls.add(btnCalc);

        frame.add(controls, BorderLayout.NORTH);

        // ========================| Table Display |======================== //
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
        scrollPane.setPreferredSize(new Dimension(600,214));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        // ========================| Average Display |======================== //
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

        // ========================| Return Button |======================== //
        JButton btnReturn = new JButton("Return to Main Menu");
        btnReturn.setFocusable(false);
        btnReturn.addActionListener(e -> {
            new selection();
            frame.dispose();
        });
        frame.add(btnReturn, BorderLayout.SOUTH);

        // ========================| Button Actions |======================== //
        btnAdd.addActionListener(e -> addRow());

        btnRemove.addActionListener(e -> {
            int count = model.getRowCount();

            // Remove up to 2 Rows
            if (count <= 2) {
                JOptionPane.showMessageDialog(frame, 
                        "Please include at least 2 processes", 
                        "Minimum Processes Reached!", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            model.removeRow(count - 1);
        });

        // Call CalculateSJF() Function
        btnCalc.addActionListener(e -> calculateSJF());

        frame.setVisible(true);
    }

    // Add up to 12 rows
    private void addRow() {
        int count = model.getRowCount();

        if (count >= 12) {
            JOptionPane.showMessageDialog(frame, 
                    "Please limit the number of processes to 12", 
                    "Maximum Processes Reached!", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        model.addRow(new Object[]{"P" + (count + 1), "", "", "", "", ""});
    }

    // ========================| Calculate SJF |======================== //
    private void calculateSJF() {

        // Get number of rows
        int rows = model.getRowCount();
        int[] arrival = new int[rows];
        int[] burst = new int[rows];

        try {
            // Read table
            for (int i = 0; i < rows; i++) {
                String arrivalText = model.getValueAt(i, 1).toString().trim();
                String burstText = model.getValueAt(i, 2).toString().trim();

                // Check fpr empty values
                if (arrivalText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Please complete the Arrival Time (AT) columns", 
                            "Arrival Time Incomplete", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (burstText.isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Please complete the Burst Time (BT) columns", 
                            "Burst Time Incomplete", 
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Convert values
                arrival[i] = Integer.parseInt(arrivalText);
                burst[i] = Integer.parseInt(burstText);
            }
        } catch (NumberFormatException ex) {
            // Check if correct format
            JOptionPane.showMessageDialog(frame,
                    "Only whole numbers are allowed for Arrival and Burst Time.",
                    "Invalid Number Format",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        // Initialize arrays
        boolean[] completed = new boolean[rows];
        int[] completion = new int[rows];
        int[] tat = new int[rows];
        int[] wt = new int[rows];

        int time = 0, completedCount = 0;

        // List Gantt chart information
        ArrayList<Integer> execOrder = new ArrayList<>();
        ArrayList<Integer> startTimes = new ArrayList<>();
        ArrayList<Integer> endTimes = new ArrayList<>();

        // idle if no process at frame 0
        int earliestArrival = Integer.MAX_VALUE;
        for (int a : arrival) earliestArrival = Math.min(earliestArrival, a);

        if (earliestArrival > 0) {
            execOrder.add(-1); 
            startTimes.add(0);
            endTimes.add(earliestArrival);
            time = earliestArrival;
        }

        while (completedCount < rows) {
            // detect process with shortest burst time
            int shortest = -1;
            int minBurst = Integer.MAX_VALUE;

            for (int i = 0; i < rows; i++) {
                if (!completed[i] && arrival[i] <= time && burst[i] < minBurst) {
                    minBurst = burst[i];
                    shortest = i;
                }
            }

            // idle if no process
            if (shortest == -1) {
                time++;
                continue;
            }

            // execute proces
            execOrder.add(shortest);
            startTimes.add(time);
            time += burst[shortest];
            endTimes.add(time);

            completion[shortest] = time;
            tat[shortest] = time - arrival[shortest];
            wt[shortest] = tat[shortest] - burst[shortest];

            completed[shortest] = true;
            completedCount++;
        }

        // update table with result
        for (int i = 0; i < rows; i++) {
            model.setValueAt(completion[i], i, 3);
            model.setValueAt(tat[i], i, 4);
            model.setValueAt(wt[i], i, 5);
        }

        double avgCT = 0, avgTAT = 0, avgWT = 0;

        // copute averages
        for (int i = 0; i < rows; i++) {
            avgCT += completion[i];
            avgTAT += tat[i];
            avgWT += wt[i];
        }

        ansCT.setText(String.format("%.2f", avgCT / rows));
        ansTAT.setText(String.format("%.2f", avgTAT / rows));
        ansWT.setText(String.format("%.2f", avgWT / rows));

        // ========================| Gantt Chart Display |======================== //
        if (ganttPanel != null) bodyPanel.remove(ganttPanel);

        String[] processNames = new String[rows];
        for (int i = 0; i < rows; i++) processNames[i] = model.getValueAt(i, 0).toString();

        ganttPanel = new GanttChart(processNames, execOrder, startTimes, endTimes);

        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        ganttScroll.setPreferredSize(new Dimension(800, 100));
        ganttScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        bodyPanel.add(ganttScroll);
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    // ========================| Gantt Chart Function |======================== //
    class GanttChart extends JPanel {

        private String[] processes;
        private ArrayList<Integer> execOrder, start, end;

        public GanttChart(String[] processes, ArrayList<Integer> execOrder,
                          ArrayList<Integer> start, ArrayList<Integer> end) {

            this.processes = processes;
            this.execOrder = execOrder;
            this.start = start;
            this.end = end;

            int totalTime = end.get(end.size() - 1);
            setPreferredSize(new Dimension(totalTime * 60 + 50, 80));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int x = 20;
            int height = 30;

        for (int i = 0; i < execOrder.size(); i++) {

            int pIndex = execOrder.get(i);
            int width = (end.get(i) - start.get(i)) * 60;

            // Display idle
            if (pIndex == -1) {
                g.setColor(Color.WHITE);
                g.fillRect(x, 30, width, height);
                g.setColor(Color.BLACK);
                g.drawRect(x, 30, width, height);
                g.drawString("Idle", x + width / 3, 50);
                g.drawString(String.valueOf(start.get(i)), x - 5, 25);
                x += width;
                continue;
            }

            // Display proceses
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(x, 30, width, height);

            g.setColor(Color.BLACK);
            g.drawRect(x, 30, width, height);
            g.drawString(processes[pIndex], x + width / 3, 50);
            g.drawString(String.valueOf(start.get(i)), x - 5, 25);

            x += width;
        }

        g.drawString(String.valueOf(end.get(end.size() - 1)), x - 5, 25);
        }
    }
}