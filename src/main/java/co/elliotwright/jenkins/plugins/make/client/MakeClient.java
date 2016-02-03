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

package co.elliotwright.jenkins.plugins.make.client;

import co.elliotwright.jenkins.plugins.make.exception.MakeException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

public class MakeClient {
    private Launcher launcher;
    private TaskListener listener;

    public MakeClient(Launcher launcher, TaskListener listener) {
        this.launcher = launcher;
        this.listener = listener;
    }

    public void runTarget(
            String target,
            FilePath pwd,
            EnvVars env)
        throws IOException, InterruptedException, MakeException {

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        PrintStream logger = listener.getLogger();

        ArgumentListBuilder args = new ArgumentListBuilder();

        args.add(findMakeBinary());
        args.add(target);

        logger.println(" > " + args.toString());

        Launcher.ProcStarter process = launcher.launch()
            .quiet(true)
            .cmds(args.toCommandArray())
            .envs(env)
            .stdout(stdout)
            .stderr(stderr)
        ;

        if (pwd != null) {
            process.pwd(pwd);
        }

        int status = process.start().join();

        if (status != 0) {
            throw new MakeException("Target \"" + target + "\" returned status code " + status + ": " + stderr.toString());
        } else {
            logger.print(stdout.toString());
        }
    }

    /**
     * Attempt to find the location of make
     *
     * @return The location of make
     */
    private String findMakeBinary() {
        // @todo: Actually find the real path...
        return "make";
    }
}
