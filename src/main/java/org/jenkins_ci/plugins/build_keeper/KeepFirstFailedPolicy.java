/*
 * The MIT License
 *
 * Copyright (C) 2012 by Anthony Robinson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkins_ci.plugins.build_keeper;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class KeepFirstFailedPolicy extends BuildKeeperPolicy {

    private final int numberOfFails;

    @DataBoundConstructor
    public KeepFirstFailedPolicy(int numberOfFails) {
        this.numberOfFails = numberOfFails;
    }

    public int getNumberOfFails() {
        return numberOfFails;
    }

    @Override
    public void apply(final AbstractBuild build, final BuildListener listener) throws IOException {
        if ((build.getResult() == null) || (build.getResult() != Result.FAILURE)) return;
        Run failed = build;
        for (int i = 1; i < numberOfFails; i++) {
            failed = failed.getPreviousBuild();
            if (failed == null) return;
            if ((failed.getResult() == null) || (failed.getResult() != Result.FAILURE)) return;
        }
        final Run previous = failed.getPreviousBuild();
        if ((previous == null) || ((previous.getResult() != Result.FAILURE)))
            failed.keepLog(true);
    }

    @Extension
    public static class KeepFirstFailedPolicyDescriptor extends BuildKeeperPolicyDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.keepFirstFailedPolicy_displayName();
        }

    }

}
