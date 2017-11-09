package com.dcits.modelbank.utils;

/**
 * 常用的常量归到此处，统一管理
 * Created on 2017-11-07 16:05.
 *
 * @author kevin
 */
public interface Const {
    String JGIT = "JGIT";
    String GIT = ".git";
    String REF_REMOTES="refs/remotes/origin/";
    String REFS_HEADS="refs/heads/";
    /**
     * pull拉取方式
     */
    String MERGE="merge";
    String REBASE="rebase";

    String AUTHOR_NAME = "name";
    String VERSION_ID = "versionID";
    String TIMESTAMP = "timestamp";
    String DESC = "desc";
    String CHANGE_TYPE = "changeType";
    String CHECK = "check";
}
