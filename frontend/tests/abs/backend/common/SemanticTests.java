/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.common;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import abs.backend.BackendTestDriver;
import abs.backend.erlang.ErlangTestDriver;
import abs.backend.java.JavaTestDriver;
import abs.backend.java.dynamic.JavaDynamicTestDriver;
import abs.backend.maude.MaudeCompiler;
import abs.backend.maude.MaudeTestDriver;

@RunWith(Parameterized.class)
public abstract class SemanticTests {
    final private BackendTestDriver driver;

    public SemanticTests(BackendTestDriver d) {
        driver = d;
    }

    public static boolean checkMaude() {
        return checkProg("maude");
    }

    public static boolean checkErlang() {
        return checkProg("erl");
    }

    public static boolean checkProg(String... prog) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(prog);
        try {
            Process p = pb.start();
            p.destroy();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        final Collection<Object[]> data = new LinkedList<Object[]>();
        /* TODO: Mark Maude tests as ignored instead of just missing them */
        /*
         * TODO: For the Java backend, we just use different RUNTIME options,
         * not code-gen options. So we could actually just compile the code to
         * Java once, and then run it with the different options.
         */
        /*
         * Append new tests to the end, so that we can aggregate relative
         * differences in CI
         */
        data.add(new Object[] { new JavaTestDriver() });
        data.add(new Object[] { new JavaTestDriver(1) });
        data.add(new Object[] { new JavaDynamicTestDriver() });
        if (checkMaude()) {
            data.add(new Object[] { new MaudeTestDriver(MaudeCompiler.SIMULATOR.RL) });
            data.add(new Object[] { new MaudeTestDriver(MaudeCompiler.SIMULATOR.EQ_TIMED) });
        }
        if (checkErlang()) {
            data.add(new Object[] { new ErlangTestDriver() });
        }
        // FIXME: enable after #302 is done, {new
        // JavaTestDriver(){{absArgs.add("-taskScheduler=simple");}} }
        return data;
    }

    public void assertEvalTrue(String absCode) {
        try {
            driver.assertEvalTrue("module BackendTest; " + absCode);
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO: remove; too many too handle
                                           // for now.
        }
    }

    public void assertEvalFails(String absCode) throws Exception {
        driver.assertEvalFails("module BackendTest; " + absCode);
    }
}
