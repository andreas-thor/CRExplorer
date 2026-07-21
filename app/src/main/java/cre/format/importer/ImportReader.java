package cre.format.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Iterator;

import cre.CRELogger;
import cre.store.mm.PubType_MM;

public abstract class ImportReader implements Iterator<PubType_MM>, AutoCloseable {

	protected PubType_MM entry = null;
	protected BufferedReader br = null;
	protected boolean stop = false;
	private String sourceName = "input stream";
	
	protected abstract void computeNextEntry() throws IOException;
	
	public void init(File file) throws IOException {
		this.sourceName = file.getName();
		this.initReader(new FileInputStream(file));
	}
	

	public void init(InputStream is) throws IOException {
		this.sourceName = "input stream";
		this.initReader(is);
	}

	private void initReader(InputStream is) throws IOException {
		this.entry = null;
		this.br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		this.stop = false;
		try {
			computeNextEntry();
		} catch (IOException e) {
			closeAfterFailedInitialization(e);
			CRELogger.get().logError("Could not read the first import entry: source=" + sourceName, e);
			throw e;
		} catch (RuntimeException e) {
			closeAfterFailedInitialization(e);
			throw e;
		}
	}

	private void closeAfterFailedInitialization(Exception error) {
		try {
			this.br.close();
		} catch (IOException closeError) {
			error.addSuppressed(closeError);
		} finally {
			this.br = null;
		}
	}
	
	
	
	public void stop () {
		this.stop = true;
	}
	
	public boolean hasNext() {
		if (stop) return false;
		return entry != null;
	}
	
	
	public PubType_MM next() {
		PubType_MM result = entry;
		
		try {
			computeNextEntry();
		} catch (IOException e) {
			entry = null;
			CRELogger.get().logError("Could not read the next import entry: source=" + sourceName, e);
			throw new UncheckedIOException("Could not read the next import entry from " + sourceName, e);
		}
		
		return result;
	}
	
	
	
	@Override
	public void close() throws IOException {
		if (br != null) {
			br.close();
			br = null;
		}
	}
	
	public Iterable<PubType_MM> getIterable () { 
		return () -> this;
	}
	
}
