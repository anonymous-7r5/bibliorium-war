package br.com.fortium.bibliorium.print;

import java.util.Map;

public interface Printable {
	String FILE_EXTENSION = ".txt";
	String RELATION       = "=";
	String SEPARATOR      = "_";
	String DIVISION       = "--------------------------------------------------------";
	String LINE_BREAK     = "\n";
	
	Map<String,String> getPrintableInfo();
}