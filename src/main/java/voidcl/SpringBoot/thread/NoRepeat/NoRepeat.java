package voidcl.SpringBoot.thread.NoRepeat;

import voidcl.SpringBoot.service.StuService;

public class NoRepeat extends Thread {

    StuService stuService;

    public StuService getStuService() {
        return stuService;
    }

    public void setStuService(StuService a) {
        stuService = a;
    }

    public void run() {
        stuService.delayUpdate();
    }
}
