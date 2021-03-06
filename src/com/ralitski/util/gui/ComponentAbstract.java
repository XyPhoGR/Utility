package com.ralitski.util.gui;

import java.util.LinkedList;
import java.util.List;

import com.ralitski.util.gui.render.RenderList;
import com.ralitski.util.gui.render.RenderListState;
import com.ralitski.util.gui.render.RenderStyle;

public abstract class ComponentAbstract implements Component {
	
	protected Gui gui;
	protected int minWidth;
	protected int minHeight;
	protected boolean resizable;
	protected int id = -1;
	protected Box box;
	protected RenderStyle style;
	protected Container parent;
	//render list stuff
	protected RenderList renderList;
	protected RenderListState renderListState;
	protected List<ComponentEventListener> eventListeners;
	protected boolean useParentRenderList = true;
	
	public ComponentAbstract(Gui gui) {
		prepare(gui);
		GuiOwner owner = gui.getOwner().getGuiOwner();
		box = new Box(0, 0, owner.getWidth(), owner.getHeight());
	}
	
	public ComponentAbstract(Gui gui, int width, int height) {
		prepare(gui);
		box = new Box(0, 0, width, height);
		BoxPosition.position(box, null, gui.getOwner().getWindow(), BoxPosition.CENTER);
	}
	
	public ComponentAbstract(Gui gui, Box box) {
		prepare(gui);
		BoxPosition.position(box, null, gui.getOwner().getWindow(), BoxPosition.WITHIN);
		this.box = box;
	}
	
	private void prepare(Gui gui) {
		this.gui = gui;
		eventListeners = new LinkedList<ComponentEventListener>();
		GuiOwner owner = gui.getOwner().getGuiOwner();
		if(owner.getRenderer().supportLists()) {
			renderListState = new RenderListState();
//			getRenderList(owner);
		}
	}
	
	//stuff

	public int getMinWidth() {
		return minWidth;
	}

	public void setMinWidth(int minWidth) {
		this.minWidth = minWidth;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public void setMinHeight(int minHeight) {
		this.minHeight = minHeight;
	}

	@Override
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	@Override
	public boolean isResizable() {
		return resizable;
	}

	@Override
	public Gui getGui() {
		return gui;
	}

	@Override
	public void setRenderStyle(int index, RenderStyle s) {
		style = s;
	}

	@Override
	public RenderStyle getRenderStyle(int index) {
		return style;
	}

	@Override
	public int getRenderStyles() {
		return 1;
	}

	@Override
	public Container getParent() {
		return parent;
	}

	@Override
	public void setParent(Container container) {
		this.parent = container;
//		setBox(BoxRelative.makeRelative(box, container != null ? container.getBounds() : null));
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public void setSelected(boolean selected) {
		throw new UnsupportedOperationException("This Component can't be selected.");
	}

	@Override
	public boolean isSelected() {
		return false;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public Box getBounds() {
		return box;
	}

	@Override
	public void addComponentEventListener(ComponentEventListener listener) {
		eventListeners.add(listener);
	}

	@Override
	public void removeComponentEventListener(ComponentEventListener listener) {
		eventListeners.remove(listener);
	}

	@Override
	public String getHoverText() {
		return null;
	}

	@Override
	public boolean useParentRenderList() {
		return useParentRenderList;
	}

	public void setUseParentRenderList(boolean useParentRenderList) {
		this.useParentRenderList = useParentRenderList;
	}

	@Override
	public void setParentRenderList(RenderListState state) {
		if(useParentRenderList()) {
			this.renderListState = state;
		} else {
			throw new UnsupportedOperationException("This Component does not share a RenderList with their parent");
		}
	}
	
	public void render(GuiOwner owner) {
		if(owner.getRenderer().supportLists() && !useParentRenderList()) {
			if(renderList == null) getRenderList(owner);
			if(renderListState.isDirty() || !renderList.registered()) {
				renderList.compile();
			}
			renderList.call();
		} else {
			doRender();
		}
	}
	
	protected void getRenderList(GuiOwner owner) {
		renderList = owner.getRenderer().newList(new RenderRunner());
		renderListState.setDirty(true);
	}
	
	protected void doRender() {
		gui.getOwner().getGuiOwner().getRenderer().drawBox(box, this, style);
	}
	
	private class RenderRunner implements Runnable {
		@Override
		public void run() {
			doRender();
		}
	}
	
	public void delete() {
		if(renderList != null) renderList.delete();
	}

}
