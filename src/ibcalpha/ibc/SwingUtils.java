// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2018 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBC is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBC is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBC.  If not, see <http://www.gnu.org/licenses/>.

package ibcalpha.ibc;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.ComboBoxModel;
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
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.MenuElement;
import javax.swing.tree.TreeModel;

class SwingUtils {

    static final String NEWLINE = System.lineSeparator();

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
        clickButton(button);
        return true;
    }

    static void clickButton(final JButton button) {
        if (! button.isEnabled()) {
            button.setEnabled(true);
            Utils.logToConsole("Button was disabled, has been enabled: " + button.getText());
        }

        Utils.logToConsole("Click button: " + button.getText());
        button.doClick();
        if (! button.isEnabled()) Utils.logToConsole("Button now disabled: " + button.getText());
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
            if (component instanceof JButton && text.equalsIgnoreCase(((JButton)component).getText())) return (JButton)component;
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
            if (component instanceof JCheckBox && text.equalsIgnoreCase(((JCheckBox)component).getText())) return (JCheckBox)component;
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
            if (text.equalsIgnoreCase(component.getName())) return component;
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
            if (component instanceof JRadioButton && text.equalsIgnoreCase(((JRadioButton)component).getText())) return (JRadioButton)component;
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
            if (component instanceof JLabel && ((JLabel)component).getText() != null &&  ((JLabel)component).getText().toLowerCase().contains(text.toLowerCase())) return (JLabel)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the ith JList
     * (0 based indexing).
     *
     * @param container
     *  the Container to search in
     * @param ith
     *   specifies which JList to return (the first one is specified by 0,
     * the next by 1, etc)
     * @return
     *  the required JList if it is found, otherwise null
     */
    static JList<?> findList(Container container, int ith) {
        ComponentIterator iter = new ComponentIterator(container);
        int i = 0;
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JList<?> && i++ == ith) return (JList<?>)component;
        }
        return null;
    }

    /**
     * Traverses a container hierarchy and returns the JTextArea
     * that contains the given substring.
     * @param container
     *  the Container to search in
     * @param text
     *  the substring to find in a JTextArea
     * @return
     *  the JTextArea, if it was found;  otherwise null
     */
    static JTextArea findTextArea(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JTextArea) {
                String content = ((JTextArea)component).getText();
                if (content != null && content.toLowerCase().contains(text.toLowerCase())) {
                    return (JTextArea)component;
                }
            }
        }
        return null;
    }

    static String getTexts(Container container) {
        ComponentIterator iter = new ComponentIterator(container);
        String s = "";
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JTextArea) {
                String content = ((JTextArea)component).getText();
                if (content != null) {
                    if (s.length() != 0) s += NEWLINE;
                    s += content;
                }
            }
        }
        return s;
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
     * Traverses a container hierarchy and returns the JTextPane
     * that contains the given substring.
     * @param container
     *  the Container to search in
     * @param text
     *  the substring to find in a JTextPane
     * @return
     *  the JTextArea, if it was found;  otherwise null
     */
    static JTextPane findTextPane(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JTextPane) {
                String content = ((JTextPane)component).getText();
                if (content != null && content.toLowerCase().contains(text.toLowerCase())) {
                    return (JTextPane)component;
                }
            }
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
    static JToggleButton findToggleButton(Container container, String text) {
        ComponentIterator iter = new ComponentIterator(container);
        while (iter.hasNext()) {
            Component component = iter.next();
            if (component instanceof JToggleButton && text.equalsIgnoreCase(((JToggleButton)component).getText())) return (JToggleButton)component;
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
                if (button.getText().equalsIgnoreCase(text)) {
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
            if (currNode.toString() != null && currNode.toString().equalsIgnoreCase(text)) return currNode;
        }
        return null;
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
     * Returns a string representing the structure of the specified window.
     * 
     * Details of each component in the window are included, indented to reflect
     * the component's position in the hierarchy.
     * @param window
     * The Window whose structure is to be returned.
     */
    static String getWindowStructure(Window window) {
        StringBuilder builder = new StringBuilder();
        try {
            for (Component component : window.getComponents()) appendComponentStructure(component, builder);
        } catch (Exception e) {
            builder.append("Exception occurred while generating window structure: ");
            builder.append(e.toString());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            builder.append(sw.toString());
        }
        builder.append(NEWLINE);
        builder.append(NEWLINE);
        return builder.toString();
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
        return (title != null && title.toLowerCase().contains(text.toLowerCase()));
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
        return (title != null && title.equalsIgnoreCase(text));
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
                return "Deiconified";
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

    static final String NO_TITLE = "** no title **"; 
      static String getWindowTitle(Window window) {
        String title = NO_TITLE;
        if (window instanceof JDialog) {
            title = ((JDialog)window).getTitle();
        } else  if (window instanceof JFrame) {
            title =((JFrame)window).getTitle();
        }
        if (title == null) title = NO_TITLE;
        return title;
    }

    private static String getComponentDetails(Component component) {
        String s = component.isEnabled() ? "" : "[Disabled]";
        if (component instanceof JButton) {
            s += "JButton: "; 
            s += ((JButton)component).getText();
        } else if (component instanceof JCheckBox) {
            s += "JCheckBox: ";
            s += ((JCheckBox) component).getText();
            s += "(" + (((JCheckBox) component).isSelected() ? "selected" : "unselected") + ")";
        } else if (component instanceof JLabel) {
            s += "JLabel: "; 
            s += ((JLabel) component).getText();
        } else if (component instanceof JOptionPane) {
            s += "JOptionPane: ";
            s += ((JOptionPane) component).getMessage().toString();
        }else if (component instanceof JRadioButton) {
            s += "JRadioButton: "; 
            s += ((JRadioButton) component).getText();
        } else if (component instanceof JPasswordField) {
            s += "JPasswordField: ";
            s += "***";
        } else if (component instanceof JTextArea) {
            s += "JTextArea: ";
            s += ((JTextArea) component).getText();
        } else if (component instanceof JTextField) {
            s += "JTextField: ";
            s += ((JTextField) component).getText();
        } else if (component instanceof JTextPane) {
            s += "JTextPane: ";
            s += ((JTextPane) component).getText();
        } else if (component instanceof JMenuBar) {
            s += "JMenuBar: "; 
            s += ((JMenuBar) component).getName();
        } else if (component instanceof JMenuItem) {
            s += "JMenuItem: ";
            s += ((JMenuItem) component).getText();
        } else if (component instanceof JTree) {
            s += "JTree: ";
        }else if (component instanceof JComboBox) {
            s += "JComboBox: ";
            s += ((JComboBox) component).getSelectedItem().toString();
        } else if (component instanceof JList) {
            s += "JList: ";
        } else if (component instanceof JToggleButton) {
            s += "JToggleButton: ";
            s += ((JToggleButton) component).getText();
        } else {
            s+= getClassDerivation(component);
        }
        return s;
    }

    private static String getClassDerivation(Object object) {
        String s = object.getClass().getSimpleName();
        Class<?> c = object.getClass().getSuperclass();
        while (c != null) {
            s = c.getSimpleName() + "." + s;
            c = c.getSuperclass();
        }
        return s;
    }

    private static void appendComponentStructure(Component component, StringBuilder builder) {
        appendComponentStructure(component, builder, "");
    }

    private static void appendComponentStructure(Component component, StringBuilder builder, String indent) {
        builder.append(NEWLINE);
        builder.append(indent);
        builder.append(component.getName());
        builder.append("(");
        builder.append(component.getClass().getName());
        builder.append(")");
        builder.append("{");
        builder.append(getComponentDetails(component));
        builder.append("}");
        if (component instanceof JTree) appendTreeNodes(((JTree) component).getModel(), ((JTree) component).getModel().getRoot(), builder, "|   " + indent);
        if (component instanceof JList<?>) appendListItems(((JList<?>) component).getModel(), builder, "|   " + indent);
        if (component instanceof JComboBox<?>) appendComboItems(((JComboBox<?>) component).getModel(), builder, "|   " + indent);
        if (component instanceof JMenuBar) {
            appendMenuItem(component, builder, "|   " + indent);
        } else if (component instanceof Container) {
            for (Component subComponent : ((Container)component).getComponents()) appendComponentStructure(subComponent, builder, "|   " + indent);
        }
    }

    private static void appendComboItems(ComboBoxModel<?> model, StringBuilder builder, String indent) {
        for (int i = 0; i < model.getSize(); i++) {
            builder.append(NEWLINE);
            builder.append(indent);
            builder.append(model.getElementAt(i).toString());
        }
    }
    
    private static void appendListItems(ListModel<?> model, StringBuilder builder, String indent) {
        for (int i = 0; i < model.getSize(); i++) {
            builder.append(NEWLINE);
            builder.append(indent);
            builder.append(model.getElementAt(i).toString());
        }
    }
    
    private static void appendTreeNodes(TreeModel model, Object node, StringBuilder builder, String indent) {
        builder.append(NEWLINE);
        builder.append(indent);
        if (node instanceof Component) {
            builder.append(node.toString());
            appendComponentStructure((Component)node, builder, "|   " + indent);
            builder.append(getClassDerivation(node));
        } else {
            builder.append(node.toString());
            builder.append("  (");
            builder.append(getClassDerivation(node));
            builder.append(")");
        }
        for (int i = 0; i < model.getChildCount(node); i++) appendTreeNodes(model, model.getChild(node, i), builder, "|   " + indent);
    }

    private static void appendMenuItem(Component menuItem, StringBuilder builder, String indent) {
        if (menuItem instanceof JMenuBar) {
            appendMenuSubElements((MenuElement)menuItem, builder, indent);
        } else if (menuItem instanceof JPopupMenu) {
            appendMenuSubElements((MenuElement)menuItem, builder, indent);
        } else if (menuItem instanceof JMenuItem) {
            builder.append(NEWLINE);
            builder.append(indent);
            builder.append(((JMenuItem)menuItem).getText());
            builder.append(((JMenuItem)menuItem).isEnabled() ? "" : "[Disabled]");
            appendMenuSubElements((JMenuItem)menuItem, builder, "|   " + indent);
        } else if (menuItem instanceof JSeparator) {
            builder.append(NEWLINE);
            builder.append(indent);
            builder.append("--------");
        }
    }

    private static void appendMenuSubElements(MenuElement element, StringBuilder builder, String indent) {
        for (MenuElement subItem : element.getSubElements()) {
            appendMenuItem((Component)subItem, builder, indent);
        }
    }

}
