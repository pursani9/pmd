/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.lang.ast.test.BaseParsingHelper;
import net.sourceforge.pmd.lang.ast.test.BaseTreeDumpTest;
import net.sourceforge.pmd.lang.ast.test.RelevantAttributePrinter;
import net.sourceforge.pmd.lang.java.JavaParsingHelper;

class Java21TreeDumpTest extends BaseTreeDumpTest {
    private final JavaParsingHelper java21 =
            JavaParsingHelper.DEFAULT.withDefaultVersion("21")
                    .withResourceContext(Java21TreeDumpTest.class, "jdkversiontests/java21/");
    private final JavaParsingHelper java20 = java21.withDefaultVersion("20");

    Java21TreeDumpTest() {
        super(new RelevantAttributePrinter(), ".java");
    }

    @Override
    public BaseParsingHelper<?, ?> getParser() {
        return java21;
    }

    @Test
    void patternMatchingForSwitch() {
        doTest("Jep441_PatternMatchingForSwitch");
    }

    @Test
    void patternMatchingForSwitchBeforeJava21() {
        ParseException thrown = assertThrows(ParseException.class, () -> java20.parseResource("Jep441_PatternMatchingForSwitch.java"));
        assertTrue(thrown.getMessage().contains("Patterns in switch statements is a preview feature of JDK 20, you should select your language version accordingly"));
    }

    @Test
    void dealingWithNull() {
        doTest("DealingWithNull");
    }

    @Test
    void dealingWithNullBeforeJava21() {
        ParseException thrown = assertThrows(ParseException.class, () -> java20.parseResource("DealingWithNull.java"));
        assertTrue(thrown.getMessage().contains("Null in switch cases is a preview feature of JDK 20, you should select your language version accordingly"),
                "Unexpected message: " + thrown.getMessage());
    }


    @Test
    void enhancedTypeCheckingSwitch() {
        doTest("EnhancedTypeCheckingSwitch");
    }

    @Test
    void exhaustiveSwitch() {
        doTest("ExhaustiveSwitch");
    }

    @Test
    void guardedPatterns() {
        doTest("GuardedPatterns");
    }

    @Test
    void guardedPatternsBeforeJava21() {
        ParseException thrown = assertThrows(ParseException.class, () -> java20.parseResource("GuardedPatterns.java"));
        assertTrue(thrown.getMessage().contains("Patterns in switch statements is a preview feature of JDK 20, you should select your language version accordingly"),
                "Unexpected message: " + thrown.getMessage());
    }

    @Test
    void patternsInSwitchLabels() {
        doTest("PatternsInSwitchLabels");
    }

    @Test
    void patternsInSwitchLabelsBeforeJava21() {
        ParseException thrown = assertThrows(ParseException.class, () -> java20.parseResource("PatternsInSwitchLabels.java"));
        assertTrue(thrown.getMessage().contains("Patterns in switch statements is a preview feature of JDK 20, you should select your language version accordingly"),
                "Unexpected message: " + thrown.getMessage());
    }

    @Test
    void refiningPatternsInSwitch() {
        doTest("RefiningPatternsInSwitch");
    }

    @Test
    void scopeOfPatternVariableDeclarations() {
        doTest("ScopeOfPatternVariableDeclarations");
    }

    @Test
    void recordPatterns() {
        doTest("RecordPatterns");
    }

    @Test
    void recordPatternsBeforeJava21Preview() {
        ParseException thrown = assertThrows(ParseException.class, () -> java21.parseResource("RecordPatterns.java"));
        assertTrue(thrown.getMessage().contains("Deconstruction patterns is a preview feature of JDK 20, you should select your language version accordingly"),
                "Unexpected message: " + thrown.getMessage());
    }

    @Test
    void recordPatternsInEnhancedFor() {
        doTest("RecordPatternsInEnhancedFor");
    }

    @Test
    void recordPatternsInEnhancedForBeforeJava21Preview() {
        ParseException thrown = assertThrows(ParseException.class, () -> java21.parseResource("RecordPatternsInEnhancedFor.java"));
        assertTrue(thrown.getMessage().contains("Deconstruction patterns in enhanced for statement is a preview feature of JDK 20, you should select your language version accordingly"),
                "Unexpected message: " + thrown.getMessage());
    }

    @Test
    void recordPatternsExhaustiveSwitch() {
        doTest("RecordPatternsExhaustiveSwitch");
    }
}
