/*
 * Purpose: To create the PhysicsAPI Editor Window that is able to define objects by the user, simulate them, and save them.
 * Original Creation Date: January 11 2016
 * @author Emilio Kartono
 * @version January 15 2016
 */

package com.javaphysicsengine.gui.editor;

import com.javaphysicsengine.api.PWorld;
import com.javaphysicsengine.api.body.PBody;
import com.javaphysicsengine.api.body.PCircle;
import com.javaphysicsengine.api.body.PConstraints;
import com.javaphysicsengine.api.body.PPolygon;
import com.javaphysicsengine.api.body.PSpring;
import com.javaphysicsengine.api.body.PString;
import com.javaphysicsengine.gui.codegenerator.PCodeGenerator;
import com.javaphysicsengine.gui.editor.canvas.PEditorMouseHandler;
import com.javaphysicsengine.gui.editor.canvas.PEditorPanel;
import com.javaphysicsengine.gui.editor.canvas.PEditorRenderer;
import com.javaphysicsengine.gui.editor.properties.PBodyPropertiesPanel;
import com.javaphysicsengine.gui.editor.store.PEditorObservableStore;
import com.javaphysicsengine.gui.io.PFileReader;
import com.javaphysicsengine.gui.io.PFileWriter;
import com.javaphysicsengine.gui.simulation.PSimulationWindow;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.javaphysicsengine.gui.editor.canvas.PEditorPanel.EDIT_MODE_CURSOR;

public class PEditorFrame extends JFrame implements ActionListener {
    private PEditorPanel editorPanel = null;
    private JTabbedPane propertiesPane;
    private PEditorObservableStore store;
    private PEditorMouseHandler mouseHandler;
    private PEditorRenderer renderer;

    /**
     * Creates a PEditorFrame window object
     */
    public PEditorFrame() {
        super("Physics API Editor");
        this.setFocusable(true);

        // Setting up the GUI components and link it to ActionListener
        setupMenuBar();
        addPanels();
    }

    /**
     * Post-condition: Starts the PhysicsAPI Editor window, and sets the look and feel
     */
    public static void main(String[] args) {
        // Setting the new look and feel of the window
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
        } catch (Exception ignored) {  }

        // Setting the look and feel of GUI components
        UIManager.put("nimbusBase", new Color(50, 50, 50));
        UIManager.put("nimbusBlueGrey", new Color(50, 50, 50));
        UIManager.put("control", new Color(50, 50, 50));
        UIManager.put("TextField.background", new Color(40, 40, 40));
        UIManager.put("TextField.foreground", new Color(150, 150, 150));
        UIManager.put("OptionPane.messagebackground", new Color(200, 200, 200));
        UIManager.put("OptionPane.foreground", new Color(200, 200, 200));

