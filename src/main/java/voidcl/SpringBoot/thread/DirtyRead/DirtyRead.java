package voidcl.SpringBoot.thread.DirtyRead;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import voidcl.SpringBoot.service.StuService;

public class DirtyRead extends Thread {

    StuService stuService;

    public StuService getStuService() {
        return stuService;
    }

    public void setStuService(StuService a) {
        stuService = a;
    }


    public void run() {
        Logger logger = LoggerFactory.getLogger("voidcl.SpringBoot.thread.DirtyRead.DirtyRead");
        logger.debug("sup");
        // LogbackLoggingSystem log= new LogbackLoggingSystem(this.getClass().getClassLoader());
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);


        stuService.mutiInsert();
    }

}
