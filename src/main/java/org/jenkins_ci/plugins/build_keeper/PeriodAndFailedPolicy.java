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

import hudson.model.Result;
import hudson.model.Run;
import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

public abstract class PeriodAndFailedPolicy extends BuildKeeperPolicy {
    
    private int buildPeriod;
    private boolean dontKeepFailed;

    public PeriodAndFailedPolicy(final int buildPeriod, final boolean dontKeepFailed) {
        this.buildPeriod = buildPeriod;
        this.dontKeepFailed = dontKeepFailed;
    }

    public int getBuildPeriod() {
        return buildPeriod;
    }

    public boolean isDontKeepFailed() {
        return dontKeepFailed;
    }

    public boolean isKeepFailed() {
        return !dontKeepFailed;
    }

    final void keep(final Run run) throws IOException {
        final Result result = run.getResult();
        if (isKeepFailed() || ((result != null) && result.isBetterThan(Result.FAILURE)))
            run.keepLog();
    }

    public static abstract class PeriodAndFailedPolicyDescriptor extends BuildKeeperPolicyDescriptor {

        protected PeriodAndFailedPolicyDescriptor() { }

        protected PeriodAndFailedPolicyDescriptor(Class<? extends BuildKeeperPolicy> clazz) {
            super(clazz);
        }

        public FormValidation doCheckBuildPeriod(@QueryParameter final String value) {
            return FormValidation.validatePositiveInteger(value);
        }

        public int getDefaultBuildPeriod() {
            return 10;
        }

    }

}
