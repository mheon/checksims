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
 * Copyright (c) 2014-2015 Nicholas DeMarinis, Matthew Heon, and Dolan Murvihill
 */

package net.lldp.checksims.algorithm.commoncode;

import net.lldp.checksims.submission.Submission;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Do not remove or otherwise interact with common code - just pass through every submission.
 */
public final class CommonCodePassthroughHandler implements CommonCodeHandler {
    private static CommonCodePassthroughHandler instance;

    private CommonCodePassthroughHandler() {}

    /**
     * @return Singleton instance of CommonCodePassthroughHandler
     */
    public static CommonCodePassthroughHandler getInstance() {
        if(instance == null) {
            instance = new CommonCodePassthroughHandler();
        }

        return instance;
    }

    /**
     * Pass through input submissions, not altering them.
     *
     * @param input Submissions to handle common code within
     * @return Unaltered input
     */
    @Override
    public Set<Submission> handleCommonCode(Set<Submission> input) {
        checkNotNull(input);

        return input;
    }

    @Override
    public String toString() {
        return "Singleton instance of CommonCodePassthroughHandler";
    }
}