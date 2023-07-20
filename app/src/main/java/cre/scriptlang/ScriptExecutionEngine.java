package cre.scriptlang;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import cre.CitedReferencesExplorerFX;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import cre.store.db.CRTable_DB;

public class ScriptExecutionEngine {

	public static void main(String[] args) throws CompilationFailedException, IOException {

		Binding bind = new Binding();
		CompilerConfiguration config = new CompilerConfiguration();  
		config.setScriptBaseClass("cre.scriptlang.DSL");
		GroovyShell shell = new GroovyShell(ScriptExecutionEngine.class.getClassLoader(), bind, config);

		System.out.println(CitedReferencesExplorerFX.title);

		CitedReferencesExplorerFX.setDatabaseParams(args);
		if (CRTable.type == TABLE_IMPL_TYPES.DB) {
			System.out.println(String.format("***DB MODE*** (%s)", CRTable_DB.url)); 
		}
		
		//shell.run (new File (this.args[0]));
		shell.evaluate(new File (args[0]));

	}

}
