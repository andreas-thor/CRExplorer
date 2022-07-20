package cre;

import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;

import cre.scriptlang.ScriptExecutionEngine;

public class Script {
    
    public static void main(String[] args) throws CompilationFailedException, IOException {
        CitedReferencesExplorerFX.updateTitle();
        ScriptExecutionEngine.main(args);
    } 
}
