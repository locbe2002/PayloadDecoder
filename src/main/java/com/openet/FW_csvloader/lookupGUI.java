package components;
 
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.util.Random;
import java.util.StringTokenizer;

class HintTextField extends JTextField implements FocusListener {
    private String hint;
    private boolean showhint;

    public HintTextField(final String hintText) {
        super(hintText);
        hint = hintText;
        showhint = true;
        super.addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setText("");
            showhint=false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
      if(this.getText().isEmpty()) {
        super.setText(hint);
        showhint = true;
      }
    }

    @Override
    public String getText() {
        return showhint ? "" : super.getText();
    }
}

public class lookupGUI extends JPanel
                              implements ActionListener, 
                                         PropertyChangeListener {
 
    private JProgressBar progressBar;
    private JButton searchButton;
    private JButton openButton;
    private JTextArea taskOutput;
    private HintTextField lookupStringText;
    private Task task;
    private lookupJNI lookupJNIObject;
    private String filename;
    private lookupRunnable lookupRunnableObject;
    private JFileChooser fc;
 
    class Task extends SwingWorker<Void, Void> {
        private lookupJNI lookupJNIObject;
        public Task(lookupJNI object) {
            super();
            this.lookupJNIObject = object;
        }
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            Random random = new Random();
            int progress = 0;
            //Initialize progress property.
            setProgress(0);
            //Sleep for at least one second to simulate "startup".
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
            while (progress < 100) {
                //Sleep for up to one second.
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException ignore) {}
                //Make random progress.
                progress = lookupJNIObject.getProgress(lookupJNIObject.getIndex());
                setProgress(Math.min(progress, 100));
            }
            return null;
        }
        /*
         * Executed in event dispatch thread
         */
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            searchButton.setEnabled(true);
            taskOutput.append("Done!\n");
            progressBar.setVisible(false);
            progressBar.setIndeterminate(false);
        }
    }
 
    public lookupGUI() {
        super(new BorderLayout());
 
        //Create a file chooser
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        openButton = new JButton("Open");
        openButton.setActionCommand("open");
        openButton.addActionListener(this);

        //Create the demo's UI.
        searchButton = new JButton("Search");
        searchButton.setActionCommand("search");
        searchButton.addActionListener(this);
        searchButton.setEnabled(false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
 
        //Call setStringPainted now so that the progress bar height
        //stays the same whether or not the string is shown.
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
 
        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        lookupStringText = new HintTextField("Search MSISDN");
        lookupStringText.setMargin(new Insets(5,5,5,5));
 
        JPanel panel = new JPanel();
        
        panel.add(openButton);
        panel.add(searchButton);
        panel.add(progressBar);
        
        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        add(new JScrollPane(lookupStringText), BorderLayout.PAGE_END);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        lookupJNIObject = new components.lookupJNI();
    }
 
    /**
     * Invoked when the user presses the start button.
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                searchButton.setEnabled(false);
                File file = fc.getSelectedFile();
                this.filename = file.getAbsolutePath();
                progressBar.setIndeterminate(true);
                progressBar.setVisible(true);
                lookupJNIObject.init(this.filename);
                task = new Task(this.lookupJNIObject);
                task.addPropertyChangeListener(this);
                task.execute();
                lookupRunnableObject = new components.lookupRunnable(lookupJNIObject, this.filename);
                Thread t = new Thread(lookupRunnableObject);
                t.start();
            }
        } else if (evt.getSource() == searchButton) {
            String lookupString = lookupStringText.getText();
            if (!lookupString.isEmpty()) {
                StringTokenizer st = new StringTokenizer(lookupString, ",; ");
                while (st.hasMoreTokens()) {
                    String lookupToken = st.nextToken();
                    String ans = lookupJNIObject.lookup(lookupToken);
                    taskOutput.append(String.format("Search for %s : \n%s", lookupToken, ans));
                }
            }
        }
    }
 
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);
            taskOutput.append(String.format(
                        "Completed %d%% of task.\n", progress));
        }
    }

    public void cleanup() {
        if (lookupJNIObject != null) lookupJNIObject.cleanup();
    }
 
    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("lookupGUI");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                JFrame frame = (JFrame)e.getSource();
 
                int result = JOptionPane.showConfirmDialog(
                    frame,
                    "Are you sure you want to exit the application?",
                    "Exit Application",
                    JOptionPane.YES_NO_OPTION);
 
                if (result == JOptionPane.YES_OPTION) {
                    Container c = frame.getContentPane();
                    lookupGUI l  = (lookupGUI)c;
                    l.cleanup();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
            }
        });
 
        //Create and set up the content pane.
        JComponent newContentPane = new lookupGUI();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
