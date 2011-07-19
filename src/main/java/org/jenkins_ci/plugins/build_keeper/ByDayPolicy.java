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
import java.util.Calendar;

public class ByDayPolicy extends PeriodAndFailedPolicy {

    @DataBoundConstructor
    public ByDayPolicy(final int buildPeriod) {
        super(buildPeriod, true);
    }

    @Override
    public void apply(final AbstractBuild build, final BuildListener listener) throws IOException {
        final long earliestStamp = getEarliestTimestampInRange(build);
        Run current = build;
        while ((current = current.getPreviousBuild()) != null) {
            if (current.getTimeInMillis() < earliestStamp) break;
            if (current.isKeepLog()) return;
        }
        keep(build);
    }

    private long getEarliestTimestampInRange(final AbstractBuild build) {
        final Calendar earliestDay = getMidnight(build);
        int offset = getBuildPeriod() -1;
        if (offset < 0) offset = 0;
        earliestDay.add(Calendar.DAY_OF_YEAR, -offset);
        return earliestDay.getTimeInMillis();
    }

    private Calendar getMidnight(final AbstractBuild build) {
        final Calendar today = build.getTimestamp();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    @Extension
    public static class ByDayPolicyDescriptor extends PeriodAndFailedPolicyDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.byDayPolicy_displayName();
        }

        public int getDefaultBuildPeriod() {
            return 1;
        }

    }

}
