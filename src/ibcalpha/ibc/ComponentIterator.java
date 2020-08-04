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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

class ComponentIterator implements Iterator<Component> {

    Deque<NodeState> mStack = new ArrayDeque<>();

    Component mCurrent;

    ComponentIterator(Container container) {
        mStack.push(new NodeState(container));
    }

    @Override
    public boolean hasNext() {
        if (mCurrent == null)  return moveNext();
        return true;
    }

    @Override
    public Component next() {
        if (!hasNext()) throw new NoSuchElementException();
        Component result = mCurrent;
        mCurrent = null;
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    private boolean moveNext() {
        if (mStack.isEmpty()) return false;
        while (true) {
            NodeState currentState = mStack.peek();
            if (currentState.subComponents == null) {
                mStack.pop();
                if (mStack.isEmpty()) return false;
            } else {
                if (currentState.index < currentState.subComponents.length) {
                    mCurrent = currentState.subComponents[currentState.index++];
                    mStack.push(new NodeState(mCurrent));
                    return true;
                } else {
                    mStack.pop();
                    if (mStack.isEmpty()) return false;
                }
            }
        }
    }

    private class NodeState {
        int index;
        Component[] subComponents;

        NodeState(Component component) {
            this.index = 0;
            if (component instanceof Container) subComponents = ((Container)component).getComponents();
        }
    }

}
