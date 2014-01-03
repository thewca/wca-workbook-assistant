package org.worldcubeassociation.workbook;

/**
 * @author Lars Vandenbergh
 */
public enum ColumnOrder {

    WCA,                                    // Any event, any format
    BEST_OF_1_WITH_BEST_COLUMN,             // Any event except multiple blindfolded, best of 1
    MULTI_BLD_WITH_SCORE_FIRST,             // Multiple blindfolded, best of 1
    MULTI_BLD_WITH_SCORE_AND_BEST_FIRST,    // Multiple blindfolded, best of 1
    BLD_WITH_MEAN                           // 3x3x3 blindfolded, best of 3

}
