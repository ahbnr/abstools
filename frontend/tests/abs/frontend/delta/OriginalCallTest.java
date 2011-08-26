/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.frontend.delta;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import abs.frontend.ast.*;
import abs.frontend.delta.exceptions.*;


public class OriginalCallTest extends DeltaFlattenerTest {

    @Test
    public void originalCall() throws ASTNodeNotFoundException {
        Model model = assertParseOk(
                "module M;"
                + "class C { Unit m() {} }"
                + "delta D { modifies class C { modifies Unit m() { original(); } } }"
        );
        ClassDecl cls = (ClassDecl) findDecl(model, "M", "C");
        assertTrue(cls.getMethods().getNumChild() == 1);
        
        DeltaDecl delta = (DeltaDecl) findDecl(model, "M", "D");
        assertTrue(delta.getClassOrIfaceModifiers().getNumChild() == 1);
        
        model.resolveOriginalCalls(new ArrayList<DeltaDecl>(Arrays.asList(delta)));
        assertTrue(delta.getClassOrIfaceModifiers().getNumChild() == 2);
        
        model.applyDelta(delta);
        
        // there should be two methods now: original and added-by-delta
        assertTrue(cls.getMethods().getNumChild() == 2);
        assertTrue(cls.getMethod(0).getMethodSig().getName().equals("m"));
        assertTrue(cls.getMethod(1).getMethodSig().getName().startsWith("m_Orig"));

    }


}
