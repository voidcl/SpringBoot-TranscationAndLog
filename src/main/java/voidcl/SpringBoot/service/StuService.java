package voidcl.SpringBoot.service;

import voidcl.SpringBoot.entity.Student;

import java.util.List;

public interface StuService {

    void insertStu(Student data);


    List<Student> selectAllStu();

    Student selectById(String id);

    void selectTwoTimes();

    List<String> selectFake();

    void updateStuName(String id);

    void delayUpdate();

    void mutiInsert();

    void updateById(String id);

    void printInfo();

    void phantomRead();

    void simpleInsert();
}
