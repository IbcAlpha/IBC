// This file is part of the "IBController".
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2011 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBController is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBController is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBController.  If not, see <http://www.gnu.org/licenses/>.

package ibcontroller;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;

class Utils {

    static final SimpleDateFormat _DateFormatter = new SimpleDateFormat("HH:mm:ss:SSS");
    
    // set these to the defaults, so that we can continue to use them 
    // even when TWS redirects System.out and System.err to its own logfile
    private static final PrintStream out = System.out;
    private static final PrintStream err = System.err;
    
    private static boolean sendConsoleOutputToTwsLog = false;
    
    private static String logComponents;

    static {
        String logComponentsSetting =  Settings.getString("LogComponents", "never").toLowerCase();
        switch (logComponentsSetting) {
            case "activate":
            case "open":
            case "never":
                logComponents = logComponentsSetting;
                break;
            case "yes":
            case "true":
                logComponents="open";
                break;
            case "no":
            case "false":
                logComponents="never";
                break;
            default:
                Utils.logError("the LogComponents setting is invalid.");
                break;
        }
    }

    /**
     * Performs a click on the button labelled with the specified text.
     * @param window
     *  The window containing the button.
     * @param buttonText
     *  The button's label.
     * @return
     *  true if the button was found;  false if the button was not found
     */
    static boolean clickButton(final Window window, final String buttonText) {
        final JButton button = findButton(window, buttonText);
        if (button == null) return false;

        if (! button.isEnabled()) {
            button.setEnabled(true);
            logToConsole("Button was disabled, has been enabled: " + buttonText);
        }

        logToConsole("Click button: " + buttonText);
        button.doClick();
        if (! button.isEnabled()) logToConsole("Button now disabled: " + buttonText);
        return true;
    }

