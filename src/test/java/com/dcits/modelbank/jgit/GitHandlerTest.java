package com.dcits.modelbank.jgit;

import com.dcits.modelbank.utils.DateUtil;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.*;

/**
 * Created on 2017-11-07 21:16.
 *
 * @author kevin
 */
public class GitHandlerTest {
    private ApplicationContext context;
    private GitHandler gitHandler;

    @Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        gitHandler = (GitHandler) context.getBean("gitHandler");
    }

    @Test
    public void showLogToday() {

        Date dateNow = new Date();
//        DateUtil.dateToStamp(dateNow);
        System.out.println();
    }

    @Test
    public void commitAndPushAllChanges() {
        gitHandler.commitAndPushAllChanges("kevin", false);
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

}