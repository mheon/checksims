/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 * Copyright (c) 2014-2015 Matthew Heon and Dolan Murvihill
 */

package edu.wpi.checksims.util.output;

import edu.wpi.checksims.algorithm.similaritymatrix.SimilarityMatrix;
import edu.wpi.checksims.algorithm.similaritymatrix.output.MatrixPrinter;

/**
 * Print the output of a Checksims computation.
 */
public interface OutputPrinter {
    /**
     * Provide Checksims' output in a usable form.
     *
     * @param toPrint Output matrix to print
     * @param printWith Strategy to use when printing
     */
    void print(SimilarityMatrix toPrint, MatrixPrinter printWith);
}
