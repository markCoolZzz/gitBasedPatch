package com.dcits.modelBank.jgit;

import com.dcits.modelBank.utils.Const;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created on 2017-11-06 16:32.
 *
 * @author kevin
 */
public class GitHandler {
    private static final Logger logger = LoggerFactory.getLogger(GitHandler.class);


    /**
     * 回滚到指定版本的上一个版本
     *
     * @param gitRoot     git仓库目录
     * @param diffEntries 需要回滚的文件
     * @param revision    版本号
     * @param remark      备注
     * @return
     * @throws Exception
     */
    public static boolean rollBackPreRevision(String gitRoot, List<DiffEntry> diffEntries,
                                              String revision, String remark) throws Exception {

        if (diffEntries == null || diffEntries.size() == 0) {
            throw new Exception("没有需要回滚的文件");
        }

        Git git = Git.open(new File(gitRoot));

        List<String> files = new ArrayList<String>();

        //注意：下面的reset命令会将暂存区的内容恢复到指定（revesion）的状态，相当于取消add命令的操作
        /*Repository repository = jgit.getRepository();

        RevWalk walk = new RevWalk(repository);
        ObjectId objId = repository.resolve(revision);
        RevCommit revCommit = walk.parseCommit(objId);
        String preVision = revCommit.getParent(0).getName();
        ResetCommand resetCmd = jgit.reset();
        for (String file : files) {
            resetCmd.addPath(file);
        }
        resetCmd.setRef(preVision).call();
        repository.close();*/

        //取出需要回滚的文件，新增的文件不回滚
        for (DiffEntry diffEntry : diffEntries) {
            if (diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                continue;
            } else {
                files.add(diffEntry.getNewPath());
            }
        }

        if (files.size() == 0) {
            throw new Exception("没有需要回滚的文件");
        }

        //checkout操作会丢失工作区的数据，暂存区和工作区的数据会恢复到指定（revision）的版本内容
        CheckoutCommand checkoutCmd = git.checkout();
        for (String file : files) {
            checkoutCmd.addPath(file);
        }
        //加了“^”表示指定版本的前一个版本，如果没有上一版本，在命令行中会报错，例如：error: pathspec '4.vm' did not match any file(s) known to jgit.
        checkoutCmd.setStartPoint(revision + "^");
        checkoutCmd.call();

        //重新提交一次
        CommitCommand commitCmd = git.commit();
        for (String file : files) {
            commitCmd.setOnly(file);
        }
        commitCmd.setCommitter("yonge", "654166020@qq.com").setMessage(remark).call();

        return true;
    }

    /**
     * 获取上一版本的变更记录，如果是新增的文件，不会显示，因为做回滚时不需要回滚新增的文件
     *
     * @param gitRoot  git仓库目录
     * @param revision 版本号
     * @return
     * @throws Exception
     */
    public static List<DiffEntry> rollBackFile(String gitRoot, String revision) throws Exception {

        Git git = Git.open(new File(gitRoot));
        Repository repository = git.getRepository();

        ObjectId objId = repository.resolve(revision);
        Iterable<RevCommit> allCommitsLater = git.log().add(objId).call();
        Iterator<RevCommit> iter = allCommitsLater.iterator();
        RevCommit commit = iter.next();
        TreeWalk tw = new TreeWalk(repository);
        tw.addTree(commit.getTree());
        commit = iter.next();
        if (commit != null) {
            tw.addTree(commit.getTree());
        } else {
            throw new Exception("当前库只有一个版本，不能获取变更记录");
        }

        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));
        List<DiffEntry> diffEntries = rd.compute();
        if (diffEntries == null || diffEntries.size() == 0) {
            return diffEntries;
        }
        Iterator<DiffEntry> iterator = new ArrayList<DiffEntry>(diffEntries).iterator();
        DiffEntry diffEntry = null;
        while (iterator.hasNext()) {
            diffEntry = iterator.next();
            System.out.println("newPath:" + diffEntry.getNewPath() + "    oldPath:"
                    + diffEntry.getOldPath() + "   changeType:"
                    + diffEntry.getChangeType());
            if (diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE) {
                iterator.remove();
            }
        }
        return diffEntries;
    }
}
