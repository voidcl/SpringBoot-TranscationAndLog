package voidcl.SpringBoot.thread.info;

import voidcl.SpringBoot.service.StuService;

public class getTwiceInfo extends Thread {

    StuService stuService;

    public StuService getStuService() {
        return stuService;
    }

    public void setStuService(StuService a) {
        stuService = a;
    }

    public void run() {
        stuService.selectTwoTimes();
    }
}
