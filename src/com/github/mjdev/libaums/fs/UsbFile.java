package com.github.mjdev.libaums.fs;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface UsbFile extends Closeable {
	public boolean isDirectory();
	public String getName();
	public void setName(String newName) throws IOException;
	public UsbFile getParent();
	public String[] list() throws IOException;
	public UsbFile[] listFiles() throws IOException;
	public long getLength();
	public void setLength(long newLength) throws IOException;
	public void read(long offset, ByteBuffer destination) throws IOException;
	public void write(long offset, ByteBuffer source) throws IOException;
	public void flush() throws IOException;
	@Override
	public void close() throws IOException;
	public UsbFile createDirectory(String name) throws IOException;
	public UsbFile createFile(String name) throws IOException;
	public void moveTo(UsbFile destination) throws IOException;
	public void delete() throws IOException;
}
