package winterwell.utils.io;
/**
 *
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Static file-related utility functions.
 * @author Daniel Winterstein
 *
 */
public final class FileUtils {

	// private static final String STD_ISO_LATIN = "ISO-8859-1";
	public static final String UTF8 = "UTF8";
	public static final FileFilter TRUE_FILTER = new FileFilter(){
		@Override
		public boolean accept(File pathname) {
			return true;
		}
		@Override
		public String toString() {return "true";};
	};

	static final String ASCII = "ISO8859_1";

	public static final File[] ARRAY = new File[0];
	/**
	 * Append a string to a file. Creates the file if necessary (the parent
	 * directory must already exist though).
	 */
	public static void append(String string, File file) {
		try {
			BufferedWriter w = getWriter(new FileOutputStream(file, true));
			w.write(string);
			close(w);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param file
	 * @param type E.g. "txt" Must not be null. If "", only the last type will
	 * be removed - e.g. foo.bar.txt would be converted to foo.bar
	 * @return A file which is the same as file except for
	 * the type. E.g. "mydir/myfile.html" to "mydir/myfile.txt"
	 */
	public static File changeType(File file, String type) {
		String fName = file.getName();
		int i = fName.lastIndexOf('.');
		if (type.length() == 0) {
			// pop last type
			if (i==-1) return file;
			fName = fName.substring(0,i);
			return new File(file.getParentFile(), fName);
		}
		// pop lead . if present
		if (type.charAt(0) == '.') type = type.substring(1);
		assert type.length() > 0;
		if (i==-1) {
			fName = fName+"."+type;
		} else {
			fName = fName.substring(0,i+1)+type;
		}
		return new File(file.getParentFile(), fName);
	}

	/**
	 * Close, swallowing any exceptions.
	 * @param io Can be null
	 */
	public static void close(Closeable io) {
		if (io == null)
			return;
		try {
			io.close();
		} catch (IOException e) {
			// Already closed?
			if (e.getMessage() != null && e.getMessage().contains("Closed")) return;
			// Swallow!
//			Log.report(e); - bad idea: this can cause an infinite loop if report throws an IOExecption
			e.printStackTrace();
		}
	}
	/**
	 * Convenience method for {@link #copy(File, File, boolean)} with
	 * overwrite=true
	 */
	public static void copy(File in, File out) {
		copy(in, out, true);
	}

	/**
	 * Copy from in to out.
	 *
	 * @param in
	 *            A file or directory. Copying a directory will lead to a merge
	 *            with the target directory, where existing files are left alone
	 *            unless a copied file overwrites them. Note that copying a directory
	 *            *will* copy hidden files such as .svn files.
	 * @param out
	 *            Can be a target file or a directory. Parent directories must
	 *            already exist. If in is a file and out is a directory, the
	 *            name of in will be used to create a target file inside out.
	 * @param overwrite if true existing files will be overwritten. If false,
	 * existing files will lead to an RuntimeException
	 * @throws RuntimeException
	 *             If copying a directory, this is thrown at the end of the
	 *             operation. As many files as possible are copied, then the
	 *             exception is thrown.
	 */
	public static void copy(File in, File out, boolean overwrite)
	throws RuntimeException {
		assert in.exists() : "File does not exist: "+in.getAbsolutePath();
		assert ! in.equals(out) : in+" = "+out+" can cause a delete!";
		// recursively copy directories
		if (in.isDirectory()) {
			ArrayList<File> failed = new ArrayList<File>();
			copyDir(in, out, overwrite, failed);
			// Failed any?
			if (failed.size() != 0)
				throw new RuntimeException("Could not copy files: "+failed);
			return;
		}
		if (out.isDirectory())
			out = new File(out, in.getName());
		try {
			if (out.exists() && !overwrite) {
				throw new RuntimeException("Copy failed: " + out
						+ " already exists.");
			}
			// TODO use NIO for efficiency!
			copy(new FileInputStream(in), out);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage()+" copying "+in.getAbsolutePath()+" to "+out.getAbsolutePath());
		}
	}

	/**
	 * Copy from in to out. Closes both streams when done.
	 *
	 * @param in
	 * @param out
	 */
	public static void copy(InputStream in, File out) {
		assert in != null && out != null;
		if ( ! out.getParentFile().isDirectory()) throw new RuntimeException("Directory does not exist: "+out.getParentFile());
		try {
			FileOutputStream outStream = new FileOutputStream(out);
			copy(in, outStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copy from in to out. Closes both streams when done.
	 *
	 * @param in
	 * @param out
	 */
	public static void copy(InputStream in, OutputStream out) {
		try {
			byte[] bytes = new byte[20 * 1024]; // 20k buffer
			while (true) {
				int len = in.read(bytes);
				if (len == -1)
					break;
				out.write(bytes, 0, len);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(in);
			close(out);
		}
	}

	/**
	 *
	 * @param in
	 * @param out
	 * @param overwrite
	 *            applies to files, not directories. Directories are merged. Use
	 *            delete then copy if you want a true overwrite
	 * @throws RuntimeException
	 *             if a file or directory cannot be copied. This is thrown at
	 *             the end of the operation. As many files as possible are
	 *             copied, then the exception is thrown.
	 */
	private static void copyDir(File in, File out, boolean overwrite,
			List<File> failed) {
		assert in.isDirectory() : in;
		// Create out?
		if (!out.exists()) {
			boolean ok = out.mkdir();
			if (!ok) {
				failed.add(in);
				return;
			}
		}
		assert out.isDirectory() : out;
		for (File f : in.listFiles()) {
			// recurse on dirs
			if (f.isDirectory()) {
				File subOut = new File(out, f.getName());
				copyDir(f, subOut, overwrite, failed);
				continue;
			}
			try {
				copy(f, out, overwrite);
			} catch (RuntimeException e) {
				failed.add(f);
			}
		}
	}

	/**
	 * Runtime exception wrapper for {@link File#createTempFile(String, String)}.
	 * @param prefix
	 * @param suffix
	 */
	public static File createTempFile(String prefix, String suffix) {
		try {
			return File.createTempFile(prefix, suffix);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is a workaround for bugs under Windows.
	 *
	 * @param file Delete this file. Returns quietly if the file does
	 * not exist. If file is null - does nothing.
	 */
	public static void delete(File file) {
		if (!file.exists())
			return;
		boolean ok = file.delete();
		if (ok)
			return;
		System.gc();
		ok = file.delete();
		if (ok)
			return;
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			//
		}
		ok = file.delete();
		if ( ! file.exists())
			return;
		throw new RuntimeException("Could not delete file " + file);
	}

	public static void deleteDir(File file) {
		if ( ! file.isDirectory()) throw new RuntimeException(file+" is not a directory");
		for (File f : file.listFiles()) {
			if (f.isDirectory())
				deleteDir(f);
			else
				delete(f);
		}
		delete(file);
	}

	/**
	 * Recursively search for files by filter. Convenience method for
	 * {@link #find(File, FileFilter, boolean)} with includeHiddenFiles = true
	 *
	 * @param baseDir
	 * @param filter
	 * @return
	 */
	public static List<File> find(File baseDir, FileFilter filter) {
		return find(baseDir, filter, true);
	}

	/**
	 * Recursively search for files by filter
	 *
	 * @param baseDir Must exist and be a directory
	 * @param filter
	 * @param includeHiddenFiles
	 *            If false, hidden files are ignored, as are hidden
	 *            sub-directories. A file is considered hidden if:
	 *            {@link File#isHidden()} returns true or
	 *            the file name begins with a .
	 * @return
	 */
	public static List<File> find(File baseDir, FileFilter filter,
			boolean includeHiddenFiles)
		{
		if ( ! baseDir.isDirectory())
			throw new IllegalArgumentException(baseDir.getAbsolutePath()+" is not a directory");
		List<File> files = new ArrayList<File>();
		find2(baseDir, filter, files, includeHiddenFiles);
		return files;
	}


	private static void find2(File baseDir, FileFilter filter,
			List<File> files, boolean includeHiddenFiles)
	{
		assert baseDir != null && filter != null && files != null;
		for (File f : baseDir.listFiles()) {
			if (f.equals(baseDir)) continue;
			// Hidden?
			if (!includeHiddenFiles && f.isHidden()) {
				continue;
			}
			assert includeHiddenFiles || ! f.getName().startsWith(".") : f;
			// Add?
			if (filter.accept(f)) {
				files.add(f);
			}
			// Recurse
			if (f.isDirectory()) {
				find2(f, filter, files, includeHiddenFiles);
			}
		}
	}

	/**
	 * Retrieve all classes from the specified path.
	 *
	 * @param root
	 *            Root of directory of where to search for classes.
	 * @return List of classes on the form "com.company.ClassName".
	 *
	 * @author Jacob Dreyer, released as public with permission to edit and use on
	 *         http://www.velocityreviews.com/forums/t149403-junit-html-report.html
	 *         Some modifications by Daniel Winterstein
	 */
	private static List<String> getAllClasses(File root) throws IOException {
		assert root != null : "Root cannot be null";

		// Prepare the return array
		List<String> classNames = new ArrayList<String>();

		// Get all classes recursively
		String path = root.getCanonicalPath();
		getAllClasses(root, path.length() + 1, classNames);

		return classNames;
	}

	/**
	 * Retrive all classes from the specified path.
	 *
	 * @param root
	 *            Root of directory of where to search for classes.
	 * @param prefixLength
	 *            Index into root path name of path considered.
	 * @param result
	 *            Array to add classes found
	 */
	private static void getAllClasses(File root, int prefixLength,
			List<String> result) throws IOException {
		assert root != null : "Root cannot be null";
		assert prefixLength >= 0 : "Illegal index specifier";
		assert result != null : "Missing return array";

		// Scan all entries in the directory
		for (File entry : root.listFiles()) {

			// If the entry is a directory, get classes recursively
			if (entry.isDirectory()) {
				if (entry.canRead()) {
					getAllClasses(entry, prefixLength, result);
				}
				continue;
			}
			// Entry is a file. Filter out non-classes and inner classes
			String path = entry.getPath();
			boolean isClass = path.endsWith(".class")
			&& path.indexOf("$") < 0;
			if ( ! isClass) continue;
			String name = entry.getCanonicalPath().substring(
					prefixLength);
			String className = name.replace(File.separatorChar, '.')
			.substring(0, name.length() - 6);
			result.add(className);
		}
	}

	/**
	 *
	 * @param filen
	 * @return file name without last .suffix E.g. foo.bar to "foo"
	 * Note: <i>does</i> strip out directories, so /dir/foo.bar would
	 * go to "foo"
	 */
	public static String getBasename(File file) {
		return getBasename(file.getName());
	}

	/**
	 *
	 * @param filename
	 * @return filename without last .suffix E.g. "foo.foo.bar" to "foo.foo"
	 * Note: does not strip out directories, so "/dir/foo.bar" would
	 * go to "/dir/foo"
	 */
	public static String getBasename(String filename) {
		int i = filename.lastIndexOf('.');
		if (i==-1) return filename;
		return filename.substring(0, i);
	}


	/**
	 * @param file
	 * @return a non-existent file based on the input file. E.g.
	 * given /home/myfile.txt this might return /home/myfile2.txt
	 * @throws RuntimeException if you reach >10000 files of the same base name.
	 * This being a strong sign that perhaps either a clean-up or
	 * some other storage mechanism should be considered.
	 */
	public static File getNewFile(File file) {
		if ( ! file.exists()) return file;
		String path = file.getParent();
		String name = file.getName();
		int dotI = name.lastIndexOf('.');
		String preType;
		String dotType  = "";
		if (dotI==-1) {
			preType = name;
		} else {
			preType = name.substring(0,dotI);
			dotType = name.substring(dotI);
		}
		for(int i=2; i<10000; i++) {
			File f = new File(path, preType+i+dotType);
			if ( ! f.exists()) return f;
		}
		throw new RuntimeException("Could not find a non-existing file name for "+file);
	}

	public static BufferedReader getReader(File file) {
		try {
			return getReader(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * UTF8 and buffered
	 *
	 * @param in
	 * @return
	 */
	public static BufferedReader getReader(InputStream in) {
		try {
			InputStreamReader reader = new InputStreamReader(in, UTF8);
			// \uFFFD
			return new BufferedReader(reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param f
	 * @param base
	 * @return the path of f, relative to base
	 * @throws IllegalArgumentException if f is not a sub path of base
	 */
	public static String getRelativePath(File f, File base) throws IllegalArgumentException {
		try {
			String fp = f.getCanonicalPath();
			String bp = base.getCanonicalPath();
			if (!fp.startsWith(bp)) {
				if (f.equals(base))
					return ""; // Is this what we want?
				throw new IllegalArgumentException(f+"="+fp + " is not a sub-file of "
						+ base+"="+bp);
			}
			String rp = fp.substring(bp.length());
			char ec = rp.charAt(0); // TODO a bit more efficient
			if (ec == '\\' || ec == '/')
				rp = rp.substring(1);
			return rp;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Check that you don't want {@link #getExtension(File)}
	 * @param f
	 * @return "txt", or "". Never null. Always lowercase
	 */
	public static String getType(File f) {
		String fs = f.toString();
		return getType(fs);
	}

	/**
	 * Check that you don't want {@link #getExtension(File)}
	 * @param filename
	 * @return E.g. "txt" Maybe "", never null.
	 */
	public static String getType(String filename) {
		int i = filename.lastIndexOf(".");
		if (i == -1 || i == filename.length() - 1)
			return "";
		return filename.substring(i + 1).toLowerCase();
	}

	/**
	 * Return the full extension of the given file. This includes the leading period.
	 * Always lower case. Can be "", never null
	 * e.g. foo.tar.gz -> ".tar.gz"
	 *      foo/.bar/baz.tgz -> ".tgz"
	 *      baz -> ""
	 */
	public static String getExtension(File f) {
		String filename = f.getName();
		int i = filename.indexOf('.');
		if (i == -1) return "";
		return filename.substring(i).toLowerCase();
	}

	/**
	 * Convenience wrapper for {@link #getExtension(File)}
	 */
	public static String getExtension(String filename) {
		return getExtension(new File(filename));
	}

	/**
	 * @return The application directory, in canonical form.
	 */
	public static File getWorkingDirectory() {
		// String prop = System.getProperty("user.dir");
		try {
			return new File(".").getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedWriter getWriter(File file) {
		try {
			try {
				return new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file), UTF8));
			} catch (UnsupportedEncodingException e) {
				return new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file)));
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * @param out
	 * @return a buffered UTF8 encoded writer.
	 */
	public static BufferedWriter getWriter(OutputStream out) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(out, UTF8);
			return new BufferedWriter(writer);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Move a file. More robust than {@link File#renameTo(File)}, which
	 * can silently fail where a copy+delete would succeed.
	 * @param src
	 * @param dest
	 * @throws RuntimeException
	 *
	 * TODO @testedby {@link FileUtilsTest#testMove()}
	 * TODO test this works properly with relative Files
	 */
	public static void move(File src, File dest) throws RuntimeException{
		assert src.exists();
		// protect the path of the src object from being modified
		File src2 = new File(src.getPath());
		assert src2.equals(src);
		boolean ok = src2.renameTo(dest);
		if (ok) {
			return;
		}
		// oh well: copy+delete
		FileUtils.copy(src, dest);
		FileUtils.delete(src);
	}

	/**
	 * Like append but adds to the start of a file.
	 * Not terribly efficient - involves copying the whole file twice.
	 * @param file If this does not exist it will be created
	 * @param string Must not be null
	 */
	public static void prepend(File file, String string) {
		assert ! file.isDirectory() && string != null;
		// Does the file exist?
		if (! file.exists() || file.length()==0) {
			write(file, string);
			return;
		}
		try {
			File temp = File.createTempFile("prepend", "");
			write(temp, string);
			FileInputStream in = new FileInputStream(file);
			FileOutputStream out = new FileOutputStream(temp, true);
			copy(in, out);
			move(temp, file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param file Will be opened, read and closed
	 * @return The contents of file
	 */
	public static String read(File file) throws RuntimeException {
		try {
			return read(new FileInputStream(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param input Will be read and closed
	 * @return The contents of input
	 */
	public static String read(InputStream in) {
		return read(getReader(in));
	}

	/**
	 * @param r Will be read and closed
	 * @return The contents of input
	 */
	public static String read(Reader r) {
		try {
			BufferedReader reader = r instanceof BufferedReader ? (BufferedReader) r
					: new BufferedReader(r);
			final int bufSize = 8192; // this is the default BufferredReader
			// buffer size
			StringBuilder sb = new StringBuilder(bufSize);
			char[] cbuf = new char[bufSize];
			while (true) {
				int chars = reader.read(cbuf);
				if (chars == -1)
					break;
				sb.append(cbuf, 0, chars);
			}
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			FileUtils.close(r);
		}
	}

	/**
	 * Convert a string so that it can safely be used as a filename. Does not
	 * remove /s or \s. Does allow sub-dirs
	 *
	 * @param name
	 * @return
	 */
	public static String safeFilename(String name) {
		return safeFilename(name, true);
	}

	/**
	 * Checks for the presence of potentially dangerous characters in a filename.
	 * I.e. which could be used for a hacking attack.
	 * This includes checking for the use of ".." to access higher directories.
	 * @param filename
	 * @return true if this filename is kosher
	 */
	public static boolean isSafe(String filename) {
		if (filename==null || filename.matches("\\s+")) return false;
		if (filename.contains("..")) return false;
		if (filename.contains(";")) return false;
		if (filename.contains("|")) return false;
		if (filename.contains(">")) return false;
		if (filename.contains("<")) return false;
		return true;
	}

	/**
	 * Convert a string so that it can safely be used as a filename.
	 * WARNING: Many to one mapping!
	 * TODO make this a one-to-one mapping
	 * @param name
	 * @param if false, will remove /s and \s
	 * @see isSafe
	 */
	public static String safeFilename(String name, boolean allowSubDirs) {
		if (name==null) return "null";
		name = name.trim();
		if (name.equals("")) name="empty";

		// Use _ as a sort of escape character
		name = name.replace("_", "__");
		name = name.replace("..", "_.");
		name = name.replaceAll("[^ a-zA-Z0-9-_.~/\\\\]", "");
		name = name.trim();
		name = name.replaceAll("\\s+", "_");
		if (!allowSubDirs) {
			name = name.replace("/", "_");
			name = name.replace("\\", "_");
		}
		// chars not good for the end of a name
		while("./-\\".indexOf(name.charAt(name.length()-1)) != -1) {
			name = name.substring(0,name.length()-1);
		}
		// impose a max length TODO on a per directory basis
//		12345678901234567890123456789012345678901234567890
		if (name.length() > 50) {
			name = name.substring(0,10) + name.hashCode() + name.substring(name.length()-10);
		}
		return name;
	}

	/**
	 * Create a new file, changing the type
	 * @param f
	 * @param type
	 *            E.g. "txt"
	 * @return E.g. "myfile.html" to "myfile.txt", or "foo" to "foo.txt"
	 */
	public static File setType(File f, String type) {
		assert ! type.startsWith(".") : type;
		String fs = f.toString();
		int i = fs.lastIndexOf(".");
		if (i != -1) {
			fs = fs.substring(0, i);
		}
		return new File(fs + "." + type);
	}

	/**
	 * Write page to file (over-writes if the file already exists), closing streams afterwards.
	 * @param out
	 * @param page
	 */
	public static void write(File out, String page) {
		try {
			BufferedWriter writer = getWriter(new FileOutputStream(out));
			writer.append(page);
			close(writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Count the number of lines in a file.
	 * @param file
	 * @return
	 */
	public static int numLines(File file) {
		int cnt = 0;
		try {
			BufferedReader r = FileUtils.getReader(file);
			while(true) {
				String line = r.readLine();
				if (line==null) break;
				cnt++;
			}
			return cnt;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Like {@link #getBasename(String)}, except this will ignore
	 * endings that are longer than 4 characters.
	 * @param filename
	 * @return e.g. "mybase" from "mybase.html",
	 * but "winterwell.utils.FileUtils" will be unchanged!
	 *
	 * @testedby {@link FileUtilsTest#testGetBasenameCautious()}
	 */
	public static String getBasenameCautious(String filename) {
		int i = filename.lastIndexOf('.');
		if (i==-1) return filename;
		if (filename.length() - i > 5) return filename;
		return filename.substring(0, i);
	}


	/**
	 * TODO test me!
	 * @param f
	 * @return
	 */
	public static boolean isSymLink(File f) {
		try {
			String p = f.getPath();
			String cp = f.getCanonicalPath();		
			if (! cp.endsWith(p)) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


}
