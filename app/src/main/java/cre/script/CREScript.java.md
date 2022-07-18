package cre.script;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import cre.CitedReferencesExplorerFX;
import cre.data.type.abs.CRTable;
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

//StatusBarText status = new StatusBarText()
//StatusBar.get().setUI(status);


public class CREScript {

	public static void main(String[] args) throws CompilationFailedException, IOException {
		Binding bind = new Binding();
		//bind.setVariable("status", status)
		CompilerConfiguration config = new CompilerConfiguration();
		config.setScriptBaseClass("cre.script.CREDSL");
		GroovyShell shell = new GroovyShell(CREScript.class.getClassLoader(), bind, config);

		System.out.println(CitedReferencesExplorerFX.title);

		if (args.length==0) {
			System.out.println("exit");
			return;
		}

		if (Arrays.asList(args).contains("-db")) { 
			CRTable.type = TABLE_IMPL_TYPES.DB;
			System.out.println("***DB MODE***");
		}


		shell.evaluate(new File (args[0]));
	}
}


