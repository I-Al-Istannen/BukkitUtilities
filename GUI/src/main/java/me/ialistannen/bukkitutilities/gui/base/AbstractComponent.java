package me.ialistannen.bukkitutilities.gui.base;


import me.ialistannen.bukkitutilities.gui.Gui;
import me.ialistannen.bukkitutilities.gui.util.Dimension;

/**
 * A Skeleton class for {@link Component}
 */
public abstract class AbstractComponent implements Component, Cloneable {

    /**
     * The counter is used to distinguish instances, no matter if the internal
     * variables change. As you can't validate real equality for some components
     * (Runnable) the equals method can't check for logic equality. Besides that
     * you may want functionally identical components in a Gui, but not the same
     * one twice (bugs may occur)
     */
    private static int counter = 0;

    /**
     * This variable is expected to be final. Cloning prohibits actually doing
     * this, but to communicate the fact, it is written in CAPS
     */
    private int ID = counter++;

    protected Gui ownerGui;

    private Dimension size;
    private boolean visible = true;

    /**
     * @param size The size of the component
     */
    public AbstractComponent(Dimension size) {
        this.size = size;
    }

    @Override
    public void setGui(Gui gui) {
        ownerGui = gui;
    }

    @Override
    public Gui getGui() {
        return ownerGui;
    }

    @Override
    public int getHeight() {
        return size.getHeight();
    }

    @Override
    public int getWidth() {
        return size.getWidth();
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;

        if (getGui() != null) {
            getGui().reRender();
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * @return The Unique ID for this component
     */
    @SuppressWarnings("unused")
    public int getID() {
        return ID;
    }

    // we want this method to be supported. End of discussion.
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    protected Object clone() {
        try {
            AbstractComponent clone = (AbstractComponent) super.clone();
            clone.size = size;
            // clear the gui
            clone.ownerGui = null;
            clone.ID = counter++;
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A counter is used to distinguish instances, no matter if the internal
     * variables change.
     * <p>
     * As you can't validate real equality for some components (Runnable) the
     * equals method can't check for logic equality.
     * <p>
     * Besides that you may want functionally identical components in a Gui, but
     * not the same one twice (bugs may occur)
     *
     * @return True if they are the same instance
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractComponent)) {
            return false;
        }
        AbstractComponent that = (AbstractComponent) o;
        return ID == that.ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }
}
