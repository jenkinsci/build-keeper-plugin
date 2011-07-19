/*
 * The MIT License
 *
 * Copyright (C) 2010-2011 by Anthony Robinson
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
import hudson.model.Run;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class KeepSincePolicy extends PeriodAndFailedPolicy {

    @DataBoundConstructor
    public KeepSincePolicy(final int buildPeriod, final boolean dontKeepFailed) {
        super(buildPeriod, dontKeepFailed);
    }

    @Override
    public void apply(final AbstractBuild build, final BuildListener listener) throws IOException {
        Run current = build;
        final int loop = getBuildPeriod() > 0 ? getBuildPeriod() -1 : 0;
        for (int i = 0; i < loop; i++) {
            if (current.getPreviousBuild() == null) break;
            if (current.getPreviousBuild().isKeepLog()) return;
            current = current.getPreviousBuild();
        }
        keep(build);
    }

    @Extension
    public static class KeepSincePolicyDescriptor extends PeriodAndFailedPolicyDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.keepSincePolicy_displayName();
        }

    }

}
