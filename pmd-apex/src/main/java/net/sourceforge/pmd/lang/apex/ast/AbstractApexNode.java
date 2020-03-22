/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.ast;

import net.sourceforge.pmd.lang.ast.AbstractNodeWithTextCoordinates;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.SourceCodePositioner;

import apex.jorje.data.Location;
import apex.jorje.data.Locations;
import apex.jorje.semantic.ast.AstNode;
import apex.jorje.semantic.exception.UnexpectedCodePathException;

abstract class AbstractApexNode<T extends AstNode> extends AbstractNodeWithTextCoordinates<ApexNode<?>> implements ApexNode<T> {

    protected final T node;

    protected AbstractApexNode(T node) {
        this.node = node;
    }

    /* package */ void calculateLineNumbers(SourceCodePositioner positioner, int startOffset, int endOffset) {
        // end column will be interpreted as inclusive, while endOffset/endIndex
        // is exclusive
        endOffset -= 1;

        this.beginLine = positioner.lineNumberFromOffset(startOffset);
        this.beginColumn = positioner.columnFromOffset(this.beginLine, startOffset);
        this.endLine = positioner.lineNumberFromOffset(endOffset);
        this.endColumn = positioner.columnFromOffset(this.endLine, endOffset);

        if (this.endColumn < 0) {
            this.endColumn = 0;
        }
    }

    @Override
    public int getBeginLine() {
        if (this.beginLine > 0) {
            return this.beginLine;
        }
        Node parent = getParent();
        if (parent != null) {
            return parent.getBeginLine();
        }
        throw new RuntimeException("Unable to determine beginning line of Node.");
    }

    @Override
    public int getBeginColumn() {
        if (this.beginColumn > 0) {
            return this.beginColumn;
        }
        Node parent = getParent();
        if (parent != null) {
            return parent.getBeginColumn();
        }
        throw new RuntimeException("Unable to determine beginning column of Node.");
    }

    @Override
    public int getEndLine() {
        if (this.endLine > 0) {
            return this.endLine;
        }
        Node parent = getParent();
        if (parent != null) {
            return parent.getEndLine();
        }
        throw new RuntimeException("Unable to determine ending line of Node.");
    }

    @Override
    public int getEndColumn() {
        if (this.endColumn > 0) {
            return this.endColumn;
        }
        Node parent = getParent();
        if (parent != null) {
            return parent.getEndColumn();
        }
        throw new RuntimeException("Unable to determine ending column of Node.");
    }

    @Override
    public final String getXPathNodeName() {
        return this.getClass().getSimpleName().replaceFirst("^AST", "");
    }

    void calculateLineNumbers(SourceCodePositioner positioner) {
        if (!hasRealLoc()) {
            return;
        }

        Location loc = node.getLoc();
        calculateLineNumbers(positioner, loc.getStartIndex(), loc.getEndIndex());
    }

    protected void handleSourceCode(String source) {
        // default implementation does nothing
    }

    @Deprecated
    @InternalApi
    @Override
    public T getNode() {
        return node;
    }

    @Override
    public boolean hasRealLoc() {
        try {
            Location loc = node.getLoc();
            return loc != null && Locations.isReal(loc);
        } catch (UnexpectedCodePathException e) {
            return false;
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            // bug in apex-jorje? happens on some ReferenceExpression nodes
            return false;
        }
    }

    public String getLocation() {
        if (hasRealLoc()) {
            return String.valueOf(node.getLoc());
        } else {
            return "no location";
        }
    }

    @Override
    public String getDefiningType() {
        if (node.getDefiningType() != null) {
            return node.getDefiningType().getApexName();
        }
        return null;
    }

    @Override
    public String getNamespace() {
        if (node.getDefiningType() != null) {
            return node.getDefiningType().getNamespace().toString();
        }
        return null;
    }
}
