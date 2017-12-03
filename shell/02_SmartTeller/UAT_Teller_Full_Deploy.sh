#!/bin/bash
source ~/.bashrc

echo **********************************************************
echo **                                                      **
echo **              Teller9 Deploy Shell                    **
echo **              http://www.dcits.com                    **
echo **            author  zhangjig@dcits.com                **
echo **********************************************************

#注意的点 Teller 的启动脚本 start 启动为 ./run.sh ，执行start脚本前需先cd切换到Teller目录下增加执行权限，再sh start，否则会调用不到run.sh

#不同环境下脚本修改指南
#   Var Setting中修改：
#       1、PORT_APP 端口号
#       2、DCITS_HOME 应用部署主目录
#  非阜新银行项目，请注释掉第88行：sed -i 's/ssoindex/fxindex/g' ./configuration/config.ini
#

########## Var Setting START ##########
echo "开始SmartTeller9全量部署"
# 应用端口号，注意需加单引号
PORT_APP='9080'
# 启动应用检查时间间隔设定(单位：10秒)
CHECK_TIME=12

# 应用状态 APP_RUN_STATUS - 0：停止状态；1：启动状态
APP_RUN_STATUS=-10
MSG_START_SUCCESS='Teller应用启动状态'
MSG_STOP_SUCCESS='Teller应用停止状态'
MSG_STOP_FAILD='Teller应用停止失败，请人工停止原应用并部署'
MSG_STATUS_ERROR='Teller应用状态未知,请人工确认当前状态'

DCITS_HOME=/app/dcits
APP_HOME=${DCITS_HOME}
BACKUP_HOME=${DCITS_HOME}/backup/SmartTeller9
ZIP_HOME=${BACKUP_HOME}
VERSION_ID=App_${TAG_NAME}
TARGET=${VERSION_ID}.zip

########## Var Setting END ##########

######## Function START ########
# 检查应用当前状态
CheckAppState() {
    PID_APP=`/usr/sbin/lsof -n -P -t -i :${PORT_APP}`
    echo 'PID_APP:' ${PID_APP}
    APP_RUN_STATUS=`ps -ef | grep ${PID_APP} | grep -v 'grep' | wc -l`
    echo 'APP_RUN_STATUS:' ${APP_RUN_STATUS}
}

# 检查应用是否停止 并返回状态码：停止成功:1；停止失败:0
CheckStopState(){
    CheckAppState
    if [ ${APP_RUN_STATUS} -eq 0 ];then
        # 成功停止
        echo ${MSG_STOP_SUCCESS}
    fi
}

# 检查应用是否启动 并返回状态码：启动成功:1；启动失败:0
CheckStartState() {
    CheckAppState
    if [ ${APP_RUN_STATUS} -eq 1 ]
    then
        echo ${MSG_START_SUCCESS}
    else
        APP_RUN_STATUS=-1
        echo ${MSG_STATUS_ERROR}
    fi
}

CHECK_INTERVAL() {
    for i in `seq $1`
    do
        sleep 10s
        echo 'check' ${i}0s
        CheckAppState
        if [ ${APP_RUN_STATUS} -ne 0 ];then
            break
        fi
    done
}

# 启动teller应用
START_TELLER() {
    cd ${APP_HOME}/SmartTeller9
    sed -i 's/ssoindex/fxindex/g' ./configuration/config.ini
    chmod 755 ${APP_HOME}/SmartTeller9/*
    sh start
}

# 新应用发布成功后，备份被替换的旧应用（主要为日志备份）
BACKUP_OLD_APP() {
    versionNum=`cat ${APP_HOME}/SmartTeller9-old/versionid.txt`
    tar -czf ${BACKUP_HOME}/${versionNum}-end.tar.gz  ${APP_HOME}/SmartTeller9-old
    rm -rf ${APP_HOME}/SmartTeller9-old
#   rm ${BACKUP_HOME}/${versionNum}.zip
}
######## Function END ########

# 备份全量包，并解压包已备部署 DONE
echo "部署的TAG_NAME为："${TAG_NAME}
cd ${BACKUP_HOME}
mkdir SmartTeller9
cd SmartTeller9
unzip ${BACKUP_HOME}/${TARGET}
echo ${VERSION_ID} > ${BACKUP_HOME}/SmartTeller9/versionid.txt
echo ${VERSION_ID} > ${BACKUP_HOME}/SmartTeller9/version_list.txt

# 检查并停止应用，以备部署新应用
CheckStopState
if [ ${APP_RUN_STATUS} -ne 0 ];then
    echo 'Teller stopping ...'
    sh ${APP_HOME}/SmartTeller9/stop.sh
	CHECK_INTERVAL 1
    for i in `seq 3`
    do   
        CheckStopState
        if [ ${APP_RUN_STATUS} -eq 0 ];then
            break
        else
            echo 'Retry Teller stopping ...'
            sh ${APP_HOME}/SmartTeller9/stop.sh skip
        fi
        CHECK_INTERVAL 3
    done
    if [ ${APP_RUN_STATUS} -ne 0 ];then
        # 停止失败
        echo ${MSG_STOP_FAILD}
        exit
    fi
fi

# 备份原应用包
cd ${APP_HOME}
if [[ -d ${APP_HOME}/SmartTeller9-old/ ]];then
    rm -rf ${APP_HOME}/SmartTeller9-old
fi

if [[ -d ${APP_HOME}/SmartTeller9/ ]];then
    mv ${APP_HOME}/SmartTeller9 ${APP_HOME}/SmartTeller9-old
fi

#cd $DCITS_HOME
#echo replace conf
#tar -zxvf ~/backup/Template/telconf.tar.gz


# 部署新的应用包，并启动新应用
mv ${BACKUP_HOME}/SmartTeller9 ${APP_HOME}
echo 'Teller starting ...'
START_TELLER
CHECK_INTERVAL ${CHECK_TIME}

# 检查新部署应用是否启动成功
CheckStartState
if [ ${APP_RUN_STATUS} -eq 1 ];then
    # 新应用启动，删除旧应用
    BACKUP_OLD_APP
    echo ${MSG_START_SUCCESS}
else
    for i in `seq 5`
    do   
        CheckStartState
        if [ ${APP_RUN_STATUS} -eq 1 ];then
            # 新应用启动，删除旧应用
            echo "Start successful, deleting old app ..."
            BACKUP_OLD_APP
            echo ${MSG_START_SUCCESS}
            break
        else
            echo 'Retry Teller starting ...'
            START_TELLER
        fi
        CHECK_INTERVAL ${CHECK_TIME}
    done
    if [ ${APP_RUN_STATUS} -eq 0 ];then
        # 新部署应用多次尝试启动失败，未知异常待人工检查状态
        echo ${MSG_STATUS_ERROR}
    fi
fi

echo "结束SmartTeller9全量部署。。。"