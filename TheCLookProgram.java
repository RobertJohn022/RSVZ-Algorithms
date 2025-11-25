import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class TheCLookProgram extends JFrame {

    private JTextField headInput;
    private JTextField requestsInput;
    private JTextArea resultsArea;
    private Graph graph;
    private List<Integer> order = new ArrayList<>();

    public TheCLookProgram(){
        setVisible(true);
        setTitle("The C-Look Program - Verald Charl Vergara");
        
        //screen resolution
        setSize(700,800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //--------------Input Fields
        JPanel input = new JPanel(new GridLayout(3, 2, 5, 5));

        JPanel Headpan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Headpan.add(new JLabel("Starting Head Position:"));
        headInput = new JTextField(5);
        Headpan.add(headInput);
        input.add(Headpan);

        JPanel Reqpan = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Reqpan.add(new JLabel("Requests:"));
        requestsInput = new JTextField(15);
        Reqpan.add(requestsInput);
        input.add(Reqpan);

        JButton run = new JButton("Simulate C-LOOK");
        JPanel runpan = new JPanel(new FlowLayout(FlowLayout.CENTER));
        runpan.add(run);
        input.add(runpan);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.add(input);
        add(wrapper, BorderLayout.NORTH);

        //--------------Results 
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setPreferredSize(new Dimension(getWidth(), 80)); // Fixed height
        add(scrollPane, BorderLayout.CENTER);

        //----------------Graph
        graph = new Graph();
        graph.setPreferredSize(new Dimension(getWidth(), 500)); // Ensure graph has space
        add(graph, BorderLayout.SOUTH);

        //button function
        run.addActionListener(e -> goclook());

    }

    private void goclook() {
        try {
            int head = Integer.parseInt(headInput.getText().trim());
            
            //excludes the spaces from being processed in the requests input field
            String[] parts = requestsInput.getText().trim().split(" ");
            List<Integer> requests = new ArrayList<>();

            for (String p : parts){
                requests.add(Integer.parseInt(p));
            }

            order = clook(head, requests);
            graph.setorder(order);
            
            updateResultsArea();

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Invalid, try again.");
        }
    }

    private void updateResultsArea() {
        
        // total head movement
        int totalMovement = 0;
        int previous = order.get(0); // Start with head position
        
        for (int i = 1; i < order.size(); i++) {
            int current = order.get(i);
            totalMovement += Math.abs(current - previous);
            previous = current;
        }
        
        // display only total head movement and seek sequence
        String result = "Total Head Movement: " + totalMovement + "\n" +
                       "Seek Sequence: " + order;
        
        resultsArea.setText(result);
        
        graph.repaint();
    }

    //-----Clook Algo
    public static List<Integer> clook(int head, List<Integer> requests) {
        List<Integer> sorted = new ArrayList<>(requests);
        Collections.sort(sorted);
        
        List<Integer> above = new ArrayList<>();
        List<Integer> below = new ArrayList<>();

        for (int r : sorted) {
            if (r >= head) above.add(r);
            else below.add(r);
        }

        List<Integer> order = new ArrayList<>();
        order.add(head);
        order.addAll(above);
        order.addAll(below);

        return order;
    }

    //----------Graph screen 
    class Graph extends JPanel {

        private List<Integer> order = new ArrayList<>();

        public Graph() {
            setBackground(Color.WHITE);
        }

        public void setorder(List<Integer> order){
            this.order = order;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);

            if (order == null || order.isEmpty())
                return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2));

            int width = getWidth();
            int height = getHeight();

            int maxCylinder = Collections.max(order);
            int minCylinder = Collections.min(order);

            int padding = 50;
            int prevX = 0;
            int prevY = 0;
            
            
            
            for (int i=0; i < order.size(); i++){
                int cylinder = order.get(i);

                int x = padding + (int)(((double)(cylinder - minCylinder) / (maxCylinder - minCylinder)) *(width - 2 * padding));

                int y = padding + (int) ((double) i/(order.size() - 1) * (height - 2 * padding));

                g2.setColor(new Color(25, 118, 210));
                g2.fillOval(x - 5, y - 5, 10, 10);

                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(cylinder), x - 10, y - 10);
               
                if(i > 0){
                    g2.setColor(new Color(25, 118, 210));
                    g2.drawLine(prevX, prevY, x, y);
                }

                prevX = x;
                prevY = y;
            }

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Head Movement Graph", 20, 20);
        }
    }
}