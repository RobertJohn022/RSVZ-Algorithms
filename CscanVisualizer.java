import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
CSCAN ALGO SIMULAToR START
 */
public class CscanVisualizer {
    private JFrame frame;

    //call main class
    public CscanVisualizer() {
        SwingUtilities.invokeLater(CscanVisualizer::createAndShowUi);
        frame.setVisible(true);
    }

    //UI STUFF STARTTTT

    private static void createAndShowUi() {
        JFrame frame = new JFrame("C-SCAN Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        InputPanel inputPanel = new InputPanel();
        frame.add(inputPanel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


//UI INPUT PANEL TOP START
//===============================================================================================
    private static class InputPanel extends JPanel {
        private final JTextField minField = new JTextField("0", 5);
        private final JTextField maxField = new JTextField("199", 5);
        private final JTextField headField = new JTextField("53", 5);
        private final JTextField requestsField = new JTextField("98,183,37,122,14,124,65,67", 30);
        private final JTextArea outputArea = new JTextArea(5, 40);
        private final TrackPanel trackPanel = new TrackPanel();

        InputPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(4, 4, 4, 4);

            form.add(new JLabel("Minimum Cylinder:"), gbc);
            gbc.gridx = 1;
            form.add(minField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Maximum Cylinder:"), gbc);
            gbc.gridx = 1;
            form.add(maxField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Initial Head:"), gbc);
            gbc.gridx = 1;
            form.add(headField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
            form.add(new JLabel("Request Sequence:"), gbc);
            gbc.gridx = 1;
            form.add(requestsField, gbc);

            JButton simulateButton = new JButton("Simulate C-SCAN");
            simulateButton.addActionListener(e -> runSimulation());

            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            topPanel.add(form, BorderLayout.CENTER);
            topPanel.add(simulateButton, BorderLayout.EAST);

            outputArea.setEditable(false);
            outputArea.setLineWrap(true);
            outputArea.setWrapStyleWord(true);

            add(topPanel, BorderLayout.NORTH);
            add(new JScrollPane(outputArea), BorderLayout.CENTER);

            JPanel visualizationPanel = new JPanel(new BorderLayout());
            visualizationPanel.setBorder(BorderFactory.createTitledBorder("Head Movement"));
            JScrollPane trackScrollPane = new JScrollPane(trackPanel);
            trackScrollPane.setBorder(null);
            visualizationPanel.add(trackScrollPane, BorderLayout.CENTER);

            add(visualizationPanel, BorderLayout.SOUTH);
        }

//UI INPUT PANEL TOP END
//===============================================================================================


//RUN MAIN SIMULATION CALL CSCAN SIMULATOR make sure user input correctly 
        private void runSimulation() {
            try {

                // get the user input from above the jtextfield panels and parse it to ints
                int min = Integer.parseInt(minField.getText().trim());
                int max = Integer.parseInt(maxField.getText().trim());
                int head = Integer.parseInt(headField.getText().trim());

                //check valid
                if (min >= max) {
                    showError("Minimum cylinder must be less than maximum cylinder.");
                    return;
                }
                if (head < min || head > max) {
                    showError("Initial head must be between minimum and maximum cylinder.");
                    return;
                }

                List<Integer> requests = parseRequests(requestsField.getText());
                for (int request : requests) {
                    if (request < min || request > max) {
                        showError("Requests must be between minimum and maximum cylinder.");
                        return;
                    }
                }

                //call the scan simulator, place inputs from above
                CSCANResult result = CSCANSimulator.simulate(min, max, head, requests);

                //update the output area with the results
                outputArea.setText(result.describe());
                trackPanel.updateData(min, max, result.getVisitOrder());
                
                //error catching
            } catch (NumberFormatException ex) {
                showError("Please enter valid integer values.");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        }

        private List<Integer> parseRequests(String text) {
            if (text.isBlank()) {
                throw new IllegalArgumentException("Request sequence cannot be empty.");
            }
            String[] tokens = text.split("[,\\s]+");
            List<Integer> values = new ArrayList<>();
            for (String token : tokens) {
                values.add(Integer.parseInt(token.trim()));
            }
            return values;
        }

        //error message popup
        private void showError(String message) {
            JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //UI CONTAINER END

    /**
     * Custom component that renders the head movement path vertically, allowing
     * long sequences to be scrolled.
     */
    private static class TrackPanel extends JPanel {
        private List<Integer> visitOrder = List.of();
        private int min = 0;
        private int max = 199;

        TrackPanel() {
            setPreferredSize(new Dimension(600, 250));
        }

        void updateData(int min, int max, List<Integer> visitOrder) {
            this.min = min;
            this.max = max;
            this.visitOrder = visitOrder;
            revalidate();
            repaint();
        }



//UI BOTTOM PAINT LINES START
//=====================================================================================================

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (visitOrder.isEmpty() || max == min) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int paddingX = 60;
            int paddingY = 30;
            int width = Math.max(1, getWidth() - 2 * paddingX);
            int startY = paddingY + 10;
            int stepY = 25;

            int requiredHeight = Math.max(200, paddingY + visitOrder.size() * stepY + paddingY);
            Dimension preferred = getPreferredSize();
            if (preferred.height != requiredHeight) {
                setPreferredSize(new Dimension(preferred.width, requiredHeight));
                revalidate();
            }

            // Draw axis labels.
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("Cylinder range: " + min + " - " + max, paddingX, paddingY);

            // Draw the path moving downward.
            g2d.setColor(new Color(25, 118, 210));
            int prevX = -1;
            int prevY = -1;
            for (int i = 0; i < visitOrder.size(); i++) {
                int cylinder = visitOrder.get(i);
                int x = paddingX + (int) ((cylinder - min) / (double) (max - min) * width);
                int y = startY + i * stepY;

                // Reference line for each stop.
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawLine(paddingX, y, paddingX + width, y);

                g2d.setColor(new Color(25, 118, 210));
                g2d.fillOval(x - 4, y - 4, 8, 8);
                if (prevX != -1) {
                    g2d.drawLine(prevX, prevY, x, y);
                }

                // Cylinder label next to the point.
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(String.valueOf(cylinder), x + 6, y - 6);

                prevX = x;
                prevY = y;
            }

            g2d.dispose();
        }
    }

    /**
     CSCAN LOGIC STARRT
     */
    private static class CSCANSimulator {
        /**
         * Runs the algorithm and returns the full visit order plus summary stats.
         *
         * @param min      lowest cylinder on the disk
         * @param max      highest cylinder on the disk
         * @param head     current head position
         * @param requests pending requests (not sorted)
         */
        static CSCANResult simulate(int min, int max, int head, List<Integer> requests) {
            if (requests.isEmpty()) {
                throw new IllegalArgumentException("Request sequence cannot be empty.");
            }

            // Sort once so we can split into upward and downward scans.
            List<Integer> sorted = new ArrayList<>(requests);
            Collections.sort(sorted);

            List<Integer> upward = sorted.stream()
                    .filter(r -> r >= head)
                    .collect(Collectors.toList());

            List<Integer> downward = sorted.stream()
                    .filter(r -> r < head)
                    .collect(Collectors.toList());

            // Will hold the path the head takes, including wrap points.
            List<Integer> visitOrder = new ArrayList<>();
            visitOrder.add(head);

            int totalSeek = 0;
            int current = head;

            // Serve requests moving toward the high end.
            for (int target : upward) {
                totalSeek += Math.abs(current - target);
                current = target;
                visitOrder.add(target);
            }

            if (!downward.isEmpty()) {
                // Jump to the max cylinder before wrapping.
                if (current != max) {
                    totalSeek += Math.abs(current - max);
                    current = max;
                    visitOrder.add(max);
                }
                // Wrap around from max to min (treated as a jump).
                totalSeek += Math.abs(current - min);
                current = min;
                visitOrder.add(min);

                // Continue servicing the remaining lower requests.
                for (int target : downward) {
                    totalSeek += Math.abs(current - target);
                    current = target;
                    visitOrder.add(target);
                }
            }

            // Average seek is total distance divided by number of serviced requests.
            double averageSeek = totalSeek / (double) requests.size();
            return new CSCANResult(visitOrder, totalSeek, averageSeek);
        }
    }

    /**
     * Holds the derived statistics and visit order from a C-SCAN run.
     */
    private static class CSCANResult {
        private final List<Integer> visitOrder;
        private final int totalSeek;
        private final double averageSeek;

        CSCANResult(List<Integer> visitOrder, int totalSeek, double averageSeek) {
            this.visitOrder = List.copyOf(visitOrder);
            this.totalSeek = totalSeek;
            this.averageSeek = averageSeek;
        }

        List<Integer> getVisitOrder() {
            return visitOrder;
        }

        String describe() {
            return String.format(
                    "Visit order: %s%nTotal seek distance: %d%nAverage seek distance: %.2f",
                    visitOrder,
                    totalSeek,
                    averageSeek);
        }
    }
}

