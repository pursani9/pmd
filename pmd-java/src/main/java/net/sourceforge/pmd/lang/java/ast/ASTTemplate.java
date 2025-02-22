/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.annotation.Experimental;

/**
 * This is a Java 21 Preview feature.
 *
 * <pre class="grammar">
 *
 * Template ::= ({@link ASTTemplateFragment TemplateFragment} {@link ASTExpression Expression}?)* {@link ASTTemplateFragment TemplateFragment}
 *
 * </pre>
 *
 * @see <a href="https://openjdk.org/jeps/430">JEP 430: String Templates (Preview)</a>
 */
@Experimental
public final class ASTTemplate extends AbstractJavaNode {
    ASTTemplate(int i) {
        super(i);
    }

    @Override
    protected <P, R> R acceptVisitor(JavaVisitor<? super P, ? extends R> visitor, P data) {
        return visitor.visit(this, data);
    }
}
