package uk.ac.cam.aks73.fjava.tick2star;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.security.SecureClassLoader;

public class SafeObjectInputStream extends ObjectInputStream {

	private SecureClassLoader current = (SecureClassLoader) SecureClassLoader.getSystemClassLoader();

	public SafeObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,	ClassNotFoundException {
		try {
			return current.loadClass(desc.getName());
		}
		catch (ClassNotFoundException e) {
			return super.resolveClass(desc);
		}
	}

	public void addClass(final String name, final byte[] defn) {
		current = new SecureClassLoader(current) {
		@Override
		protected Class<?> findClass(String className)throws ClassNotFoundException {
			if (className.equals(name)) {
				Class<?> result = defineClass(name, defn, 0, defn.length);
				return result;
			}
			else {
				throw new ClassNotFoundException();
			}
		}
		};
	}

}
