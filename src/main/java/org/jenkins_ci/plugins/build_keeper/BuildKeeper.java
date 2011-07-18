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
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

public class BuildKeeper extends BuildWrapper {

    private int buildPeriod;
    private boolean dontKeepFailed;
    private boolean countFromLastKept;

    @DataBoundConstructor
    public BuildKeeper(final int buildPeriod, final boolean dontKeepFailed, final boolean countFromLastKept) {
        this.buildPeriod = buildPeriod;
        this.dontKeepFailed = dontKeepFailed;
        this.countFromLastKept = countFromLastKept;
    }

    public int getBuildPeriod() {
        return buildPeriod;
    }

    public boolean isDontKeepFailed() {
        return dontKeepFailed;
    }

    public boolean isCountFromLastKept() {
        return countFromLastKept;
    }

    private boolean isKeepFailed() {
        return !dontKeepFailed;
    }

    @Override
    public BuildKeeperDescriptor getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(BuildKeeperDescriptor.class);
    }

    @Override
    public Environment setUp(final AbstractBuild build, final Launcher launcher, final BuildListener listener) {
        return countFromLastKept ? new RelativeCount() : new ByBuildNumber();
    }

    final void keep(final Run run) throws IOException {
        final Result result = run.getResult();
        if (isKeepFailed() || ((result != null) && result.isBetterThan(Result.FAILURE)))
            run.keepLog();
    }

    private class RelativeCount extends Environment {
        @Override
        public boolean tearDown(final AbstractBuild build, final BuildListener listener) throws IOException, InterruptedException {
            if (build == null) return true;
            Run current = build;
            final int loop = buildPeriod > 0 ? buildPeriod -1 : 0;
            for (int i = 0; i < loop; i++) {
                if (current.getPreviousBuild() == null) break;
                if (current.getPreviousBuild().isKeepLog()) return true;
                current = current.getPreviousBuild();
            }
            keep(build);
            return true;
        }        
    }

    private class ByBuildNumber extends Environment {
        @Override
        public boolean tearDown(final AbstractBuild build, final BuildListener listener) throws IOException, InterruptedException {
            if (build == null) return true;
            if ((build.getNumber() -1) % buildPeriod == 0) {
                keep(build);
            }
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

        public FormValidation doCheckBuildPeriod(@QueryParameter final String value) {
            return FormValidation.validatePositiveInteger(value);
        }

    }

}
