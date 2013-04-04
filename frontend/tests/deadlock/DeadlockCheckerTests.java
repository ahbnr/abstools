/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package deadlock;

import java.io.PrintStream;

import org.junit.Assert;

import deadlock.analyser.Analyser;

import abs.frontend.analyser.SemanticErrorList;
import abs.frontend.ast.Model;
import abs.frontend.parser.ParseSamplesTest;
import abs.frontend.typesystem.ExamplesTypeChecking;

public class DeadlockCheckerTests extends ExamplesTypeChecking {

    public DeadlockCheckerTests(String input, String product) {
        super(input, product);
    }

    @Override
    protected Model parse(String input) throws Exception {
        Model m = super.parse(input);
        (new Analyser()).deadlockAnalysis(m, true, 2, System.out);
        return m;
    }
}
