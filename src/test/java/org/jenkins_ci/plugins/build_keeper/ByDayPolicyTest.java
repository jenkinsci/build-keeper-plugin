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

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.jenkins_ci.plugins.build_keeper.Helper.buildAndAssertKeepForever;
import static org.junit.Assert.assertEquals;

public class ByDayPolicyTest extends JenkinsRule {

    static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    public void testNoPreviousBuildsWillKeep() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(1));
        project.getBuildWrappersList().add(new BuildKeeper(new ByDayPolicy(50)));
        buildAndAssertKeepForever(true, project);
    }

    public void testEveryDayWIllKeepBuildOnFirstMinuteOfDay() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(1, 2, 3, 4, 5, 6, 7, 8, 9));
        final BuildDateSetterBuilder dateSetterBuilder = new BuildDateSetterBuilder("17/07/2011 23:59:59.999");
        project.getBuildersList().add(dateSetterBuilder);
        project.getBuildWrappersList().add(new BuildKeeper(new ByDayPolicy(1)));
        // just before midnight yesterday 
        buildAndAssertKeepForever(true, project);
        // midnight yesterday
        dateSetterBuilder.setDate("18/07/2011 00:00:00.000");
        buildAndAssertKeepForever(true, project);
        // a little later
        dateSetterBuilder.setDate("18/07/2011 00:00:00.001");
        buildAndAssertKeepForever(false, project);
        // just before midnight
        dateSetterBuilder.setDate("18/07/2011 23:59:59.999");
        buildAndAssertKeepForever(false, project);
        // midnight
        dateSetterBuilder.setDate("19/07/2011 00:00:00.000");
        buildAndAssertKeepForever(true, project);
        // a little later
        dateSetterBuilder.setDate("19/07/2011 00:00:00.001");
        buildAndAssertKeepForever(false, project);
        // tomorrow
        dateSetterBuilder.setDate("20/07/2011 00:00:00.000");
        buildAndAssertKeepForever(true, project);
    }

    public void testKeepsFirstSuccessOfDay() throws Exception {
        final Calendar reference = getStartOfDay();
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(4, 5, 6));
        final BuildDateSetterBuilder dateSetterBuilder = new BuildDateSetterBuilder(reference);
        project.getBuildersList().add(dateSetterBuilder);
        project.getBuildWrappersList().add(new BuildKeeper(new ByDayPolicy(1)));
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.HOUR_OF_DAY, 2);
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.HOUR_OF_DAY, 2);
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.HOUR_OF_DAY, 2);
        buildAndAssertKeepForever(true, project);
        reference.add(Calendar.HOUR_OF_DAY, 2);
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.HOUR_OF_DAY, 2);
        buildAndAssertKeepForever(false, project);
    }

    public void testKeepEvery3Days() throws Exception {
        final Calendar reference = getStartOfDay();
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(1, 2, 3, 4, 5, 7, 8, 9));
        final BuildDateSetterBuilder dateSetterBuilder = new BuildDateSetterBuilder(reference);
        project.getBuildersList().add(dateSetterBuilder);
        project.getBuildWrappersList().add(new BuildKeeper(new ByDayPolicy(3)));
        dateSetterBuilder.setDate("19/07/2011 00:00:00.000");
        buildAndAssertKeepForever(true, project);
        reference.add(Calendar.DAY_OF_YEAR, 1);
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.DAY_OF_YEAR, 1);
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.HOUR_OF_DAY, 5);
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.HOUR_OF_DAY, 3);
        buildAndAssertKeepForever(false, project);
        reference.add(Calendar.DAY_OF_YEAR, 1);
        buildAndAssertKeepForever(false, project); // #6 failed build
        reference.add(Calendar.HOUR_OF_DAY, 1);
        buildAndAssertKeepForever(true, project);
        reference.add(Calendar.DAY_OF_YEAR, 1);
        buildAndAssertKeepForever(false, project);
    }

    public void testBuildDateSetterBuilder() throws Exception {
        final Calendar inPast = Calendar.getInstance();
        inPast.add(Calendar.DAY_OF_YEAR, -2);
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new BuildDateSetterBuilder(inPast));
        final FreeStyleBuild build = buildAndAssertSuccess(project);
        assertEquals(inPast.getTimeInMillis(), build.getTimeInMillis());
    }

    private Calendar getStartOfDay() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private static class BuildDateSetterBuilder extends TestBuilder {

        private Calendar calendar;

        public BuildDateSetterBuilder(final String date) throws Exception {
            this(Calendar.getInstance());
            setDate(date);
        }

        public BuildDateSetterBuilder(final Calendar calendar) {
            this.calendar = calendar; // no clone to allow external manipulation between builds
        }

        public void setDate(final String date) throws Exception {
            calendar.setTime(format.parse(date));
        }

        @Override
        public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
                throws InterruptedException, IOException {
            System.out.println("Setting date to: " + format.format(new Date(calendar.getTimeInMillis())));
            try {
                final Field buildDate = Run.class.getDeclaredField("timestamp");
                buildDate.setAccessible(true);
                buildDate.setLong(build, calendar.getTimeInMillis());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return true;
        }

    }

}
