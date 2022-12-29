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

import hudson.model.FreeStyleProject;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.JenkinsRule;

import static org.jenkins_ci.plugins.build_keeper.Helper.buildAndAssertKeepForever;

public class BuildKeeperTest extends JenkinsRule {

    public void testEvery3KeepsFirstAndFourthBuild() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder());
        project.getBuildWrappersList().add(createBuildKeeper(3));
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project, 2);
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project);
    }

    public void testDontKeepFailed() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(2, 3, 7));
        project.getBuildWrappersList().add(createBuildKeeper(3, true));
        buildAndAssertKeepForever(false, project, 6);
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project);
    }

    public void testEvery3KeepsFirstAndFourthBuildWhenRelativeCount() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder());
        project.getBuildWrappersList().add(createBuildKeeper(3, false, true));
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project, 2);
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project);
    }

    public void testMarksImmediatelyIfNoPreviousKeptAndRelativeCount() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder());
        buildAndAssertKeepForever(false, project);
        project.getBuildWrappersList().add(createBuildKeeper(3, false, true));
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project, 2);
        buildAndAssertKeepForever(true, project);
    }

    public void testMarksImmediatelyIfNoPreviousKeptWithinCountRange() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder());
        buildAndAssertKeepForever(false, project, 4);
        project.getBuildWrappersList().add(createBuildKeeper(3, false, true));
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project, 2);
        buildAndAssertKeepForever(true, project);
    }

    public void testDoesNotMarkFailedButMarksNextSuccess() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(1, 5, 10, 11, 12, 13));
        project.getBuildWrappersList().add(createBuildKeeper(3, true, true));
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project, 3);
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project, 4);
        buildAndAssertKeepForever(true, project);
        buildAndAssertKeepForever(false, project, 2);
        buildAndAssertKeepForever(true, project);
    }
    
    private BuildKeeper createBuildKeeper(final int buildPeriod) {
        return createBuildKeeper(buildPeriod, false);
    }

    private BuildKeeper createBuildKeeper(final int buildPeriod, final boolean dontKeepFailed) {
        return createBuildKeeper(buildPeriod, dontKeepFailed, false);
    }

    private BuildKeeper createBuildKeeper(final int buildPeriod, final boolean dontKeepFailed, final boolean countFromLastKept) {
        final BuildKeeperPolicy policy = countFromLastKept ? new KeepSincePolicy(buildPeriod, dontKeepFailed)
                                                           : new BuildNumberPolicy(buildPeriod, dontKeepFailed);
        return new BuildKeeper(policy);
    }

}
