package wftech.worldgenrevisited.virtualpack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.server.packs.resources.IoSupplier;
import wftech.worldgenrevisited.WorldgenRevisited;

public class MemoryBasedIoSupplier implements IoSupplier {

	private byte[] contents = null;
	private InputStream stream = null;
	private String key = null;
	
	public MemoryBasedIoSupplier(String contents, String key) {
		this.contents = contents.getBytes();
		this.stream = new ByteArrayInputStream(this.contents);
		this.key = null;
	}
	
	@Override
	public Object get() throws IOException {
    	//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] Disable this message");
        //StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        //for (StackTraceElement element : stackTrace) {
        //	WorldgenRevisited.LOGGER.error("\t\t" + element.toString());
        //}
		//return stream;
		return stream;
	}

}
