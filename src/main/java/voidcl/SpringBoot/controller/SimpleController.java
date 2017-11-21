package voidcl.SpringBoot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import voidcl.SpringBoot.service.StuService;
import voidcl.SpringBoot.thread.DirtyRead.DirtyRead;
import voidcl.SpringBoot.thread.NoRepeat.NoRepeat;
import voidcl.SpringBoot.thread.PhantomRead.PhantomRead;
import voidcl.SpringBoot.thread.PhantomRead.SimpleInsert;
import voidcl.SpringBoot.thread.info.getInfo;
import voidcl.SpringBoot.thread.info.getTwiceInfo;

import javax.annotation.Resource;

@Controller
@RequestMapping("/simple")
public class SimpleController {

    @Resource
    StuService stuService;


    @RequestMapping(value = "/dirtyRead", method = RequestMethod.GET)
    public void dirtyRead() {
        DirtyRead t1 = new DirtyRead();
        getInfo t2 = new getInfo();
        t1.setStuService(stuService);
        t2.setStuService(stuService);
        t1.start();
        t2.start();
    }

    @RequestMapping(value = "/repeat", method = RequestMethod.POST)
    public void repeatRead() {
        NoRepeat t1 = new NoRepeat();
        getTwiceInfo t2 = new getTwiceInfo();
        t1.setStuService(stuService);
        t2.setStuService(stuService);
        t2.start();
        t1.start();
    }

    @RequestMapping(value = "/phantom", method = RequestMethod.POST)
    public void phantomRead() {
        PhantomRead t1 = new PhantomRead();
        SimpleInsert t2 = new SimpleInsert();
        t1.setStuService(stuService);
        t2.setStuService(stuService);
        t2.start();
        t1.start();

    }


}
