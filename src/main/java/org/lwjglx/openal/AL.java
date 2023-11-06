package org.lwjglx.openal;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.ALC11;
import org.lwjglx.LWJGLException;
import org.lwjglx.Sys;

public class AL {
	
	static long alContext = -1;
	static ALCdevice alcDevice;
	
	private static boolean created = false;
	
	static {
		Sys.initialize(); // init using dummy sys method
	}
	
	public static void create() throws LWJGLException {
		if (alContext == -1) {
			long device = ALC11.alcOpenDevice((ByteBuffer) null);

			IntBuffer attribs = BufferUtils.createIntBuffer(16);

			attribs.put(org.lwjgl.openal.ALC10.ALC_FREQUENCY);
			attribs.put(44100);

			attribs.put(org.lwjgl.openal.ALC10.ALC_REFRESH);
			attribs.put(60);

			attribs.put(org.lwjgl.openal.ALC10.ALC_SYNC);
			attribs.put(org.lwjgl.openal.ALC10.ALC_FALSE);

			attribs.put(0);
			attribs.flip();
			
			alContext = org.lwjgl.openal.ALC10.alcCreateContext(device, attribs);

			alcDevice = new ALCdevice(device);
			created = true;
		}
	}
	
	public static boolean isCreated() {
		return created;
	}
	
	public static void destroy() {
		org.lwjgl.openal.AL.setCurrentProcess(null);
		alContext = -1;
		alcDevice = null;
		created = false;
	}
	
	public static ALCdevice getDevice() {
		return alcDevice;
	}

}