    /**
     * Traverses a container hierarchy and returns the button with
     * the given text.
     * @param container
     *  the Container to search in
     * @param text
     *  the label of the button to be found
     * @return
     *  the button, if was found;  otherwise null
     */
    static JButton findButton(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JButton && text.equals(((JButton)component).getText())) return (JButton)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the checkbox with
     * the given text.
     * @param container
     *  the Container to search in
     * @param text
     *  the label of the checkbox to be found
     * @return
     *  the checkbox, if it was found;  otherwise null
     */
    static JCheckBox findCheckBox(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JCheckBox && text.equals(((JCheckBox)component).getText())) return (JCheckBox)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the ith JComboBox
     * (0 based indexing).
     *
     * @param container
     *  the Container to search in
     * @param ith
     *   specifies which JComboBox to return (the first one is specified by 0,
     * the next by 1, etc)
     * @return
     *  the required JComboBox if it is found, otherwise null
     */
    static JComboBox<?> findComboBox(Container container, int ith) {
        ComponentIterator iter = new ComponentIterator(container);
        int i = 0;
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JComboBox<?> && i++ == ith) return (JComboBox<?>)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the Component with
     * the given text.
     * @param container
     *  the Container to search in
     * @param text
     *  the label of the Component to be found
     * @return
     *  the Component, if it was found;  otherwise null
     */
    static Component findComponent(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (text.equals(component.getName())) return component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the radio button with
     * the given text.
     * @param container
     *  the Container to search in
     * @param text
     *  the label of the radio button to be found
     * @return
     *  the radio button, if it was found;  otherwise null
     */
    static JRadioButton findRadioButton(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JRadioButton && text.equals(((JRadioButton)component).getText())) return (JRadioButton)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the JTextLabel
     * that contains the given substring.
     * @param container
     *  the Container to search in
     * @param text
     *  the substring to find in a JLabel
     * @return
     *  the JLabel, if it was found;  otherwise null
     */
    static JLabel findLabel(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JLabel && ((JLabel)component).getText() != null &&  ((JLabel)component).getText().contains(text)) return (JLabel)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the ith JTextField
     * (0 based indexing).
     *
     * @param container
     *  the Container to search in
     * @param ith
     *   specifies which JTextField to return (the first one is specified by 0,
     * the next by 1, etc)
     * @return
     *  the required JTextField if it is found, otherwise null
     */
    static JTextField findTextField(Container container, int ith) {
        ComponentIterator iter = new ComponentIterator(container);
        int i = 0;
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JTextField && i++ == ith) return (JTextField)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the first JMenuBar
     * it finds.
     * @param container
     *  the Container to search in
     * @return
     * the first JMenuBar found, if any; otherwise null
     */
    static JMenuBar findMenuBar(Container container) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JMenuBar) return (JMenuBar)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the ith JMenuBar
     * (0 based indexing).
     *
     * @param container
     *  the Container to search in
     * @param ith
     *   specifies which JMenuBar to return (the first one is specified by 0,
     * the next by 1, etc)
     * @return
     *  the required JMenuBar if it is found, otherwise null
     */
    static JMenuBar findMenuBar(Container container, int ith) {
        ComponentIterator iter = new ComponentIterator(container);
        int i = 0;
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JMenuBar && i++ == ith) return (JMenuBar)component;
        }
        return null;
    }

    /**
     * Searches a MenuElement's subelements for the JMenuItem with
     * the given text.
     * @param container
     *  the MenuElement to search in
     * @param text
     *  the label of the JMenuItem to be found
     * @return
     *  the JMenuItem, if it was found;  otherwise null
     */
    static JMenuItem findMenuItem(MenuElement container, String text) {
        MenuElement[] elements = container.getSubElements();

        for (MenuElement element : elements) {
            if (element instanceof JMenuItem) {
                JMenuItem button = (JMenuItem) element;
                if (button.getText().equals(text)) {
                    return button;
                }
            } else {
                JMenuItem button = findMenuItem(element, text);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the JMenuItem with
     * the given path from the first JMenuBar encountered, or null if the 
     * first JMenuBar doesn't contain an item with that path
     *
     * @param container
     *  the Container to search in
     * @param path
     *  the required menu path
     * @return
     *  the JMenuItem at the specified path, if found; otherwise null
     */
    static JMenuItem findMenuItem(Container container, String[] path) {
        if (path.length == 0) return null;

        JMenuBar menuBar = findMenuBar(container);
        if (menuBar == null) return null;

        return findMenuItem(menuBar, path);
    }

    /**
     * Traverses a container hierarchy and returns the JMenuItem with
     * the given path from the first JMenuBar that contains it
     *
     * @param container
     *  the Container to search in
     * @param path
     *  the required menu path
     * @return
     *  the JMenuItem at the specified path, if found; otherwise null
     */
    static JMenuItem findMenuItemInAnyMenuBar(Container container, String[] path) {
        if (path.length == 0) return null;

        int i = 0;
        while (true) {
            JMenuBar menuBar = findMenuBar(container, i);
            if (menuBar == null) {
                String s = path[0];
                for (int j = 1; j < path.length; j++) s = s + " > " + path[j];
                return null;
            }
            JMenuItem menuItem = findMenuItem(menuBar, path);
            if (menuItem != null) return menuItem;
            i++;
        }
    }

    /**
     * Traverses a JMenubar's menu structure for a JMenuItem with
     * the specified path
     * @param menuBar
     * the JMenuBar to search
     * @param path
     * the required menu path
     * @return
     *  the JMenuItem at the specified path, if found; otherwise null
     */
    static JMenuItem findMenuItem(JMenuBar menuBar, String[] path) {
        if (path.length == 0) return null;

        MenuElement currentItem = menuBar;
        for (String pathElement : path) {
            currentItem = findMenuItem(currentItem, pathElement);
            if (currentItem == null) return null;
        }
        return (JMenuItem)currentItem;
    }

    /**
     * Traverses a container hierarchy and returns the first JOptionPane
     * it finds.
     * @param container
     *  the Container to search in
     * @return
     *  the first JOptionPane, if one was found;  otherwise null
     */
    static JOptionPane findOptionPane(Container container) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JOptionPane) return (JOptionPane)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the first JTree
     * it finds.
     * @param container
     *  the Container to search in
     * @return
     *  the first JTree, if one was found;  otherwise null
     */
    static JTree findTree(Container container) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JTree) return (JTree)component;
        }
        return null;
    }

    /**
     * Returns the node with the given text below the given node in the specified TreeModel
     * @param model
     *  the TreeModel to search
     * @param node
     *  the node to search below
     * @param text
     *  the text associated with the required node
     * @return
     * the required node, if found; otherwise null
     */
    static Object findChildNode(TreeModel model, Object node, String text) {
        for (int i = 0; i < model.getChildCount(node); i++) {
            Object currNode = model.getChild(node, i);
            if (currNode.toString() != null && currNode.toString().equals(text)) return currNode;
        }
        return null;
    }
    
    /**
     * Performs a click on the menu item at the specified path, waiting if necessary for the
     * menu item to become enabled.
     * 
     * If there is more than one menu bar within the specified container, they are searched
     * (in hierarchical containment order) until one is found that contains the specified menu item.
     * 
     * Note that this method may block the calling thread if the required menu item is currently disabled.
     * @param container
     * the Container to search in
     * @param path
     * the path of the required menu item
     * @return
     * true if the menu item was successfully clicked; false if the menu item could not be found
     * @throws  IllegalStateException 
     * the method has been called on the Swing event dispatch thread
     */
    static boolean invokeMenuItem(final Container container, final String[] path) throws IllegalStateException {
        if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Function must not be called on the event dispatch thread, as it may block the thread");
        while (true) {
            FutureTask<Boolean> task = new FutureTask<>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws IBControllerException {
                    String s = path[0];
                    for (int i = 1; i < path.length; i++) s = s + " > " + path[i];

                    JMenuItem menuItem = Utils.findMenuItemInAnyMenuBar(container, path);
                    if (menuItem == null) throw new IBControllerException("menu item: " + s);
                    if (!menuItem.isEnabled()) return false;
                    menuItem.doClick();
                    return true;
                }
            });

            GuiDeferredExecutor.instance().execute(task);
            
            try {
                if (task.get()) return true;
            } catch (InterruptedException e) {
                logError("invokeMenuItem task interrupted");
                return false;
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if (t instanceof IBControllerException) {
                    return false;
                }
                if (t instanceof RuntimeException) throw (RuntimeException)t;
                if (t instanceof Error) throw (Error)t;
            }
            
            pause(250);
        }
    }

    /**
     * Indicates whether the specified JButton is enabled.
     * @param window
     * the window in which to search for the required JButton
     * @param buttonText
     * the label of the required JButton
     * @return
     * true if the JButton is enabled; false if there is no such JButton, or it
     * is disabled
     */
    static boolean isButtonEnabled(final Window window, final String buttonText) {
        final JButton button = findButton(window, buttonText);
        if (button == null) return false;

        return button.isEnabled();
    }

    /**
     * Indicates whether the specified JCheckBox is selected.
     * @param window
     * the window in which to search for the required JCheckBox
     * @param buttonText
     * the label of the required JCheckBox
     * @return
     * true if the JCheckBox is enabled; false if there is no such JCheckBox, or it
     * is not selected
     */
    static boolean isCheckBoxSelected(Window window, String buttonText) {
        final JCheckBox cb = findCheckBox(window, buttonText);
        if (cb == null) return false;

        return cb.isSelected();
    }

    /**
     * Indicates whether the specified JRadioButton is selected.
     * @param window
     * the window in which to search for the required JRadioButton
     * @param buttonText
     * the label of the required JRadioButton
     * @return
     * true if the JRadioButton is enabled; false if there is no such JRadioButton, or it
     * is not selected
     */
    static boolean isRadioButtonSelected(Window window, String buttonText) {
        final JRadioButton rb = findRadioButton(window, buttonText);
        if (rb == null) return false;

        return rb.isSelected();

    }

    /**
     * Writes the structure of the specified Component to the console.
     * @param component
     * The Component to be logged
     */
    static void logComponent(Component component) {
        Utils.logComponent(component, "");
    }
    
    static void logError(String message) {
        getErrStream().println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        getErrStream().println(formatMessage(message));
        getErrStream().println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
    
    /**
     * Writes a plain text message to the console.
     * @param msg
     * The message to be written
     */
    static void logRawToConsole(String msg) {
        getOutStream().println(msg);
    }
    
    /**
     * Writes a text message prefixed with the current time to the console.
     * @param msg
     * The message to be written
     */
    static void logToConsole(String msg) {
        getOutStream().println(formatMessage(msg));
    }
    
    private static PrintStream getErrStream() {
        if (sendConsoleOutputToTwsLog) {
            return System.err;
        } else {
            return err;
        }
    }
    
    private static PrintStream getOutStream() {
        if (sendConsoleOutputToTwsLog) {
            return System.out;
        } else {
            return out;
        }
    }
    
    private static String formatMessage(String message) {
        return _DateFormatter.format(new Date()) + " IBController: " + message;
    }

    static void logWindow(Window window, int eventID) {
        String event = windowEventToString(eventID);

        if (window instanceof JFrame) {
            Utils.logToConsole("detected frame entitled: " + ((JFrame) window).getTitle() + "; event=" + event);
        } else if (window instanceof JDialog) {
            Utils.logToConsole("detected dialog entitled: " + ((JDialog) window).getTitle() + "; event=" + event);
        } else {
            Utils.logToConsole("detected window: type=" + window.getClass().getName() + "; event=" + event);
        }
        
        if ((eventID == WindowEvent.WINDOW_OPENED && logComponents.equals("open"))
            ||
            (eventID == WindowEvent.WINDOW_ACTIVATED && logComponents.equals("activate")))
        {
            Utils.logWindowComponents(window);
        }
    }
    
/**
     * Writes the structure of the specified window to the console.
     * 
     * Details of each component in the window are written, indented to reflect
     * the component's position in the hierarchy.
     * @param window
     * The Window whose structure is to be logged.
     */
    static void logWindowComponents(Window window) {
        for (Component component : window.getComponents()) logComponent(component, "");
    }
    
    /**
     * sleeps for millis milliseconds, approximately.
     * 
     * Note that this method swallows the InterruptedException that may
     * result from a call to sleep().
     * @param millis
     * the number of milliseconds to sleep for
     */
    static void pause(int millis) {
        try {
            Thread.sleep(millis); // sleep a bit before trying again.
        } catch (InterruptedException ie) {
        }
    }
    
    /**
     * Sets or clears the specified JCheckBox.
     * @param window
     * the window in which to search for the required JCheckBox
     * @param buttonText
     * the label for the required JCheckBox
     * @param value
     * true to set the JCheckBox; false to clear it
     * @return
     * true if the JCheckBox was found; otherwise false
     */
    static boolean setCheckBoxSelected(Window window, String buttonText, final boolean value) {
        final JCheckBox cb = findCheckBox(window, buttonText);
        if (cb == null) return false;
        cb.setSelected(value);
        return true;
    }
    
    static void showTradesLogWindow() {
            MyCachedThreadPool.getInstance().execute(new Runnable () {
                @Override public void run() {invokeMenuItem(MainWindowManager.getMainWindow(), new String[] {"Account", "Trade Log"});}
            });
    }
    
    static void sendConsoleOutputToTwsLog(boolean value) {
        sendConsoleOutputToTwsLog = value;
    }

    /**
     * Sets or clears the specified JRadioButton .
     * @param window
     * the window in which to search for the required JRadioButton 
     * @param buttonText
     * the label for the required JRadioButton 
     * @param value
     * true to set the JRadioButton ; false to clear it
     * @return
     * true if the JRadioButton  was found; otherwise false
     */
    static boolean setRadioButtonSelected(Window window, String buttonText) {
        final JRadioButton rb = findRadioButton(window, buttonText);
        if (rb == null) return false;

        if (rb.isSelected()) return true;

        rb.doClick();
        return true;
    }

    /**
     * Sets the specified JTextField to the given value.
     * @param window
     * the window in which to search for the JTextField
     * @param fieldNumber
     * the number of the required JTextField in the window, counting
     * from 0
     * @param value
     * the value to be set in the JTextField
     * @return
     * true if the required JTextField was found; otherwise false
     */
    static boolean setTextField(Window window, int fieldNumber, final String value) {
        final JTextField tf = findTextField(window, fieldNumber);
        if (tf != null) {
            tf.setText(value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Indicates whether the specified window's title contains the given string.
     * @param window
     * the window to be checked
     * @param text
     * the text to be searched for
     * @return
     * true if the window's title contains text, otherwise false
     */
    static boolean titleContains(Window window, String text) {
        String title = getWindowTitle(window);
        return (title != null && title.contains(text));
    }

    /**
     * Indicates whether the specified window's title is the same as the given string.
     * @param window
     * the window to be checked
     * @param text
     * the text to be searched for
     * @return
     * true if the window's title equals text, otherwise false
     */
    static boolean titleEquals(Window window, String text) {
        String title = getWindowTitle(window);
        return (title != null && title.equals(text));
    }

    static String windowEventToString(int eventID) {
        switch (eventID) { 
            case WindowEvent.WINDOW_ACTIVATED:
                return "Activated";
            case WindowEvent.WINDOW_CLOSED:
                return "Closed";
            case WindowEvent.WINDOW_CLOSING:
                return "Closing";
            case WindowEvent.WINDOW_DEACTIVATED:
                return "Deactivated";
            case WindowEvent.WINDOW_DEICONIFIED:
                return "Deiconfied";
            case WindowEvent.WINDOW_GAINED_FOCUS:
                return "Focused";
            case WindowEvent.WINDOW_ICONIFIED:
                return "Iconified";
            case WindowEvent.WINDOW_LOST_FOCUS:
                return "Lost focus";
            case WindowEvent.WINDOW_OPENED:
                return "Opened";
            case WindowEvent.WINDOW_STATE_CHANGED:
                return "State changed";
            default:
                return "???";
        }
    }

    private static String getComponentDetails(Component component) {
        String s = component.isEnabled() ? "" : "[Disabled]";
        if (component instanceof JButton) s = s + "JButton: " + ((JButton)component).getText();
        else if (component instanceof JCheckBox) s = s + "JCheckBox: " + ((JCheckBox) component).getText();
        else if (component instanceof JLabel) s = s + "JLabel: " + ((JLabel) component).getText();
        else if (component instanceof JOptionPane) s = s + "JOptionPane: " + ((JOptionPane) component).getMessage().toString();
        else if (component instanceof JRadioButton) s = s + "JRadioButton: " + ((JRadioButton) component).getText();
        else if (component instanceof JTextField) s = s + "JTextField: " + ((JTextField) component).getText();
        else if (component instanceof JMenuBar) s = s + "JMenuBar: " + ((JMenuBar) component).getName();
        else if (component instanceof JMenuItem) s = s + "JMenuItem: " + ((JMenuItem) component).getText();
        else if (component instanceof JTree) s = s + "JTree: ";
        else if (component instanceof JComboBox) s = s + "JComboBox: ";
        else if (component instanceof JList) s = s + "JList: ";
        
        if (!s.isEmpty()) s = "{" + s + "}";
        return s;
    }
    
    private static String getWindowTitle(Window window) {
        String title = null;
        if (window instanceof JDialog) {
            title = ((JDialog)window).getTitle();
        } else  if (window instanceof JFrame) {
            title =((JFrame)window).getTitle();
        }
        return title;
    }
    
    private static String logClassDerivation(Object object) {
        String s = object.getClass().getSimpleName();
        Class<?> c = object.getClass().getSuperclass();
        while (c != null) {
            s = c.getSimpleName() + "." + s;
            c = c.getSuperclass();
        }
        return s;
    }

    private static void logComponent(Component component, String indent) {
        logToConsole(indent + component.getName() + "(" + component.getClass().getName() + ")" + getComponentDetails(component));
        if (component instanceof JTree) logTreeNodes(((JTree) component).getModel(), ((JTree) component).getModel().getRoot(), "|   " + indent);
        if (component instanceof JMenuBar) {
            logMenuItem(component, "|   " + indent);
        } else if (component instanceof Container) {
            for (Component subComponent : ((Container)component).getComponents()) logComponent(subComponent,"|   " + indent);
        }
    }

    private static void logTreeNodes(TreeModel model, Object node, String indent) {
        if (node instanceof Component) {
            logToConsole(indent + node.toString());
            logComponent((Component)node, "|   " + indent);
        } else {
            logToConsole(indent + node.toString() + "  (" + logClassDerivation(node) + ")");
        }
        for (int i = 0; i < model.getChildCount(node); i++) logTreeNodes(model, model.getChild(node, i), "|   " + indent);
    }

    private static void logMenuItem(Component menuItem, String indent) {
        if (menuItem instanceof JMenuBar) {
            logMenuSubElements((MenuElement)menuItem, indent);
        } else if (menuItem instanceof JPopupMenu) {
            logMenuSubElements((MenuElement)menuItem, indent);
        } else if (menuItem instanceof JMenuItem) {
            logToConsole(indent + ((JMenuItem)menuItem).getText() + (((JMenuItem)menuItem).isEnabled() ? "" : "[Disabled]"));
            logMenuSubElements((JMenuItem)menuItem, "|   " + indent);
        } else if (menuItem instanceof JSeparator) {
            logToConsole(indent + "--------");
        }
    }
    
    private static void logMenuSubElements(MenuElement element, String indent) {
        for (MenuElement subItem : element.getSubElements()) {
            logMenuItem((Component)subItem, indent);
        }
    }

}

