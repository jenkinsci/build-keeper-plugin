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
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class BuildKeeper extends BuildWrapper {

    private int buildPeriod;
    private boolean dontKeepFailed;
    private boolean countFromLastKept;
    private BuildKeeperPolicy policy;

    @DataBoundConstructor
    public BuildKeeper(final BuildKeeperPolicy policy) {
        this.policy = policy;
    }

    public BuildKeeperPolicy getPolicy() {
        return policy;
    }

    @Override
    public BuildKeeperDescriptor getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(BuildKeeperDescriptor.class);
    }

    @Override
    public Environment setUp(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
        return new BuildKeeperEnvironment();
    }

    private class BuildKeeperEnvironment extends Environment {
        @Override
        public boolean tearDown(final AbstractBuild build, final BuildListener listener) throws IOException, InterruptedException {
            if (build == null) return true;
            policy.apply(build, listener);
            return true;
        }
    }

    @Extension
    public static class BuildKeeperDescriptor extends BuildWrapperDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.buildKeeper_displayName();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> abstractProject) {
            return true;
        }

        public BuildKeeperPolicy.BuildKeeperPolicyDescriptor getDefaultPolicy() {
            return Hudson.getInstance().getDescriptorByType(ByDayPolicy.ByDayPolicyDescriptor.class);
        }

    }

    public Object readResolve() {
        if (policy == null) {
            policy = countFromLastKept ? new KeepSincePolicy(buildPeriod, dontKeepFailed)
                                       : new BuildNumberPolicy(buildPeriod, dontKeepFailed);
        }
        return this;
    }

}
