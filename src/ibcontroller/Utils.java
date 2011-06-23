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
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.tree.TreeModel;

class Utils {

    static final SimpleDateFormat _DateFormatter = new SimpleDateFormat("HH:mm:ss:SSS");

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
     * Traverse a container hierarchy and returns the button with
     * the given text
     *
     */
    static JButton findButton(Container container,
                                     String text) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getText().equals(text)) {
                    return button;
                }
            } else if (component instanceof Container) {
                JButton button = findButton((Container) component, text);
                if (button != null) {
                    return button;
                }
            }
        }

        return null;
    }

    /**
     * Traverse a container hierarchy and returns the button with
     * the given text
     *
     */
    static JCheckBox findCheckBox(Container container,
                                               String text) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JCheckBox) {
                JCheckBox button = (JCheckBox) component;
                if (button.getText().equals(text)) {
                    return button;
                }
            } else if (component instanceof Container) {
                JCheckBox button = findCheckBox((Container) component,
                        text);
                if (button != null) {
                    return button;
                }
            }
        }

        return null;
    }

    /**
     * Traverse a container hierarchy and returns the button with
     * the given text
     *
     */
    static JRadioButton findRadioButton(Container container,
                                               String text) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JRadioButton) {
                JRadioButton button = (JRadioButton) component;
                if (button.getText().equals(text)) {
                    return button;
                }
            } else if (component instanceof Container) {
                JRadioButton button = findRadioButton((Container) component,
                        text);
                if (button != null) {
                    return button;
                }
            }
        }

        return null;
    }

    /**
     * Traverse a container hierarchy and returns the JTextLabel
     * that contains the given substring.
     */
    static JLabel findLabel(Container container,
                                   String text) {
        Component[] components = container.getComponents();

        for (Component component: components) {
            if (component instanceof JLabel) {
                JLabel button = (JLabel) component;
                if (button.getText() != null &&
                        button.getText().contains(text)) {
                    return button;
                }
            } else if (component instanceof Container) {
                JLabel button = findLabel((Container) component, text);
                if (button != null) {
                    return button;
                }
            }
        }

        return null;
    }

    /**
     * Traverse a container hierarchy and returns the ith JTextField
     * (0 based indexing)
     *
     */
    static JTextField findTextField(Container container,
                                            int ith) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JTextField && ith > 0) {
                ith--;
            } else if (component instanceof JTextField && ith == 0) {
                return (JTextField) component;
            } else if (component instanceof Container) {
                JTextField tf = findTextField((Container) component, ith);
                if (tf != null) {
                    return tf;
                }
            }
        }
        return null;
    }

    /**
     * Traverse a container hierarchy and returns the first JMenuBar
     * it finds.
     *
     */
    static JMenuBar findMenuBar(Container container) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JMenuBar) {
                return (JMenuBar) component;
            } else if (component instanceof Container) {
                JMenuBar jmb = findMenuBar((Container) component);
                if (jmb != null) {
                    return jmb;
                }
            }
        }
        return null;
    }

    /**
     * Traverse a container hierarchy and returns the JMenuItem with
     * the given text
     *
     */
    static JMenuItem findMenuItem(MenuElement container, String text) {
        MenuElement[] components = container.getSubElements();

        for (MenuElement component : components) {
            if (component instanceof JMenuItem) {
                JMenuItem button = (JMenuItem) component;
                if (button.getText().equals(text)) {
                    return button;
                }
            } else {
                JMenuItem button = findMenuItem(component, text);
                if (button != null) {
                    return button;
                }
            }
        }

        return null;
    }

    /**
     * Traverse a container hierarchy and returns the JMenuItem with
     * the given path
     *
     */
    static JMenuItem findMenuItem(Container container, String[] path) {
        if (path.length == 0) return null;

        MenuElement currentItem = findMenuBar(container);
        if (currentItem == null) return null;

        for (int i = 0; i < path.length; i++) {
            currentItem = findMenuItem(currentItem, path[i]);
            if (currentItem == null) return null;
        }
        return (JMenuItem)currentItem;
    }

    /**
     * Traverse a container hierarchy and returns the first JMenuBar
     * it finds.
     *
     */
    static JOptionPane findOptionPane(Container container) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JOptionPane) {
                return (JOptionPane) component;
            } else if (component instanceof Container) {
                JOptionPane op = findOptionPane((Container) component);
                if (op != null) {
                    return op;
                }
            }
        }
        return null;
    }

    /**
     * Traverse a container hierarchy and returns the first JMenuBar
     * it finds.
     *
     */
    static JTree findTree(Container container) {
        Component[] components = container.getComponents();

        for (Component component : components) {
            if (component instanceof JTree) {
                return (JTree) component;
            } else if (component instanceof Container) {
                JTree tree = findTree((Container) component);
                if (tree != null) {
                    return tree;
                }
            }
        }
        return null;
    }

    /*
     * Find the node with the given text below the given node in the specified TreeModel
     */
    static Object findChildNode(TreeModel model, Object node, String text) {
        for (int i = 0; i < model.getChildCount(node); i++) {
            Object currNode = model.getChild(node, i);
            if (currNode.toString() != null && currNode.toString().equals(text)) return currNode;
        }
        return null;
    }

    static boolean isButtonEnabled(final Window window, final String buttonText) {
        final JButton button = findButton(window, buttonText);
        if (button == null) return false;

        return button.isEnabled();
    }

    static boolean isCheckBoxSelected(Window window, String buttonText) {
        final JCheckBox cb = findCheckBox(window, buttonText);
        if (cb == null) return false;

        return cb.isSelected();
    }

    static boolean isRadioButtonSelected(Window window, String buttonText) {
        final JRadioButton rb = findRadioButton(window, buttonText);
        if (rb == null) return false;

        return rb.isSelected();

    }

    /**
     * writes a text message prefixed with the current time to the console
     */
    static void logToConsole(String msg) {
        System.out.println(_DateFormatter.format(
                new Date()) + " IBController: " + msg);
    }

    static void logWindowComponents(Window window) {
        for (Component component : window.getComponents()) logComponent(component, "");
    }

    /**
     * sleeps for millis milliseconds, approximately.
     */
    static void pause(int millis) {
        try {
            Thread.sleep(millis); // sleep a bit before trying again.
        } catch (InterruptedException ie) {
        }
    }

    static boolean setCheckBoxSelected(Window window, String buttonText, final boolean value) {
        final JCheckBox cb = findCheckBox(window, buttonText);
        if (cb == null) return false;
        cb.setSelected(value);
        return true;
    }

    static boolean setRadioButtonSelected(Window window, String buttonText) {
        final JRadioButton rb = findRadioButton(window, buttonText);
        if (rb == null) return false;

        if (rb.isSelected()) return true;

        rb.doClick();
        return true;
    }

    static boolean setTextField(Window window, int fieldNumber, final String value) {
        final JTextField tf = findTextField(window, fieldNumber);
        if (tf != null) {
            tf.setText(value);  // NB: setText() is threadsafe, unlike most Swing methods
            return true;
        } else {
            return false;
        }
    }

    static boolean titleContains(Window window, String text) {
        String title = getWindowTitle(window);
        return (title != null && title.contains(text));
    }

    static boolean titleEquals(Window window, String text) {
        String title = getWindowTitle(window);
        return (title != null && title.equals(text));
    }

    private static String getComponentDetails(Component component) {
        String s = "";
        if (component instanceof JButton) s = s + "JButton: " + ((JButton)component).getText();
        else if (component instanceof JCheckBox) s = s + "JCheckBox: " + ((JCheckBox) component).getText();
        else if (component instanceof JLabel) s = s + "JLabel: " + ((JLabel) component).getText();
        else if (component instanceof JOptionPane) s = s + "JOptionPane: " + ((JOptionPane) component).getMessage().toString();
        else if (component instanceof JRadioButton) s = s + "JRadioButton: " + ((JRadioButton) component).getText();
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

    private static void logComponent(Component component, String indent) {
        logToConsole(indent + component.getName() + "(" + component.getClass().getName() + ")" + getComponentDetails(component));
        if (component instanceof JTree) logTreeNodes(((JTree) component).getModel(), ((JTree) component).getModel().getRoot(), "|   " + indent);
        if (component instanceof Container) {
            for (Component subComponent : ((Container)component).getComponents()) logComponent(subComponent,"|   " + indent);
        }
    }

    private static void logTreeNodes(TreeModel model, Object node, String indent) {
        logToConsole(indent + node.toString());
        for (int i = 0; i < model.getChildCount(node); i++) logTreeNodes(model, model.getChild(node, i), "|   " + indent);
    }

}
