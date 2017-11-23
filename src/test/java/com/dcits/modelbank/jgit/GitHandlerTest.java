package com.dcits.modelbank.jgit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.eclipse.jgit.submodule.SubmoduleStatusType.INITIALIZED;

/**
 * Created on 2017-11-07 21:16.
 *
 * @author kevin
 */
public class GitHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(GitHandlerTest.class);
    private ApplicationContext context;
    private GitHandler gitHandler;

    @Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        gitHandler = (GitHandler) context.getBean("gitHandler");
    }

    @Test
    public void getSubmodules() throws IOException, GitAPIException {
//        SubmoduleWalk walk = gitHandler.getSubmodules();
//        while (walk.next()) {
//            Repository submoduleRepository = walk.getRepository();
//            Iterable<RevCommit> iterable = Git.wrap(submoduleRepository).log().call();
//            Iterator<RevCommit> iterator = iterable.iterator();
//            while (iterator.hasNext()) {
//                logger.info(iterator.next().getFullMessage());
//                logger.info(String.valueOf(iterator.next().getCommitTime()));
//            }
//        }
        Repository repository = gitHandler.getGit().submoduleAdd().setPath("SmartEnsemble").setURI("http://57.25.2.187:8082/dcits/SmartEnsemble.git").call();
        Map<String, SubmoduleStatus> map = new Git(repository).submoduleStatus().call();
        Assert.assertEquals(1, map.size());
        SubmoduleStatus status = map.get("SmartEnsemble");
        Assert.assertEquals(INITIALIZED, status.getType());
    }

    @Test
    public void fileBlame() {
        String commitID = "fc32ede1a325514179b4bbdb4fe4fbb487db0c65";
        String file = "src/main/java/com/dcits/modelbank/utils/Const.java";
        BlameResult result = gitHandler.fileBlame(commitID, file);
        logger.info(result.getResultContents().toString());
    }

    @Test
    public void showLogToday() {
        Iterable<RevCommit> iterable = gitHandler.showLogToday();

        System.out.println();
    }

    @Test
    public void commitAndPushAllChanges() {
        gitHandler.commitAndPushAllChanges("kevin", true);
    }

    @Test
    public void commitAndPush() throws Exception {
        List<String> files = new ArrayList<>();
        files.add("readme.txt");
        gitHandler.commitAndPush(files, "commitAndPush", false);
    }

    @Test
    public void checkoutBranch() throws Exception {
        boolean flag = gitHandler.checkoutBranch("develop");
        Assert.assertEquals(true, flag);
    }

    @Test
    public void stash() throws Exception {
        boolean flag = gitHandler.stash();
        Assert.assertEquals(true, flag);
    }

    @Test
    public void stashList() throws Exception {
        Collection<RevCommit> stashes = gitHandler.stashList();
        for (RevCommit revCommit : stashes) {
            System.out.println(revCommit.getName());
            System.out.println(revCommit);
        }
    }

    @Test
    public void unstash() throws Exception {
        ObjectId objectId = gitHandler.unstash(0);
        System.out.println(objectId);
    }

    @Test
    public void fetch() throws Exception {
    }

    @Test
    public void pull() throws Exception {
        gitHandler.pull();
    }

    @Test
    public void push() throws Exception {
        gitHandler.push();
    }

    @Test
    public void showBranchDiff() throws Exception {
        List<DiffEntry> diffs = gitHandler.showBranchDiff("master", "develop");
        for (DiffEntry entry : diffs) {
            System.out.println(entry + " ====> " + entry.getNewId());
        }
    }

    @Test
    public void showCommitDiff() throws Exception {
    }

    @Test
    public void showDiffFilesByBranches() throws Exception {

    }

    @Test
    public void showDiffFilesByCommits() throws Exception {

    }

    @Test
    public void rollBackPreRevision() throws Exception {

    }

    @Test
    public void test() {
        gitHandler.getCommitsLogByFile();
    }
}