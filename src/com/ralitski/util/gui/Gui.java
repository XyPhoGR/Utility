package com.ralitski.util.gui;

import com.ralitski.util.input.InputUser;
import com.ralitski.util.input.event.KeyEvent;
import com.ralitski.util.input.event.MouseEvent;
import com.ralitski.util.render.img.Color;

public class Gui implements InputUser {
	
	//used to darken lower-level gui screens (parent gui rendered behind current gui)
	private static final Color FILM = new Color(100, 100, 100, 60);
	
	protected GuiManager owner;
	protected Gui parent;
	protected Component topLevel;
	protected Component selected; //used to keep track of the selected component within the hierarchy
	
	public Gui(GuiManager owner) {
		this.owner = owner;
	}
	
	public Gui(Gui parent) {
		this.parent = parent;
	}
	
	public void setTopLevel(Component topLevel) {
		this.topLevel = topLevel;
	}
	
	public GuiManager getOwner() {
		return owner != null ? owner : parent.getOwner();
	}
	
	public Gui getParent() {
		return parent;
	}
	
	public boolean isChildOf(Gui parent) {
		Gui g = this;
		while(g.getParent() != null) {
			g = g.getParent();
			if(g == parent) return true;
		}
		return false;
	}
	
	public void close() {
		GuiManager owner = getOwner();
		if(owner.getCurrentScreen() == this) {
			select(null);
			owner.closeScreen();
		}
	}
	
	public void open(Gui child) {
		GuiManager owner = getOwner();
		owner.openScreen(child);
	}
	
	public void select(Component c) {
		if(selected != null) {
			selected.setSelected(false);
		}
		if(c != null && c.isSelectable()) {
			selected = c;
			if(c != null) c.setSelected(true);
		} else selected = null;
	}
	
	public Component getSelected() {
		return selected;
	}
	
	public boolean renderParent() {
		return true;
	}
	
	public void render2d(float partial, float partialFromLast) {
		GuiManager manager = getOwner();
		GuiOwner render = manager.getGuiOwner();
		if(parent != null && renderParent()) {
			parent.render2d(partial, partialFromLast);
			if(drawFilm()) render.getRenderer().drawBox(manager.getWindow(), FILM);
		}
		if(topLevel != null) topLevel.render(render);
	}
	
	protected boolean drawFilm() {
		return true;
	}
	
	//optional 3d rendering
	public void render3d(float partial, float partialFromLast) {}
	
	//optional stuff to let the gui do whatever it needs to
	public void update() {}
	public void onOpen(boolean reentry) {}
	public void onClose(boolean exit) {}
	public void onResize() {}

	@Override
	public void onMouseEvent(MouseEvent event) {
		if(topLevel != null) topLevel.onMouseEvent(event);
	}

	@Override
	public void onKeyEvent(KeyEvent event) {
		if(selected != null) selected.onKeyEvent(event);
	}
}
