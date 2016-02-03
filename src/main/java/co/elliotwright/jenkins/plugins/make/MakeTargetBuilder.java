/**
 * This file is part of the "make" project.
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LICENSE is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package co.elliotwright.jenkins.plugins.make;

import co.elliotwright.jenkins.plugins.make.client.MakeClient;
import co.elliotwright.jenkins.plugins.make.exception.MakeException;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class MakeTargetBuilder extends Builder implements SimpleBuildStep {

    private final String target;

    @DataBoundConstructor
    public MakeTargetBuilder(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(
            @Nonnull Run<?, ?> build,
            @Nonnull FilePath workspace,
            @Nonnull Launcher launcher,
            @Nonnull TaskListener listener)
        throws IOException, InterruptedException {

        MakeClient client = new MakeClient(launcher, listener);
        PrintStream logger = listener.getLogger();

        try {
            client.runTarget(
                target,
                workspace,
                build.getEnvironment(listener)
            );
        } catch (MakeException e) {
            throw new AbortException(e.getMessage());
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'target'.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         * <p>
         * Note that returning {@link FormValidation#error(String)} does not
         * prevent the form from being saved. It just means that a message
         * will be displayed to the user.
         */
        public FormValidation doCheckTarget(@QueryParameter String value) throws IOException, ServletException {
            Pattern targetPattern = Pattern.compile("^[A-Za-z0-9._-]+$");
            Matcher targetMatcher = targetPattern.matcher(value);

            if (value.length() == 0) {
                return FormValidation.error("Please enter a target");
            }

            if (!targetMatcher.find()) {
                return FormValidation.error("Please enter a valid target name");
            }

            return FormValidation.ok();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public String getDisplayName() {
            return "Invoke top-level Make target";
        }
    }
}

