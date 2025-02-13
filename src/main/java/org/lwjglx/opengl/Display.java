package org.lwjglx.opengl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjglx.LWJGLException;
import org.lwjglx.Sys;
import org.lwjglx.input.Keyboard;
import org.lwjglx.input.Mouse;

public class Display {
	
	private static String windowTitle = "Game";
	
	private static long context;
	
	private static boolean displayCreated = false;
	private static boolean displayFocused = false;
	private static boolean displayVisible = true;
	private static boolean displayDirty = false;
	private static boolean displayResizable = false;
	
	private static DisplayMode mode = new DisplayMode(640, 480);
	private static DisplayMode desktopDisplayMode = new DisplayMode(640, 480);
	
	private static int latestEventKey = 0;
	
	private static int displayX = 0;
	private static int displayY = 0;
	
	private static boolean displayResized = false;
	private static int displayWidth = 0;
	private static int displayHeight = 0;
	private static int displayFramebufferWidth = 0;
	private static int displayFramebufferHeight = 0;
	
	private static boolean latestResized = false;
	private static int latestWidth = 0;
	private static int latestHeight = 0;

	private static long lastMonitor = 0;
	private static int lastX, lastY, lastWidth, lastHeight;
	private static boolean fullscreen = false;

	private static Runnable windowHints = () -> {};
	
