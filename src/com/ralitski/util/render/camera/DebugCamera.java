package com.ralitski.util.render.camera;

import com.ralitski.util.input.InputUser;
import com.ralitski.util.input.event.KeyEvent;
import com.ralitski.util.input.event.MouseEvent;
import com.ralitski.util.input.event.MouseWheelEvent;
import com.ralitski.util.input.event.KeyEvent.KeyEventType;
import com.ralitski.util.math.geom.d3.Orientation3d;
import com.ralitski.util.math.geom.d3.Point3d;
import com.ralitski.util.render.camera.Camera;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 *
 * @author ralitski
 */
public class DebugCamera implements Camera, InputUser {
    
    public static final float MIN_Y = 1;
    public static final float GRAVITY = .07F;
    public static final float DRAG = .9F;
    public static final float SPEED = .001F;
    public static final float SENSITIVITY = .5F;
    public static final float FLY_Y = .01F;
    
    private float x, y, z;
    private Pos pos = new Pos();
    private Orientation3d look = new Orientation3d();
    private float mx, my, mz;
    private boolean flying, onGround, mouseGrabbed;
    //if you only fly up/down by pressing space/LSHIFT
    private boolean ONLY_FLY_UP;

    public void update() {
        if(!flying && y > MIN_Y) {
            //gravity
            my -= GRAVITY;
        }
        mx *= DRAG;
        my *= DRAG;
        mz *= DRAG;
        x += mx;
        y += my;
        z += mz;
        if (y <= MIN_Y) {
            y = MIN_Y;
            onGround = true;
        } else if(y > MIN_Y){
            onGround = false;
        }
        if(mouseGrabbed) {
            float pitch = ((float)Math.toDegrees(look.getPitch()) - Mouse.getDY() * SENSITIVITY) % 360;
            
            if(pitch > 90) pitch = 90;
            else if(pitch < -90) pitch = -90;
            look.setPitch((float)Math.toRadians(pitch));
            float toYaw = Mouse.getDX() * SENSITIVITY;
            float yaw = ((float)Math.toDegrees(look.getYaw()) + toYaw) % 360;
            if(yaw < 0) yaw += 360;
            look.setYaw((float)Math.toRadians(yaw));
        }
    }
    
//    public boolean upsideDown() {
//        float aPitch = Math.abs(pitch);
//        return 90 < aPitch && aPitch < 270;
//    }
    
    public Point3d getPosition() {
    	return pos;
    }
    
    public Orientation3d getOrientation() {
    	return look;
    }

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setZ(float z) {
		this.z = z;
	}
    
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
    
    private void moveForward(float forward) {
    	/*
                double y = -Math.sin(Math.toRadians(pitch));
                double h = Math.cos(Math.toRadians(pitch));
                double x = -h * Math.sin(Math.toRadians(yaw));
                double z = h * Math.cos(Math.toRadians(yaw));
                Vector v = new Vector(x, y, z).multiply(scale);
                vectors.add(v);
    	 */
//        if(upsideDown()) forward = -forward;
        float rPitch = look.getPitch();
        float rYaw = look.getYaw();
        
        double y = -Math.sin(rPitch);
        double h = 1;
        if(!ONLY_FLY_UP) h = Math.cos(rPitch);
        double x = -h * Math.sin(rYaw);
        double z = h * Math.cos(rYaw);
        
        mx -= x * forward;
        mz -= z * forward;
	    if(flying && !ONLY_FLY_UP) my += y * forward;
    }
    
    private void moveStrafe(float strafe) {
        float rYaw = look.getYaw();
        
        mx -= -(float)Math.cos(-rYaw) * strafe;
        mz -= (float)Math.sin(-rYaw) * strafe;
    }
    
    private class Pos extends Point3d {
    	public void setX(float x) {
    		DebugCamera.this.x = x;
    	}
    	
    	public void setY(float y) {
    		DebugCamera.this.y = y;
    	}
    	
    	public void setZ(float z) {
    		DebugCamera.this.z = z;
    	}
    	
    	public float getX() {
    		return x;
    	}
    	
    	public float getY() {
    		return y;
    	}
    	
    	public float getZ() {
    		return z;
    	}
    }

	@Override
	public void onMouseEvent(MouseEvent event) {
		if(event instanceof MouseWheelEvent) {
			MouseWheelEvent mEvent = (MouseWheelEvent)event;
	        flying = mEvent.getWheel() > 0;
		}
	}

	@Override
	public void onKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		int key = event.getKey();
		KeyEventType type = event.getType();
		if(type == KeyEventType.DOWN) {
	        if(key == Keyboard.KEY_ESCAPE) {
	            mouseGrabbed = !Mouse.isGrabbed();
	            Mouse.setGrabbed(mouseGrabbed);
	        } else if(key == Keyboard.KEY_TAB) {
	            ONLY_FLY_UP = !ONLY_FLY_UP;
	        }
		} else if(type == KeyEventType.HOLD) {
	        if(key == Keyboard.KEY_SPACE && (flying || onGround)) {
	            my += flying ? FLY_Y : 1;
	        } else if(key == Keyboard.KEY_LSHIFT && flying) {
	            my -= FLY_Y;
	        } else if(key == Keyboard.KEY_W) {
	            //forward
	            moveForward(SPEED);
	        } else if(key == Keyboard.KEY_S) {
	            //backward
	            moveForward(-SPEED);
	        }else if(key == Keyboard.KEY_A) {
	            //left
	            moveStrafe(-SPEED);
	        } else if(key == Keyboard.KEY_D) {
	            //right
	            moveStrafe(SPEED);
	        }
		}
	}
}