        // Create the window with properties
        PEditorFrame newWindow = new PEditorFrame();
        newWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newWindow.setSize(1000, 600);
        newWindow.setResizable(true);
        newWindow.setVisible(true);
    }

    /**
     * Adds the properties pane and the gui pane onto the window
     */
    private void addPanels() {
        // Set up the properties pane
        propertiesPane = new JTabbedPane();
        propertiesPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // Set up the pane where users can draw on it
        store = new PEditorObservableStore();
        store.getAddBodyListeners().add(body -> propertiesPane.add(body.getName(), new JScrollPane(new PBodyPropertiesPanel(body, propertiesPane, store))));

        mouseHandler = new PEditorMouseHandler(store, EDIT_MODE_CURSOR);
        renderer = new PEditorRenderer(store);
        editorPanel = new PEditorPanel(store, mouseHandler, renderer);

        store.getDeleteBodyListeners().add(objectName -> {
            // Close the properties tab that shows the properties of the delete object
            for (int i = 0; i < propertiesPane.getTabCount(); i++) {
                String label = propertiesPane.getTitleAt(i);
                if (label.equals(objectName))
                    propertiesPane.remove(i);
            }
        });

        store.getClearBodiesListeners().add(() -> {
            propertiesPane.removeAll();
        });

        store.getSelectedBodyListeners().add(newSelectedBody -> {
            System.out.println(newSelectedBody);

            if (newSelectedBody != null) {
                boolean isBodyInPropertiesPane = false;
                for (int i = 0; i < propertiesPane.getTabCount(); i++) {
                    String label = propertiesPane.getTitleAt(i);
                    if (label.equals(store.getSelectedBody().getName()))
                        isBodyInPropertiesPane = true;
                }

                // Create a properties tab for that body
                if (!isBodyInPropertiesPane) {
                    propertiesPane.add(store.getSelectedBody().getName(), new JScrollPane(new PBodyPropertiesPanel(store.getSelectedBody(), propertiesPane, store)));
                }
            }
        });

        store.getChangeBodyNameListeners().add((bodyWithNewName, oldName) -> {
            String newName = bodyWithNewName.getName();

            // Change the title of the body's properties pane
            for (int i = 0; i < propertiesPane.getTabCount(); i++) {
                if (propertiesPane.getTitleAt(i).equals(oldName))
                    propertiesPane.setTitleAt(i, newName);
            }
        });


        // Create the split pane to divide the window pane to two
        JSplitPane windowSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, propertiesPane);
        windowSplitPanel.setResizeWeight(0.7);  //  Makes the right side smaller than the left side

        // Add the split pane to the window
        JPanel windowPanel = new JPanel();
        windowPanel.setLayout(new BorderLayout());
        windowPanel.add(windowSplitPanel, BorderLayout.CENTER);
        this.getContentPane().add(windowPanel);
    }

    /**
     * Creates the Menu Bar with its menu buttons on the window
     */
    private void setupMenuBar() {
        // An array storing all the JMenuItems and JMenu and all that inherit from it
        JMenuItem[] menuItems = new JMenuItem[9];
        JMenuItem[] menus = new JMenu[4];

        // Create the "File" menu tab
        menuItems[0] = new JMenuItem("New");
        menuItems[1] = new JMenuItem("Load");
        menuItems[2] = new JMenuItem("Save");
        menus[0] = new JMenu("File");
        for (int i = 0; i < 3; i++)
            menus[0].add(menuItems[i]);

        // Creating the "Options" menu tab
        menuItems[3] = new JCheckBoxMenuItem("Toggle Anti-Aliasing");
        menus[1] = new JMenu("Options");
        menus[1].add(menuItems[3]);

        // Create the "Run" menu tab
        menuItems[4] = new JMenuItem("Run Simulation");
        menus[2] = new JMenu("Run");
        menus[2].add(menuItems[4]);

        // Create the "View" menu tab
        menuItems[5] = new JCheckBoxMenuItem("View Bounding Box");
        ((JCheckBoxMenuItem) menuItems[5]).setState(true);
        menuItems[6] = new JCheckBoxMenuItem("View Shape Outline");
        ((JCheckBoxMenuItem) menuItems[6]).setState(true);
        menuItems[7] = new JCheckBoxMenuItem("View Shape Fill");
        menuItems[8] = new JMenuItem("View Generated Java Code");
        menus[3] = new JMenu("View");
        for (int i = 5; i < menuItems.length; i++)
            menus[3].add(menuItems[i]);

        // Add the menus to the menu bar
        JMenuBar menuBar = new JMenuBar();
        for (JMenuItem menu : menus) {
            menuBar.add(menu);
        }
        this.setJMenuBar(menuBar);

        for (JMenuItem menu : menuItems) {
            menu.addActionListener(this);
        }
    }

    /**
     * It will save the bodies from the gui to a file.
     * It will show a prompt to the user asking for where to save the file.
     * Note that it must be a full file path.
     */
    private void loadBodiesFromFile() {
        // Get the file path from user
        String filePath = JOptionPane.showInputDialog("Enter File Path");
        try {
            InputStream inputStream = new FileInputStream(filePath);

            PFileReader fileReader = new PFileReader(inputStream);
            List<PBody> bodies = fileReader.getBodies();
            List<PConstraints> constraints = fileReader.getConstraints();

            // Adding the bodies and constraints to the gui
            for (PBody body : bodies) {
                if (body != null) {
                    store.addBody(body);
                    propertiesPane.add(body.getName(), new JScrollPane(new PBodyPropertiesPanel(body, propertiesPane, store)));
                }
            }
            for (PConstraints constraint : constraints) {
                if (constraint != null) {
                    store.addConstraint(constraint);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * It will pop up a dialog window asking the user to enter the file path
     * that will save the bodies to.
     * Note: the file path must be a full file path.
     */
    private void saveBodiesToFile() {
        String filePath = JOptionPane.showInputDialog("Enter File Path:");

        try {
            FileOutputStream fileWriter = new FileOutputStream(filePath);

            PFileWriter pFileWriter = new PFileWriter(fileWriter);
            pFileWriter.saveBodies(store.getCreatedBodies());
            pFileWriter.saveConstraints(store.getCreatedConstraints());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * View the java code generated from the bodies made in the gui.
     * It will open up a window with the code.
     */
    private void viewJavaCode() {
        List<PBody> bodies = store.getCreatedBodies();
        List<PConstraints> constraints = store.getCreatedConstraints();
        PCodeGenerator codeGenerator = new PCodeGenerator();
        List<String> codeLines = codeGenerator.generateApiCode(bodies, constraints);

        // Add them to the text area
        StringBuilder singularLine = new StringBuilder();
        for (String line : codeLines) {
            singularLine.append(line)
                    .append("\n");
        }
        JTextArea textArea = new JTextArea(singularLine.toString());

        // Show the info to a JOptionPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        JOptionPane.showMessageDialog(null, scrollPane, "Bodies Summary", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Gets the bodies created in the editorPanel, opens a new window, and simulates it
     */
    private void runSimulation() {
        PWorld world = new PWorld();

        for (PBody body : store.getCreatedBodies()) {
            PBody copiedBody;

            if (body instanceof PPolygon) {
                copiedBody = new PPolygon((PPolygon) body);
            } else if (body instanceof PCircle) {
                copiedBody = new PCircle((PCircle) body);
            } else {
                throw new IllegalArgumentException("The class type " + body.getClass() + " is not supported!");
            }
            world.getBodies().add(copiedBody);
        }

        for (PConstraints constraint : store.getCreatedConstraints()) {
            PBody attachedBody1 = world.getBodies().stream()
                    .filter(body -> body.getName().equals(constraint.getAttachedBodies()[0].getName()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Cannot find body in world!"));

            PBody attachedBody2 = world.getBodies().stream()
                    .filter(body -> body.getName().equals(constraint.getAttachedBodies()[1].getName()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Cannot find body in world!"));

            // Making a copy of the constraints
            if (constraint instanceof PSpring) {
                PSpring springCopy = new PSpring(attachedBody1, attachedBody2);
                springCopy.setKValue(((PSpring) constraint).getKValue());
                springCopy.setLength(constraint.getLength());
                world.getConstraints().add(springCopy);

            } else if (constraint instanceof PString) {
                PString stringCopy = new PString(attachedBody1, attachedBody2);
                stringCopy.setLength(constraint.getLength());
                world.getConstraints().add(stringCopy);
            }
        }

        new PSimulationWindow(world, 64, renderer.isShapeFillDisplayed(),
                renderer.isShapeOutlineDisplayed(), renderer.isAntiAliasingToggled())
                .setVisible(true);
    }

    /**
     * Certain actions called when clicked on a Menu Item in the Menu Bar of the window
     * @param e the event triggered
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JCheckBoxMenuItem) {
            JCheckBoxMenuItem curItem = (JCheckBoxMenuItem) e.getSource();
            switch (curItem.getText()) {
                case "Toggle Anti-Aliasing":
                    System.out.println("Toggled Anti-Aliasing");
                    renderer.setAntiAliasingToggled(curItem.isSelected());
                    break;
                case "View Bounding Box":
                    renderer.setBoundingBoxDisplayed(curItem.getState());
                    System.out.println("Displayed Bounding Box");
                    break;
                case "View Shape Outline":
                    renderer.setShapeOutlineDisplayed(curItem.getState());
                    System.out.println("Displayed Shape Outline");
                    break;
                case "View Shape Fill":
                    renderer.setShapeFillDisplayed(curItem.getState());
                    System.out.println("Displayed Shape Fill");
                    break;
            }
        } else if (e.getSource() instanceof JMenuItem) {
            JMenuItem curItem = (JMenuItem) e.getSource();
            switch (curItem.getText()) {
                case "New":
                    System.out.println("Created a new file");
                    store.clearBodies();
                    store.clearConstraints();
                    propertiesPane.removeAll();
                    break;
                case "Load":
                    System.out.println("Opened a file");
                    loadBodiesFromFile();
                    break;
                case "Save":
                    System.out.println("Saved file");
                    saveBodiesToFile();
                    break;
                case "Run Simulation":
                    System.out.println("Ran Simulation");
                    runSimulation();
                    break;
                case "View Generated Java Code":
                    System.out.println("Displayed Java Code");
                    viewJavaCode();
                    break;
            }
        }
    }
}