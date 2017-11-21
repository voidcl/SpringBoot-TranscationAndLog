package voidcl.SpringBoot.thread.PhantomRead;

import voidcl.SpringBoot.service.StuService;

public class SimpleInsert extends Thread {
    StuService stuService;

    public void setStuService(StuService a) {
        stuService = a;
    }

    public StuService getStuService() {
        return stuService;
    }

    public void run() {
        stuService.simpleInsert();
    }
}
