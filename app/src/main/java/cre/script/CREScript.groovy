package cre.script

import org.codehaus.groovy.control.CompilerConfiguration

import cre.CitedReferencesExplorerFX
import cre.data.type.abs.CRTable
import cre.data.type.abs.CRTable.TABLE_IMPL_TYPES
import cre.ui.statusbar.StatusBar
import cre.ui.statusbar.StatusBarText

//StatusBarText status = new StatusBarText()
//StatusBar.get().setUI(status);

def bind = new Binding()
//bind.setVariable("status", status)
def config = new CompilerConfiguration()  
config.scriptBaseClass = 'cre.script.CREDSL'                                  
def shell = new GroovyShell(this.class.classLoader, bind, config)             

println CitedReferencesExplorerFX.title

if (this.args.contains("-db")) { 
	CRTable.type = TABLE_IMPL_TYPES.DB;
	println "***DB MODE***"
}

shell.evaluate(new File (this.args[0]));

