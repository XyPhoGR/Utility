package com.ralitski.util.gui;

import java.util.LinkedList;
import java.util.List;

import com.ralitski.util.gui.BoxEvent.BoxEventType;
import com.ralitski.util.gui.layout.BlankLayout;
import com.ralitski.util.gui.layout.Layout;
import com.ralitski.util.gui.render.RenderList;
import com.ralitski.util.gui.render.RenderListState;
import com.ralitski.util.input.event.MouseButtonEvent;
import com.ralitski.util.input.event.MouseButtonEvent.MouseEventType;
import com.ralitski.util.input.event.MouseEvent;

public abstract class ContainerAbstract extends ComponentAbstract implements BoxEventListener, Container {
	
	protected Layout layout;
	protected List<Component> children;
	
	//used to keep track of child components that use their own render lists
	
	/**
	 * The list of components that will be rendered in this Frame's render list.
	 */
	private List<Component> renderWithThis;
	
	/**
	 * The list of components that will not be rendered with this Frame's render list.
	 */
	private List<Component> renderWithSelf;
	
	//render list stuff
	protected RenderList renderList;
	
	private boolean renderSelf;
	
	public ContainerAbstract(Gui gui) {
		super(gui);
		prepare(gui);
	}
	
	public ContainerAbstract(Gui gui, int width, int height) {
		super(gui, width, height);
		prepare(gui);
	}
	
	public ContainerAbstract(Gui gui, Box box) {
		super(gui, box);
		prepare(gui);
	}
	
	private void prepare(Gui gui) {
		layout = new BlankLayout();
		children = new LinkedList<Component>();
		GuiOwner owner = gui.getOwner().getGuiOwner();
		if(owner.getRenderer().supportLists()) {
			renderWithThis = new LinkedList<Component>();
			renderWithSelf = new LinkedList<Component>();
//			getRenderList(owner);
		}
		this.useParentRenderList = false;
		box.setEventListener(this);
	}
	
	//stuff

	@Override
	public int getRenderStyles() {
		return 1;
	}
	
	public Layout getLayout() {
		return layout;
	}
	
	public void setLayout(Layout layout) {
		for(Component c : children) {
			layout.addComponent(c, null);
		}
		this.layout = layout;
	}

	@Override
	public void add(Component c) {
		add(c, null);
	}

	@Override
	public void add(Component c, String layout) {
		children.add(c);
		if(gui.getOwner().getGuiOwner().getRenderer().supportLists()) {
			if(c.useParentRenderList()) {
				this.renderWithThis.add(c);
				c.setParentRenderList(renderListState);
			}
			else this.renderWithSelf.add(c);
		}
		c.setParent(this);
		this.layout.addComponent(c, layout);
	}
	
	public void remove(Component c) {
		if(c.getParent() == this) {
			c.setParent(null);
			children.remove(c);
			layout.removeComponent(c);
			if(gui.getOwner().getGuiOwner().getRenderer().supportLists()) {
				if(c.useParentRenderList()) this.renderWithThis.remove(c);
				else this.renderWithSelf.remove(c);
//				if(!this.renderWithThis.remove(c)) this.renderWithSelf.remove(c);
				c.setParentRenderList(null);
			}
		}
	}
	
	public List<Component> getComponents() {
		return children;
	}

	@Override
	public void setParentRenderList(RenderListState state) {
		if(useParentRenderList()) {
			if(state == null) state = new RenderListState();
			this.renderListState = state;
			for(Component c : renderWithThis) {
				c.setParentRenderList(state);
			}
		} else {
			throw new UnsupportedOperationException("Containers do not share a RenderList with their parent");
		}
	}
	
