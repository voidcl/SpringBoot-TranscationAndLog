package voidcl.SpringBoot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import voidcl.SpringBoot.dao.AData.StuMapper;
import voidcl.SpringBoot.entity.Student;
import voidcl.SpringBoot.service.StuService;

import java.util.List;

@Service
public class StuServiceImpl implements StuService {

    @Autowired
    StuMapper stuMapper;

    @Override
    public void insertStu(Student data) {
        stuMapper.insertStu(data);

    }

    @Override
    public List<Student> selectAllStu() {
        return stuMapper.selectAllStu();
    }

    @Override
    public Student selectById(String id) {
        return stuMapper.selectById(id);
    }

    @Override
    public void updateStuName(String name) {
        stuMapper.updateStuName(name);
    }

    @Override
    public void updateById(String id) {
        stuMapper.updateById(id);
    }

    @Override
    public List<String> selectFake() {
        return stuMapper.selectFake();
    }


    @Transactional
            (value = "primaryTestManager",
                    isolation = Isolation.REPEATABLE_READ,
                    propagation = Propagation.NESTED)
    //当isolation低于READ_COMMITTED等级（推荐等级）时，发生脏读
    public void printInfo() {
        try {
            Thread.sleep(2000);
            List<Student> list = selectAllStu();
            for (Student data : list) {
                System.out.println(data.getId() + "   " + data.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Transactional
            (value = "primaryTestManager")
    //模拟两个人在同一个事务周期添加数据，第二个人添加失败，事务回滚
    public void mutiInsert() {
        insertStu(new Student("one", "ffet"));
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        insertStu(new Student("error", "error"));
    }


    @Transactional
            (value = "primaryTestManager",
                    isolation = Isolation.READ_COMMITTED)
    //当isolation低于REPEATABLE_READ等级时，发生不可重复读
    //RR级别，同一事物到select是快照读，mysql innodb引擎通过mvcc机制实现

    public void selectTwoTimes() {
        try {

            Student data = selectById("error");
            System.out.println(data.getId() + "   " + data.getName());
            Thread.sleep(5000);
//            Student data2 = selectById("error");
//            System.out.println(data2.getId()+"  "+data2.getName());
            List<Student> data2 = selectAllStu();
            for (Student a : data2) {
                if (a.getId().equals("error"))
                    System.out.println(a.getId() + "  " + a.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //延迟插入
    @Transactional(value = "primaryTestManager")
    public void delayUpdate() {
        try {
            Thread.sleep(2000);
            updateStuName("suc");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Transactional(
            value = "primaryTestManager",
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    //当isolation低于SERIALIZABLE等级时（且属于no-RR），发生幻读
    //在默认隔离级别REPEATABLE READ下，同一事务的所有一致性读只会读取第一次查询时创建的快照
    public void phantomRead() {
        try {
            List<String> list = selectFake();
            System.out.println(list.size());
            Thread.sleep(5000);
            List<Student> list2 = selectAllStu();
            System.out.println(list2.size());
            Thread.sleep(5000);
            List<Student> list3 = selectAllStu();
            System.out.println(list3.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional(
            value = "primaryTestManager",
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public void simpleInsert() {
        try {
            Thread.sleep(2000);
            insertStu(new Student("suc", "err"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
