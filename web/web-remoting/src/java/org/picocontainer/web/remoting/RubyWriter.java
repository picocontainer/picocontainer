package org.picocontainer.web.remoting;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.core.util.Primitives;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RubyWriter implements ExtendedHierarchicalStreamWriter {

    public static final int DROP_ROOT_MODE = 1;
    public static final int STRICT_MODE = 2;

    private final QuickWriter writer;
    private final FastStack elementStack = new FastStack(16);
    private final char[] lineIndenter;

    private int depth;
    private boolean readyForNewLine;
    private boolean tagIsEmpty;
    private final String newLine;
    private int mode;

    public RubyWriter(Writer out) {

        this.writer = new QuickWriter(out);
        this.newLine = "\n";
        this.lineIndenter = new char[]{' ', ' '};

        mode = DROP_ROOT_MODE;

    }

    public void startNode(String name) {
        startNode(name, null);
    }

    @SuppressWarnings("unchecked")
	public void startNode(String name, Class clazz) {
        Node currNode = (Node) elementStack.peek();
        if (currNode == null
                && ((mode & DROP_ROOT_MODE) == 0 || (depth > 0 && !isCollection(clazz)))) {
            writer.write("{");
        }
        if (currNode != null && currNode.fieldAlready) {
            writer.write(",");
            readyForNewLine = true;
        }
        tagIsEmpty = false;
        finishTag();
        if (currNode == null
                || currNode.clazz == null
                || (currNode.clazz != null && !currNode.isCollection)) {
            if (currNode != null && !currNode.fieldAlready) {
                writer.write("{");
                readyForNewLine = true;
                finishTag();
            }
            if ((mode & DROP_ROOT_MODE) == 0 || depth > 0) {
                writer.write(":");
                writer.write(name);
                writer.write(" => ");
            }
        }
        if (isCollection(clazz)) {
            writer.write("[");
            readyForNewLine = true;
        }
        if (currNode != null) {
            currNode.fieldAlready = true;
        }
        elementStack.push(new Node(name, clazz));
        depth++;
        tagIsEmpty = true;
    }

    public class Node {
        public final String name;
		public final Class<?> clazz;
        public boolean fieldAlready;
        public boolean isCollection;

        public Node(String name, Class<?> clazz) {
            this.name = name;
            this.clazz = clazz;
            isCollection = isCollection(clazz);
        }
    }

    public void setValue(String text) {
        Node currNode = (Node) elementStack.peek();
        if (currNode != null && currNode.fieldAlready) {
            startNode("$", String.class);
            tagIsEmpty = false;
            writeText(text, String.class);
            endNode();
        } else {
            if ((mode & (DROP_ROOT_MODE | STRICT_MODE)) == (DROP_ROOT_MODE | STRICT_MODE)
                    && depth == 1) {
                throw new ConversionException("Single value cannot be JSON/Ruby?? root element");
            }
            readyForNewLine = false;
            tagIsEmpty = false;
            finishTag();
            writeText(writer, text);
        }
    }

    public void addAttribute(String key, String value) {
        Node currNode = (Node) elementStack.peek();
        if (currNode == null || !currNode.isCollection) {
            startNode('@' + key, String.class);
            tagIsEmpty = false;
            writeText(value, String.class);
            endNode();
        }
    }

    protected void writeAttributeValue(QuickWriter writer, String text) {
        writeText(text, null);
    }

    protected void writeText(QuickWriter writer, String text) {
        Node foo = (Node) elementStack.peek();

        writeText(text, foo.clazz);
    }

	private void writeText(String text, Class<?> clazz) {
        if (needsQuotes(clazz)) {
            writer.write("\"");
        }
        if ((clazz == Character.class || clazz == Character.TYPE) && "".equals(text)) {
            text = "\0";
        }

        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    this.writer.write("\\\"");
                    break;
                case '\\':
                    this.writer.write("\\\\");
                    break;
                default:
                    if (c > 0x1f) {
                        this.writer.write(c);
                    } else {
                        this.writer.write("\\u");
                        String hex = "000" + Integer.toHexString(c);
                        this.writer.write(hex.substring(hex.length() - 4));
                    }
            }
        }

        if (needsQuotes(clazz)) {
            writer.write("\"");
        }
    }

	private boolean isCollection(Class<?> clazz) {
        return clazz != null
                && (Collection.class.isAssignableFrom(clazz)
                || clazz.isArray()
                || Map.class.isAssignableFrom(clazz) || Map.Entry.class.isAssignableFrom(clazz));
    }

	private boolean needsQuotes(Class<?> clazz) {
        clazz = clazz != null && clazz.isPrimitive() ? clazz : Primitives.unbox(clazz);
        return clazz == null || clazz == Character.TYPE;
    }

    public void endNode() {
        depth--;
        Node node = (Node) elementStack.pop();
        if (node.clazz != null && node.isCollection) {
            if (node.fieldAlready) {
                readyForNewLine = true;
            }
            finishTag();
            writer.write("]");
        } else if (tagIsEmpty) {
            readyForNewLine = false;
            writer.write("{}");
            finishTag();
        } else {
            finishTag();
            if (node.fieldAlready) {
                writer.write("}");
            }
        }
        readyForNewLine = true;
        if (depth == 0 && ((mode & DROP_ROOT_MODE) == 0 || (depth > 0 && !node.isCollection))) {
            writer.write("}");
            writer.flush();
        }
    }

    private void finishTag() {
        if (readyForNewLine) {
            endOfLine();
        }
        readyForNewLine = false;
        tagIsEmpty = false;
    }

    protected void endOfLine() {
        writer.write(newLine);
        for (int i = 0; i < depth; i++) {
            writer.write(lineIndenter);
        }
    }

    public void flush() {
        writer.flush();
    }

    public void close() {
        writer.close();
    }

    public HierarchicalStreamWriter underlyingWriter() {
        return this;
    }

}