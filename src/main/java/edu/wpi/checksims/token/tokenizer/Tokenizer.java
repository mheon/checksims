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

package edu.wpi.checksims.token.tokenizer;

import edu.wpi.checksims.token.TokenList;
import edu.wpi.checksims.token.TokenType;

/**
 * Interface to read in a file and return it as a list of tokens of a certain type.
 */
public interface Tokenizer {
    TokenList splitFile(String string);

    TokenType getType();

    /**
     * Get a Tokenizer for given token type.
     *
     * @param type Type of token
     * @return Tokenizer for given type of token
     */
    static Tokenizer getTokenizer(TokenType type) {
        switch(type) {
            case CHARACTER:
                return CharTokenizer.getInstance();
            case LINE:
                return LineTokenizer.getInstance();
            case WHITESPACE:
                return WhitespaceTokenizer.getInstance();
            default:
                // TODO handle more gracefully
                throw new RuntimeException("Unhandled tokenization requested!");
        }
    }
}
