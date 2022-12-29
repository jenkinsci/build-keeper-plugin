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

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import jenkins.model.Jenkins;

import java.io.IOException;

public abstract class BuildKeeperPolicy implements Describable<BuildKeeperPolicy>, ExtensionPoint {

    public static DescriptorExtensionList<BuildKeeperPolicy, BuildKeeperPolicyDescriptor> all() {
        return Jenkins.get().<BuildKeeperPolicy, BuildKeeperPolicyDescriptor>getDescriptorList(BuildKeeperPolicy.class);
    }

    public abstract void apply(AbstractBuild build, BuildListener listener) throws IOException;

    public BuildKeeperPolicyDescriptor getDescriptor() {
        return (BuildKeeperPolicyDescriptor)Jenkins.get().getDescriptor(getClass());
    }

    public static abstract class BuildKeeperPolicyDescriptor extends Descriptor<BuildKeeperPolicy> {

        protected BuildKeeperPolicyDescriptor() { }

        protected BuildKeeperPolicyDescriptor(Class<? extends BuildKeeperPolicy> clazz) {
            super(clazz);
        }

    }

}