	//position components and possibly resize container
	public void refresh() {
		for(Component c : children) {
			if(c instanceof Container) {
				((Container)c).refresh();
			} else {
				if(c.isResizable()) {
					box.setWidth(c.getMinWidth());
					box.setHeight(c.getMinHeight());
				}
			}
		}
		if(resizable) {
			Dimension d = layout.getMinimumSize();
			GuiManager manager = gui.getOwner();
			int dW = d != null ? d.getWidth() : 0;
			int dH = d != null ? d.getHeight() : 0;
			int w = box.getWidth();
			int h = box.getHeight();
			int width = w;
			int height = h;
			int wW = manager.getWindowWidth();
			int wH = manager.getWindowHeight();
			if(parent == null) {
				width = wW;
				height = wH;
			} else {
				if(width < dW) width = Math.min(dW, wW);
				else width = Math.min(width, dW);
				if(height < dH) height = Math.min(dH, wH);
				else height = Math.min(height, dH);
			}
			box.setWidth(width);
			box.setHeight(height);
			BoxPosition.position(box, null, manager.getWindow(), BoxPosition.WITHIN_STRICT);
		}
		layout.refresh(box);
		if(renderListState != null) renderListState.setDirty(true);
	}
	
	public void refreshAll() {
		for(Component c : children) {
			if(c instanceof Container) {
				((Container)c).refreshAll();
			}
		}
		refresh();
	}

	@Override
	public boolean doRenderSelf() {
		return renderSelf;
	}

	@Override
	public void setRenderSelf(boolean renderSelf) {
		this.renderSelf = renderSelf;
	}
	
	public void render(GuiOwner owner) {
		if(owner.getRenderer().supportLists() && !useParentRenderList()) {
			if(renderList == null) getRenderList(owner);
			if(renderListState.isDirty() || !renderList.registered()) {
				renderList.compile();
				renderListState.setDirty(false);
			}
			//render stuff with this list
			renderList.call();
			//render stuff with own list
			for(Component c : renderWithSelf) {
				c.render(owner);
			}
		} else {
			if(renderSelf) {
				renderSelf(owner);
			}
			for(Component c : children) {
				c.render(owner);
			}
		}
	}
	
	//actually renders the RenderList stuff, but no sense in making another method since this one is already defined
	@Override
	protected void doRender() {
		//render frame background, then components
		GuiOwner owner = gui.getOwner().getGuiOwner();
		if(renderSelf) {
			renderSelf(owner);
		}
		for(Component c : renderWithThis) {
			c.render(owner);
		}
	}
	
	protected void renderSelf(GuiOwner owner) {
		owner.getRenderer().drawBox(box, this, style);
	}
	
	protected void getRenderList(GuiOwner owner) {
		renderList = owner.getRenderer().newList(new RenderRunner());
		renderListState.setDirty(true);
	}
	
	private class RenderRunner implements Runnable {
		@Override
		public void run() {
			doRender();
		}
	}

	@Override
	public void onMouseEvent(MouseEvent event) {
		boolean handled = false;
		for(Component c : children) {
			Box b = c.getBounds();
			if(b.contains(event.getX(), event.getY())) {
				handled = true;
				if(isSelection(event)) {
					gui.select(c);
				}
			}
			c.onMouseEvent(event);
		}
		if(!handled) {
			mouseNotHandled(event);
		}
	}
	
	//called when the mouse event was not inside another component
	protected void mouseNotHandled(MouseEvent event) {
		gui.select(null);
	}
	
	private boolean isSelection(MouseEvent event) {
		if(event instanceof MouseButtonEvent) {
			MouseButtonEvent mEvent = (MouseButtonEvent)event;
			return mEvent.getButton() == 0 && mEvent.getType() == MouseEventType.DOWN;
		}
		return false;
	}
	
	public void onBoxEvent(BoxEvent event) {
		BoxEventType type = event.getEventType();
		if(type.isMovement()) {
			int x = event.getTranslateX();
			int y = event.getTranslateY();
			//translate the container's children
			for(Component c : children) {
				c.getBounds().translate(x, y);
			}
		} else {
			//refresh the container to distribute its children
			//TODO: use refreshAll()? naw
			this.refresh();
		}
	}
	
	public void delete() {
		if(renderList != null) renderList.delete();
		for(Component c : children) {
			c.delete();
		}
	}

}
