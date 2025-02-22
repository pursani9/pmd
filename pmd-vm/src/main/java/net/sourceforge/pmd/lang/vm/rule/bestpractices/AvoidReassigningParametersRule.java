/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.vm.rule.bestpractices;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.lang.vm.ast.ASTDirective;
import net.sourceforge.pmd.lang.vm.ast.ASTReference;
import net.sourceforge.pmd.lang.vm.ast.ASTSetDirective;
import net.sourceforge.pmd.lang.vm.rule.AbstractVmRule;

public class AvoidReassigningParametersRule extends AbstractVmRule {

    @Override
    public Object visit(final ASTDirective node, final Object data) {
        if ("macro".equals(node.getDirectiveName())) {
            final Set<String> paramNames = new HashSet<>();
            for (final ASTReference param : node.children(ASTReference.class)) {
                paramNames.add(param.getFirstToken().getImage());
            }
            for (final ASTSetDirective assignment : node.descendants(ASTSetDirective.class)) {
                final ASTReference ref = assignment.firstChild(ASTReference.class);
                if (ref != null && paramNames.contains(ref.getFirstToken().getImage())) {
                    asCtx(data).addViolation(node, ref.getFirstToken().getImage());
                }
            }
        }
        return super.visit(node, data);
    }
}
