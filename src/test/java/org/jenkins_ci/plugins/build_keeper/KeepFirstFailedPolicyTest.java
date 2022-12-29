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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.JenkinsRule;

import static org.jenkins_ci.plugins.build_keeper.Helper.buildAndAssertKeepForever;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KeepFirstFailedPolicyTest extends JenkinsRule {

    public void testDontKeepIfRunTooShort() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(5, 10));
        project.getBuildWrappersList().add(new BuildKeeper(new KeepFirstFailedPolicy(5)));
        buildAndAssertKeepForever(false, project, 11);
        for (FreeStyleBuild build : project.getBuilds())
            assertFalse(Integer.toString(build.getNumber()), build.isKeepLog());
    }

    public void testKeepFirstBuildWhenAllFail() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder());
        project.getBuildWrappersList().add(new BuildKeeper(new KeepFirstFailedPolicy(3)));
        buildAndAssertKeepForever(false, project, 8);
        for (FreeStyleBuild build : project.getBuilds())
            if (build.getNumber() != 1)
                assertFalse(Integer.toString(build.getNumber()), build.isKeepLog());
        assertTrue(project.getBuildByNumber(1).isKeepLog());
    }

    public void testKeepFirstFailedBuildWhenRunOfFail() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(1,2,6));
        project.getBuildWrappersList().add(new BuildKeeper(new KeepFirstFailedPolicy(3)));
        buildAndAssertKeepForever(false, project, 8);
        for (FreeStyleBuild build : project.getBuilds())
            if (build.getNumber() != 3)
                assertFalse(Integer.toString(build.getNumber()), build.isKeepLog());
        assertTrue(project.getBuildByNumber(3).isKeepLog());
    }

    public void testKeepFirstFailedBuildWhenLongRunOfFail() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(1,2));
        project.getBuildWrappersList().add(new BuildKeeper(new KeepFirstFailedPolicy(3)));
        buildAndAssertKeepForever(false, project, 8);
        for (FreeStyleBuild build : project.getBuilds())
            if (build.getNumber() != 3)
                assertFalse(Integer.toString(build.getNumber()), build.isKeepLog());
        assertTrue(project.getBuildByNumber(3).isKeepLog());
    }

    public void testKeepFirstFailedInEachRun() throws Exception {
        final FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new PredictableResultBuilder(1,2,6));
        project.getBuildWrappersList().add(new BuildKeeper(new KeepFirstFailedPolicy(3)));
        buildAndAssertKeepForever(false, project, 11);
        for (FreeStyleBuild build : project.getBuilds())
            if ((build.getNumber() != 3) && (build.getNumber() != 7))
                assertFalse(Integer.toString(build.getNumber()), build.isKeepLog());
        assertTrue(project.getBuildByNumber(3).isKeepLog());
        assertTrue(project.getBuildByNumber(7).isKeepLog());
    }

}
