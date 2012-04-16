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
import hudson.model.Descriptor;
import org.jenkins_ci.plugins.run_condition.RunCondition;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunConditionPolicy extends BuildKeeperPolicy {

    private static Logger LOGGER = Logger.getLogger("org.jenkins_ci.plugins.build_keeper.RunConditionPolicy");
    private final RunCondition runCondition;
    private final boolean keepBuildIfEvalFails;
    
    @DataBoundConstructor
    public RunConditionPolicy(final RunCondition runCondition, final boolean keepBuildIfEvalFails) {
        this.runCondition = runCondition;
        this.keepBuildIfEvalFails = keepBuildIfEvalFails;
    }

    public RunCondition getRunCondition() {
        return runCondition;
    }

    public boolean isKeepBuildIfEvalFails() {
        return keepBuildIfEvalFails;
    }

    @Override
    public void apply(AbstractBuild build, BuildListener listener) throws IOException {
        try {
            if (runCondition.runPerform(build, listener))
                build.keepLog(true);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, Messages.runConditionPolicy_log_failedToEvaluate(), e);
            listener.getLogger().println(Messages.runConditionPolicy_log_failedToEvaluate());
            listener.getLogger().println(e.getLocalizedMessage());
            if (keepBuildIfEvalFails)
                build.keepLog(true);
        }
    }

    @Extension
    public static class RunConditionPolicyDescriptor extends BuildKeeperPolicyDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.runConditionPolicy_displayName();
        }

        public List<? extends Descriptor<? extends RunCondition>> getRunConditions() {
            return RunCondition.all();
        }

    }

}
