/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.coco;

import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.lang.LanguagePropertyBundle;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.coco.cpd.CocoTokenizer;
import net.sourceforge.pmd.lang.impl.CpdOnlyLanguageModuleBase;

/**
 * Language implementation for Coco.
 */
public class CocoLanguageModule extends CpdOnlyLanguageModuleBase {
    private static final String ID = "coco";

    public CocoLanguageModule() {
        super(LanguageMetadata.withId(ID).name("Coco").extensions("coco"));
    }

    public static CocoLanguageModule getInstance() {
        return (CocoLanguageModule) LanguageRegistry.CPD.getLanguageById(ID);
    }

    @Override
    public Tokenizer createCpdTokenizer(LanguagePropertyBundle bundle) {
        return new CocoTokenizer();
    }
}
