package com.changlianxi.task;

import com.changlianxi.data.Circle;
import com.changlianxi.data.CircleMember;
import com.changlianxi.data.enums.RetError;
import com.changlianxi.db.DBUtils;

/**
 * 拒绝圈子邀请
 * @author teeker_bin
 *
 */
public class RefuseCircleInvitationTask extends
        BaseAsyncTask<CircleMember, Void, RetError> {
    private CircleMember member;

    @Override
    protected RetError doInBackground(CircleMember... params) {
        member = params[0];
        member.read(DBUtils.getDBsa(1));
        RetError ret = member.refuseInvitation();
        if (ret == RetError.NONE) {
            Circle c = new Circle(member.getCid());
            c.setStatus(com.changlianxi.data.AbstractData.Status.DEL);
            c.write(DBUtils.getDBsa(2));
        }
        return ret;
    }

}