	static {
		Sys.initialize(); // init using dummy sys method
		
		long monitor = glfwGetPrimaryMonitor();
		lastMonitor = monitor;
		GLFWVidMode vidmode = Objects.requireNonNull(glfwGetVideoMode(monitor), "Failed to get video mode!");

		int monitorWidth = vidmode.width();
		int monitorHeight = vidmode.height();
		int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
		int monitorRefreshRate = vidmode.refreshRate();
		
		desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);
	}
	
	public static void create(PixelFormat pixel_format, Drawable shared_drawable) throws LWJGLException {
		System.out.println("TODO: Implement Display.create(PixelFormat, Drawable)"); // TODO
		create(pixel_format);
	}
	
	public static void create(PixelFormat pixel_format, ContextAttribs attribs) throws LWJGLException {
		System.out.println("TODO: Implement Display.create(PixelFormat, ContextAttribs)"); // TODO
		create(pixel_format);
	}
	
	public static void create(PixelFormat pixel_format) throws LWJGLException {
		glfwWindowHint(GLFW_DEPTH_BITS, pixel_format.getDepthBits());
		glfwWindowHint(GLFW_STENCIL_BITS, pixel_format.getStencilBits());
		glfwWindowHint(GLFW_ALPHA_BITS, pixel_format.getAlphaBits());
		// bbp is unsupported on GLFW
		create();
	}
	
	public static void create() throws LWJGLException {
		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode vidmode = Objects.requireNonNull(glfwGetVideoMode(monitor), "Failed to get video mode!");

		int monitorWidth = vidmode.width();
		int monitorHeight = vidmode.height();
		int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
		int monitorRefreshRate = vidmode.refreshRate();
		
		desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);
		windowHints.run();
		Window.handle = glfwCreateWindow(mode.getWidth(), mode.getHeight(), windowTitle, NULL, NULL);

		if ( Window.handle == 0L )
			throw new IllegalStateException("Failed to create Display window");
		
		
		Window.keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				latestEventKey = key;

				Keyboard.addKeyEvent(key, action != GLFW_RELEASE, action == GLFW_REPEAT);
			}
		};
		
		Window.charCallback = new GLFWCharCallback() {
			@Override
			public void invoke(long window, int codepoint) {
				Keyboard.addCharEvent(latestEventKey, (char)codepoint);
			}
		};
		
		Window.cursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				Mouse.addMoveEvent(xpos, ypos);
			}
		};
		
		Window.mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				Mouse.addButtonEvent(button, action == GLFW.GLFW_PRESS);
			}
		};
		
		Window.windowFocusCallback = new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, boolean focused) {
				displayFocused = focused;
			}
		};
		
		Window.windowIconifyCallback = new GLFWWindowIconifyCallback() {
			@Override
			public void invoke(long window, boolean iconified) {
				displayVisible = iconified;
			}
		};
		
		Window.windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				latestResized = true;
				latestWidth = width;
				latestHeight = height;
			}
		};
		
		Window.windowPosCallback = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int xpos, int ypos) {
				displayX = xpos;
				displayY = ypos;
			}
		};
		
		Window.windowRefreshCallback = new GLFWWindowRefreshCallback() {
			@Override
			public void invoke(long window) {
				displayDirty = true;
			}
		};
		
		Window.framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				displayFramebufferWidth = width;
				displayFramebufferHeight = height;
			}
		};

		Window.scrollCallback = new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				Mouse.addWheelEvent((int)yoffset);
			}
		};
		
		Window.setCallbacks();
		
		displayWidth = mode.getWidth();
		displayHeight = mode.getHeight();
		
		IntBuffer fbw = BufferUtils.createIntBuffer(1);
		IntBuffer fbh = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetFramebufferSize(Window.handle, fbw, fbh);
		displayFramebufferWidth = fbw.get(0);
		displayFramebufferHeight = fbh.get(0);
		
		glfwSetWindowPos(
			Window.handle,
			(monitorWidth - mode.getWidth()) / 2,
			(monitorHeight - mode.getHeight()) / 2
		);
		
		displayX = (monitorWidth - mode.getWidth()) / 2;
		displayY = (monitorHeight - mode.getHeight()) / 2;

		glfwMakeContextCurrent(Window.handle);
		GL.createCapabilities();
		context = GLFW.glfwGetCurrentContext();
		
		glfwShowWindow(Window.handle);
		
		displayCreated = true;
	}
	
	public static boolean isCreated() {
		return displayCreated;
	}
	
	public static boolean isActive() {
		return displayFocused;
	}
	
	public static boolean isVisible() {
		return displayVisible;
	}
	
	public static long getContext() {
		return context;
	}
	
	public static void setLocation(int new_x, int new_y) {
		glfwSetWindowPos(Window.handle, new_x, new_y);
	}
	
	public static void setVSyncEnabled(boolean sync) {
		glfwSwapInterval(sync ? 1 : 0);
	}
	
	public static long getWindow() {
		return Window.handle;
	}
	
	public static void update() {
		update(true);
	}
	
	public static void update(boolean processMessages) {
		try {
			swapBuffers();
			displayDirty = false;
		}
		catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
		
		if (processMessages) processMessages();
	}
	
	public static void processMessages() {
		glfwPollEvents();
		Keyboard.poll();
		Mouse.poll();

		if (latestResized) {
			latestResized = false;
			displayResized = true;
			displayWidth = latestWidth;
			displayHeight = latestHeight;
		}
		else {
			displayResized = false;
		}
	}
	
	public static void swapBuffers() throws LWJGLException {
		glfwSwapBuffers(Window.handle);
	}
	
	public static void destroy() {
		Window.releaseCallbacks();
		glfwDestroyWindow(Window.handle);
		displayCreated = false;
	}
	
	public static void setDisplayMode(DisplayMode dm) throws LWJGLException {
		mode = dm;
	}
	
	public static DisplayMode getDisplayMode() {
		return mode;
	}
	
	public static DisplayMode[] getAvailableDisplayModes() throws LWJGLException {
		GLFWVidMode.Buffer modes = Objects.requireNonNull(glfwGetVideoModes(glfwGetPrimaryMonitor()), "Failed to create video modes!");
		int limit = modes.limit();

		DisplayMode[] displayModes = new DisplayMode[limit];

		for (int i = 0; i < limit; i++) {
			modes.position(i);

			int w = modes.width();
			int h = modes.height();
			int b = modes.redBits() + modes.greenBits() + modes.blueBits();
			int r = modes.refreshRate();

			displayModes[i] = new DisplayMode(w, h, b, r);
		}

		return displayModes;
	}

	public static void setWindowHints(Runnable hints) {
		windowHints = hints;
	}
	
	public static DisplayMode getDesktopDisplayMode() {
		return desktopDisplayMode;
	}
	
	public static boolean wasResized() {
		return displayResized;
	}
	
	public static int getX() {
		return displayX;
	}
	
	public static int getY() {
		return displayY;
	}
	
	public static int getWidth() {
		return displayWidth;
	}
	
	public static int getHeight() {
		return displayHeight;
	}
	
	public static int getFramebufferWidth() {
		return displayFramebufferWidth;
	}
	
	public static int getFramebufferHeight() {
		return displayFramebufferHeight;
	}
	
	public static void setTitle(String title) {
		windowTitle = title;
	}
	
	public static boolean isCloseRequested() {
		return glfwWindowShouldClose(Window.handle);
	}
	
	public static boolean isDirty() {
		return displayDirty;
	}
	
	public static void setInitialBackground(float red, float green, float blue) {
		// This is the closest we can get to the original behavior
		GL11.glClearColor(red, green, blue, 1.0f);
	}
	
	public static int setIcon(ByteBuffer[] icons) {
		try (GLFWImage.Buffer glfwIcons = GLFWImage.malloc(icons.length)) {
			for (int i = 0; i < icons.length; i++) {
				try (GLFWImage image = new GLFWImage(icons[i])) {
					glfwIcons.put(i, image);
				}
			}
			glfwSetWindowIcon(Window.handle, glfwIcons);
		}
		return 0;
	}
	
	public static void setResizable(boolean resizable) {
		displayResizable = resizable;
		// GLFW does not support changing this after window creation
	}
	
	public static boolean isResizable() {
		return displayResizable;
	}
	
	public static void setDisplayModeAndFullscreen(DisplayMode mode) throws LWJGLException {
		// TODO
		System.out.println("TODO: Implement Display.setDisplayModeAndFullscreen(DisplayMode)");
	}
	
	public static void setFullscreen(boolean fullscreen) throws LWJGLException {
		if (fullscreen) {
			lastMonitor = glfwGetWindowMonitor(Window.handle);
			GLFWVidMode vidmode = Objects.requireNonNull(glfwGetVideoMode(lastMonitor), "Failed to get video mode!");
			glfwSetWindowMonitor(Window.handle, lastMonitor, 0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
		} else {
			glfwSetWindowMonitor(Window.handle, lastMonitor, lastX, lastY, lastWidth, lastHeight, GLFW_DONT_CARE);
		}
		Display.fullscreen = fullscreen;
	}
	
	public static boolean isFullscreen() {
		return fullscreen;
	}
	
	public static void setParent(java.awt.Canvas parent) throws LWJGLException {
		// Do nothing as set parent not supported
	}
	
	public static void releaseContext() throws LWJGLException {
		glfwMakeContextCurrent(0);
	}
	
	public static boolean isCurrent() throws LWJGLException {
		return context == glfwGetCurrentContext();
	}
	
	public static void makeCurrent() throws LWJGLException {
		glfwMakeContextCurrent(Window.handle);
	}
	
	public static String getAdapter() {
		// TODO
		return "GeNotSupportedAdapter";
	}
	
	public static String getVersion() {
		// TODO
		return "1.0 NOT SUPPORTED";
	}
	
	/**
	 * An accurate sync method that will attempt to run at a constant frame rate.
	 * It should be called once every frame.
	 *
	 * @param fps - the desired frame rate, in frames per second
	 */
	public static void sync(int fps) {
		Sync.sync(fps);
	}
	
	public static Drawable getDrawable() {
		return null;
	}
	
	static DisplayImplementation getImplementation() {
		return null;
	}
	
	private static class Window {
		static long handle;
		
		static GLFWKeyCallback keyCallback;
		static GLFWCharCallback charCallback;
		static GLFWCursorPosCallback cursorPosCallback;
		static GLFWMouseButtonCallback mouseButtonCallback;
		static GLFWWindowFocusCallback windowFocusCallback;
		static GLFWWindowIconifyCallback windowIconifyCallback;
		static GLFWWindowSizeCallback windowSizeCallback;
		static GLFWWindowPosCallback windowPosCallback;
		static GLFWWindowRefreshCallback windowRefreshCallback;
		static GLFWFramebufferSizeCallback framebufferSizeCallback;
		static GLFWScrollCallback scrollCallback;
		
		public static void setCallbacks() {
			glfwSetKeyCallback(handle, keyCallback);
			glfwSetCharCallback(handle, charCallback);
			glfwSetCursorPosCallback(handle, cursorPosCallback);
			glfwSetMouseButtonCallback(handle, mouseButtonCallback);
			glfwSetWindowFocusCallback(handle, windowFocusCallback);
			glfwSetWindowIconifyCallback(handle, windowIconifyCallback);
			glfwSetWindowSizeCallback(handle, windowSizeCallback);
			glfwSetWindowPosCallback(handle, windowPosCallback);
			glfwSetWindowRefreshCallback(handle, windowRefreshCallback);
			glfwSetFramebufferSizeCallback(handle, framebufferSizeCallback);
			glfwSetScrollCallback(handle, scrollCallback);
		}
		
		public static void releaseCallbacks() {
			keyCallback.free();
			charCallback.free();
			cursorPosCallback.free();
			mouseButtonCallback.free();
			windowFocusCallback.free();
			windowIconifyCallback.free();
			windowSizeCallback.free();
			windowPosCallback.free();
			windowRefreshCallback.free();
			framebufferSizeCallback.free();
		}
	}

}
